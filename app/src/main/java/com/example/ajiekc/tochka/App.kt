package com.example.ajiekc.tochka

import android.app.Application
import com.example.ajiekc.tochka.db.AppDatabase
import com.vk.sdk.VKSdk

class App : Application() {

    override fun onCreate() {
        super.onCreate()
        VKSdk.initialize(this)
    }
}