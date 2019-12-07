package com.tayabuz.todolist.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.tayabuz.todolist.database.TODOItemRepository

class ItemTODOViewModelFactory(private val repository: TODOItemRepository, private val id: Long):
    ViewModelProvider.NewInstanceFactory() {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return ItemTODOViewModel(repository, id) as T
    }
}
