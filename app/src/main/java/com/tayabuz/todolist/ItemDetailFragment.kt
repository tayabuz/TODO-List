package com.tayabuz.todolist

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import com.tayabuz.todolist.databinding.ItemDetailFragmentBinding
import com.tayabuz.todolist.util.InjectUtil
import com.tayabuz.todolist.viewmodel.ItemTODOViewModel


class ItemDetailFragment:Fragment() {

    private val args: ItemDetailFragmentArgs by navArgs()

    private val itemDetailViewModel: ItemTODOViewModel by viewModels {
        InjectUtil.provideItemTODOViewModelFactory(requireContext(), args.Id)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val binding = DataBindingUtil.inflate<ItemDetailFragmentBinding>(inflater, R.layout.item_detail_fragment, container, false).apply {
            viewModel = itemDetailViewModel
            lifecycleOwner = viewLifecycleOwner
        }
        return binding.root
    }
}