package com.tayabuz.todolist.database

import android.content.Context
import androidx.room.*


@Database(entities = [TODOItem::class], version = 1, exportSchema = false)
@TypeConverters(Converter::class)
abstract class TODOItemDatabase: RoomDatabase() {
    abstract fun todoItemDAO(): TODOItemDAO

    companion object {
        private var INSTANCE: TODOItemDatabase? = null

        fun getInstance(context: Context): TODOItemDatabase? {
            if (INSTANCE == null) {
                synchronized(TODOItemDatabase::class) {
                    INSTANCE = Room.databaseBuilder(context.applicationContext,
                        TODOItemDatabase::class.java, "ToDoItems.db").allowMainThreadQueries().build()
                }
            }
            return INSTANCE
        }
    }
}

class Converter {

    @TypeConverter
    fun progressOfItemToTnt(value: TODOItem.ProgressOfItem?): Int? = value?.ordinal

    @TypeConverter
    fun intToProgressOfItem(value: Int?): TODOItem.ProgressOfItem? = TODOItem.ProgressOfItem.values()[value!!]
}