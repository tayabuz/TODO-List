package com.tayabuz.todolist.database


class TODOItemRepository private constructor(private val todoItemDao: TODOItemDAO) {

    suspend fun insert(item: TODOItem) {
        todoItemDao.insert(item)
    }

    suspend fun delete(item: TODOItem) {
        todoItemDao.delete(item)
    }

    fun getItemsByProgress(progress: TODOItem.ProgressOfItem) = todoItemDao.getItemsByProgress(progress)

    fun getItem(id: Long) = todoItemDao.getItem(id)

    fun getAll() = todoItemDao.getAll()

    companion object {
        @Volatile private var instance: TODOItemRepository ? = null

        fun getInstance(todoItemDao: TODOItemDAO) = instance ?: synchronized(this) {
            instance ?: TODOItemRepository(todoItemDao).also { instance = it }
        }
    }
}
