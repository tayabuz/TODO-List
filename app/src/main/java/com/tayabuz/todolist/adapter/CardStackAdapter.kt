package com.tayabuz.todolist.adapter


import android.view.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.PopupMenu
import androidx.databinding.DataBindingUtil
import androidx.navigation.findNavController
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.tayabuz.todolist.R
import com.tayabuz.todolist.StartFragmentDirections
import com.tayabuz.todolist.database.TODOItem
import com.tayabuz.todolist.databinding.ItemBinding
import com.tayabuz.todolist.viewmodel.TODOItemsViewModel


class CardStackAdapter: ListAdapter<TODOItem, CardStackAdapter.ViewHolder>(TODOItemDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(DataBindingUtil.inflate(LayoutInflater.from(parent.context), R.layout.item, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    private var menuClickListener: MenuClickListener ?= null

    inner class ViewHolder(
        private val binding: ItemBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.setToDetailPage { view ->
                binding.item?.let { item -> navigateToDetail(item, view)  }
            }

            binding.setShowMenu { view ->
                menuClickListener!!.onMenuItemClick(binding.item!!, view)
            }
        }


        private fun navigateToDetail(item: TODOItem, view: View) {
            val direction = StartFragmentDirections.actionStartFragmentToItemDetailFragment(item.id)
            view.findNavController().navigate(direction)
        }

        fun bind(item: TODOItem) {
            binding.apply{
                this.item = item
                executePendingBindings()
            }
        }
    }

    fun setMenuClickListener(menuClickListener: MenuClickListener) {
        this.menuClickListener = menuClickListener
    }

    interface MenuClickListener {
        fun onMenuItemClick(todoItem: TODOItem, view: View)
    }
}

private class TODOItemDiffCallback: DiffUtil.ItemCallback<TODOItem>(){
    override fun areItemsTheSame(oldItem: TODOItem, newItem: TODOItem): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: TODOItem, newItem: TODOItem): Boolean {
        return oldItem == newItem
    }
}