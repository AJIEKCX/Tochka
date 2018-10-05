package com.example.ajiekc.tochka.api.vk

import com.google.gson.annotations.SerializedName

data class VKUserResponse(@SerializedName("response") val response: List<Response>?) {

    data class Response(@SerializedName("id") val id: Int?,
                        @SerializedName("first_name") val firstName: String?,
                        @SerializedName("last_name") val lastName: String?,
                        @SerializedName("photo_max_orig") val photoMaxOrig: String?
    )
}