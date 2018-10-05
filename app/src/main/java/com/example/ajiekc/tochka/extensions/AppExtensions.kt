package com.example.ajiekc.tochka.extensions

import android.content.Context
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.Toast

fun Context.toast(message: String) = Toast.makeText(this, message, Toast.LENGTH_SHORT).show()

fun View.hide() {
    visibility = GONE
}

fun View.show() {
    visibility = VISIBLE
}