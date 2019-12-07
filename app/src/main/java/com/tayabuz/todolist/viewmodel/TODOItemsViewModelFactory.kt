package com.tayabuz.todolist.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.tayabuz.todolist.database.TODOItem
import com.tayabuz.todolist.database.TODOItemRepository

class TODOItemsViewModelFactory(private val repository: TODOItemRepository, private val progress: TODOItem.ProgressOfItem):ViewModelProvider.NewInstanceFactory() {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return TODOItemsViewModel(repository, progress) as T
    }
}