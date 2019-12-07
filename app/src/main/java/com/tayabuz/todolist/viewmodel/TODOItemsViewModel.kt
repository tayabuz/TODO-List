package com.tayabuz.todolist.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tayabuz.todolist.database.TODOItem
import com.tayabuz.todolist.database.TODOItemRepository
import kotlinx.coroutines.launch

class TODOItemsViewModel internal constructor(private val todoItemRepository: TODOItemRepository, progress: TODOItem.ProgressOfItem): ViewModel() {

    var todoItems: LiveData<List<TODOItem>> = todoItemRepository.getItemsByProgress(progress)

    fun insert(item: TODOItem)
    {
        viewModelScope.launch { todoItemRepository.insert(item) }
    }

    fun delete(item: TODOItem){
        viewModelScope.launch { todoItemRepository.delete(item) }
    }
}