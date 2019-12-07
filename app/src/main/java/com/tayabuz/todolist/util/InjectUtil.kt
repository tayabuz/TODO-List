package com.tayabuz.todolist.util

import android.content.Context
import com.tayabuz.todolist.database.TODOItem
import com.tayabuz.todolist.database.TODOItemDatabase
import com.tayabuz.todolist.database.TODOItemRepository
import com.tayabuz.todolist.viewmodel.ItemTODOViewModelFactory
import com.tayabuz.todolist.viewmodel.TODOItemsViewModelFactory

object InjectUtil {
    private fun getTODOItemRepository(context: Context): TODOItemRepository {
        return TODOItemRepository.getInstance(
            TODOItemDatabase.getInstance(context.applicationContext)!!.todoItemDAO()
        )
    }

    fun provideTODOItemsViewModelFactory(context: Context, progress: TODOItem.ProgressOfItem): TODOItemsViewModelFactory {
        val repository = getTODOItemRepository(context)
        return TODOItemsViewModelFactory(repository, progress)
    }

    fun provideItemTODOViewModelFactory(context: Context, id: Long):ItemTODOViewModelFactory {
        val repository = getTODOItemRepository(context)
        return ItemTODOViewModelFactory(repository, id)
    }
}