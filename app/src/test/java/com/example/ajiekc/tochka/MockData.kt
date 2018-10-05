package com.example.ajiekc.tochka

import com.example.ajiekc.tochka.db.User

fun singleUserList() = arrayListOf(User(1, "Alex"))

fun emptyUserList() = arrayListOf<User>()