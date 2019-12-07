package com.tayabuz.todolist.viewmodel

import androidx.lifecycle.ViewModel
import com.tayabuz.todolist.database.TODOItemRepository


class ItemTODOViewModel(todoItemRepository: TODOItemRepository, private val id: Long) : ViewModel() {

    val todoItem = todoItemRepository.getItem(id)
}