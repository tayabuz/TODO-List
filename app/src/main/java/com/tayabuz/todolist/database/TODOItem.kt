package com.tayabuz.todolist.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "TODOItems")
data class TODOItem(val Header: String, val Explanation: String, var progress: ProgressOfItem = ProgressOfItem.ToDo) {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    var id: Long = 0

    enum class ProgressOfItem {ToDo, InProcess, Done}
}