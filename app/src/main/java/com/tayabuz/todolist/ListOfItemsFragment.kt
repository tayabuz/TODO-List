package com.tayabuz.todolist

import android.app.AlertDialog
import android.os.Bundle
import android.view.*
import androidx.appcompat.widget.PopupMenu
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.observe
import androidx.navigation.Navigation
import com.tayabuz.todolist.adapter.CardStackAdapter
import com.tayabuz.todolist.database.TODOItem
import com.tayabuz.todolist.databinding.ListOfItemsFragmentBinding
import com.tayabuz.todolist.stacklayoutmanager.Config
import com.tayabuz.todolist.stacklayoutmanager.StackLayoutManager
import com.tayabuz.todolist.util.InjectUtil
import com.tayabuz.todolist.viewmodel.TODOItemsViewModel


class ListOfItemsFragment(private val progress: TODOItem.ProgressOfItem) : Fragment() {

    private lateinit var binding: ListOfItemsFragmentBinding

    private val viewModel: TODOItemsViewModel by viewModels {
        InjectUtil.provideTODOItemsViewModelFactory(requireContext(), progress)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = ListOfItemsFragmentBinding.inflate(inflater, container, false)
        binding.progress = this.progress
        binding.buttonItemAdd.setOnClickListener { Navigation.findNavController(this.view!!).navigate(R.id.action_startFragment_to_itemAddingFragment) }

        val cardAdapter = CardStackAdapter()
        cardAdapter.setMenuClickListener(MenuItemClick())

        binding.cardStackView.adapter = cardAdapter
        val config = Config()
        config.secondaryScale = 0.8f
        config.scaleRatio = 0.1f
        config.maxStackCount = 6
        config.initialStackCount = 6
        config.space = 30
        config.parallax = 2.5f
        config.align = StackLayoutManager.Align.TOP

        binding.cardStackView.layoutManager = StackLayoutManager(config)
        subscribeUi(cardAdapter, binding)

        return binding.root
    }

    private fun subscribeUi(adapter: CardStackAdapter, binding: ListOfItemsFragmentBinding) {
        viewModel.todoItems.observe(viewLifecycleOwner){
                result ->
            binding.hasItems =! result.isNullOrEmpty()
            adapter.submitList(result)
        }
    }


    inner class MenuItemClick: CardStackAdapter.MenuClickListener{
        override fun onMenuItemClick(todoItem: TODOItem, view: View) {
            when(progress){
                TODOItem.ProgressOfItem.ToDo -> {
                    val popup = PopupMenu(requireContext(), view)
                    val inflater: MenuInflater = popup.menuInflater
                    inflater.inflate(R.menu.item_menu_todo, popup.menu)
                    popup.setOnMenuItemClickListener { item ->
                        when (item.itemId) {
                            R.id.menu_delete_item_todo -> {
                                deleteItem(todoItem)
                                true
                            }
                            R.id.menu_move_to_inprogress_item_todo -> {
                                changeItemProgress(TODOItem.ProgressOfItem.InProcess, todoItem)
                                true
                            }
                            R.id.menu_move_to_done_item_todo -> {
                                changeItemProgress(TODOItem.ProgressOfItem.Done, todoItem)
                                true
                            }
                            else -> false
                        }
                    }
                    popup.show()
                }
                TODOItem.ProgressOfItem.InProcess -> {
                    val popup = PopupMenu(requireContext(), view)
                    val inflater: MenuInflater = popup.menuInflater
                    inflater.inflate(R.menu.item_menu_inprogress, popup.menu)
                    popup.setOnMenuItemClickListener { item ->
                        when (item.itemId) {
                            R.id.menu_delete_item_inprogress -> {
                                deleteItem(todoItem)
                                true
                            }
                            R.id.menu_move_to_todo_item_inprogess -> {
                                changeItemProgress(TODOItem.ProgressOfItem.ToDo, todoItem)
                                true
                            }
                            R.id.menu_move_to_done_item_inprogress -> {
                                changeItemProgress(TODOItem.ProgressOfItem.Done, todoItem)
                                true
                            }
                            else -> false
                        }
                    }
                    popup.show()
                }
                TODOItem.ProgressOfItem.Done -> {
                    val popup = PopupMenu(requireContext(), view)
                    val inflater: MenuInflater = popup.menuInflater
                    inflater.inflate(R.menu.item_menu_done, popup.menu)
                    popup.setOnMenuItemClickListener { item ->
                        when (item.itemId) {
                            R.id.menu_delete_item_done -> {
                                deleteItem(todoItem)
                                true
                            }
                            R.id.menu_move_to_todo_item_done -> {
                                changeItemProgress(TODOItem.ProgressOfItem.ToDo, todoItem)
                                true
                            }
                            R.id.menu_move_to_inprogress_item_done -> {
                                changeItemProgress(TODOItem.ProgressOfItem.InProcess, todoItem)
                                true
                            }
                            else -> false
                        }
                    }
                    popup.show()
                }
            }
        }

        private fun changeItemProgress(progress: TODOItem.ProgressOfItem, todoItem: TODOItem){
            todoItem.progress = progress
            viewModel.insert(todoItem)
        }

        private fun deleteItem(todoItem: TODOItem){
            val alertDialog = AlertDialog.Builder(view!!.context)
            alertDialog.setMessage(getString(R.string.dialog_delete_item_header))
            alertDialog.setCancelable(true)

            alertDialog.setPositiveButton(getString(R.string.dialog_delete_item_ok))
            { _, _ -> viewModel.delete(todoItem) }

            alertDialog.setNegativeButton(getString(R.string.dialog_delete_item_cancel))
            { dialog, _ -> dialog.cancel()}

            alertDialog.show()
        }
    }
}