package com.example.ajiekc.tochka.db

import android.arch.persistence.room.Database
import android.arch.persistence.room.Room
import android.arch.persistence.room.RoomDatabase
import android.content.Context
import com.example.ajiekc.tochka.SingletonHolder

@Database(entities = arrayOf(User::class), version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao

    companion object : SingletonHolder<AppDatabase, Context>({
        Room.databaseBuilder(
                it.applicationContext,
                AppDatabase::class.java,
                "app_database")
                .build()
    })
}