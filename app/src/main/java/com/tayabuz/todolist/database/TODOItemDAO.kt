package com.tayabuz.todolist.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy.REPLACE
import androidx.room.Query


@Dao
interface TODOItemDAO {
    @Query("SELECT * from TODOItems")
    fun getAll(): LiveData<List<TODOItem>>

    @Query("SELECT * from TODOItems WHERE progress = :Progress")
    fun getItemsByProgress(Progress: TODOItem.ProgressOfItem): LiveData<List<TODOItem>>

    @Query("SELECT * FROM TODOItems WHERE id = :Id")
    fun getItem(Id: Long):LiveData<TODOItem>

    @Insert(onConflict = REPLACE)
    suspend fun insert(item: TODOItem)

    @Delete
    suspend fun delete(item: TODOItem)
}