package com.example.ajiekc.tochka.api.vk

import com.example.ajiekc.tochka.BuildConfig
import com.example.ajiekc.tochka.Injection
import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.Query

interface VKService {

    @GET("${BuildConfig.VK_ENDPOINT}users.get")
    fun getUser(@Query("user_id") userId: String,
                @Query("fields") fields: String,
                @Query("v") version: String,
                @Query("access_token") accessToken: String): Single<VKUserResponse>

    companion object {
        val instance: VKService by lazy {
            val retrofit = Injection.provideRetrofit()
            retrofit.create(VKService::class.java)
        }
    }
}