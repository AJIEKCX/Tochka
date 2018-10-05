package com.example.ajiekc.tochka.api.fb

import com.google.gson.annotations.SerializedName

data class FBMeResponse(@SerializedName("first_name") val firstName: String?,
                        @SerializedName("last_name") val lastName: String?,
                        @SerializedName("picture") val picture: Picture?) {

    data class Picture(@SerializedName("data") val data: Data?) {

        data class Data(@SerializedName("url") val url: String?)
    }
}
