package com.example.ajiekc.tochka.api.fb

import com.example.ajiekc.tochka.BuildConfig
import com.example.ajiekc.tochka.Injection
import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.Query

interface FBService {

    @GET("${BuildConfig.FB_ENDPOINT}me")
    fun me(@Query("fields") fields: String,
           @Query("access_token") accessToken: String): Single<FBMeResponse>

    companion object {
        val instance: FBService by lazy {
            val retrofit = Injection.provideRetrofit()
            retrofit.create(FBService::class.java)
        }
    }
}