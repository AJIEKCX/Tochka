package com.example.ajiekc.tochka.repository

import com.example.ajiekc.tochka.db.User
import io.reactivex.Single

interface IGithubRepository {
    fun getUsers(since: Int = 0): Single<List<User>>

    fun getUsersWithFilter(filter: String?): Single<List<User>>
}