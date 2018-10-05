package com.example.ajiekc.tochka.db

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Insert
import android.arch.persistence.room.OnConflictStrategy
import android.arch.persistence.room.Query
import io.reactivex.Single

@Dao
interface UserDao {

    @Query("SELECT * FROM users_table")
    fun getAllUsers(): Single<List<User>>

    @Query("SELECT * FROM users_table WHERE id > :since ORDER BY id ASC LIMIT 30")
    fun getUsers(since: Int): Single<List<User>>

    @Query("SELECT * FROM users_table WHERE login LIKE :filter || '%' ORDER BY id ASC LIMIT 30")
    fun getUsersWithFilter(filter: String?): Single<List<User>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(word: User)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(word: List<User>)

    @Query("DELETE FROM users_table")
    fun deleteAll()
}