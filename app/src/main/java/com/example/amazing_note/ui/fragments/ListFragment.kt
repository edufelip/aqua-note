package com.example.amazing_note.ui.fragments

import android.os.Bundle
import android.view.*
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.amazing_note.R
import com.example.amazing_note.data.models.Note
import com.example.amazing_note.databinding.FragmentListBinding
import com.example.amazing_note.ui.adapters.ListAdapter
import com.example.amazing_note.ui.utils.SwipeToDelete
import com.example.amazing_note.ui.viewmodels.NoteViewModel
import com.example.amazing_note.ui.viewmodels.SharedViewModel
import com.google.android.material.snackbar.Snackbar

class ListFragment : Fragment(), SearchView.OnQueryTextListener {
    private var _binding: FragmentListBinding? = null
    private val binding get() = _binding!!

    private val adapter: ListAdapter by lazy { ListAdapter() }
    private val mNoteViewModel: NoteViewModel by viewModels()
    private val mSharedViewModel: SharedViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // binding
        _binding = FragmentListBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = viewLifecycleOwner
        binding.mSharedViewModel = mSharedViewModel

        // set recyclerview
        setupRecyclerView()

        // observe livedata
        mNoteViewModel.noteList.observe(viewLifecycleOwner, { data ->
            adapter.setNotes(data)
        })

        binding.listLayout.setOnClickListener {
            findNavController().navigate(R.id.action_listFragment_to_updateFragment)
        }

        setHasOptionsMenu(true)

        return binding.root
    }

    private fun setupRecyclerView() {
        val recyclerView = binding.listfragRecyclerView
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(requireActivity())

        swipeToDelete(recyclerView)
    }


    override fun onQueryTextSubmit(query: String?): Boolean {
        if(query != null) {
            searchDatabase(query)
        }
        return true
    }

    override fun onQueryTextChange(query: String?): Boolean {
        if(query != null) {
            searchDatabase(query)
        }
        return true
    }

    private fun searchDatabase(query: String?) {
        val searchQuery = "%${query}%"
        mNoteViewModel.searchNote(searchQuery).observe(this, { list ->
            list?.let {
                adapter.setNotes(it)
            }
        })
    }

    private fun swipeToDelete(recyclerView: RecyclerView) {
        val swipeToDeleteCallBack = object: SwipeToDelete() {
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val noteDelete = adapter.noteList[viewHolder.adapterPosition]
                mNoteViewModel.deleteNote(noteDelete)
                adapter.notifyItemRemoved(viewHolder.adapterPosition)
                restoreDeletedNote(viewHolder.itemView, noteDelete, viewHolder.adapterPosition)
            }
        }
        val itemTouchHelper = ItemTouchHelper(swipeToDeleteCallBack)
        itemTouchHelper.attachToRecyclerView(recyclerView)
    }

    private fun restoreDeletedNote(view: View, deletedNote: Note, position: Int) {
        val snackBar = Snackbar.make(
            view, "Deleted '${deletedNote.title}'", Snackbar.LENGTH_SHORT
        )
        snackBar.setAction("Undo") {
            mNoteViewModel.insertNote(deletedNote)
            adapter.notifyItemChanged(position)
        }
        snackBar.show()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.list_fragment_menu, menu)

        val search = menu.findItem(R.id.menu_search)
        val searchView = search.actionView as? SearchView
        searchView?.isSubmitButtonEnabled = true
        searchView?.setOnQueryTextListener(this)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}