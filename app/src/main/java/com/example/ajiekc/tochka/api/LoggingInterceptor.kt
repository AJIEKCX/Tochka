package com.example.ajiekc.tochka.api

import com.example.ajiekc.tochka.BuildConfig
import okhttp3.Interceptor
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import okhttp3.logging.HttpLoggingInterceptor.Level.BODY
import okhttp3.logging.HttpLoggingInterceptor.Level.NONE

class LoggingInterceptor : Interceptor {

    private val interceptor: Interceptor

    init {
        interceptor = HttpLoggingInterceptor()
                .setLevel(if (BuildConfig.DEBUG) BODY else NONE)
    }

    override fun intercept(chain: Interceptor.Chain): Response = interceptor.intercept(chain)
}