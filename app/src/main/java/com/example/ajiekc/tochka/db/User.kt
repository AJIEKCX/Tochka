package com.example.ajiekc.tochka.db

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.Ignore
import android.arch.persistence.room.PrimaryKey
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

@Entity(tableName = "users_table")
data class User(@PrimaryKey
                @ColumnInfo(name = "id")
                @Expose
                @SerializedName("id") var id: Int? = null,
                @ColumnInfo(name = "login")
                @Expose
                @SerializedName("login") var login: String? = null,
                @ColumnInfo(name = "avatar_url")
                @Expose
                @SerializedName("avatar_url") var avatarUrl: String? = null,
                @Ignore @ColumnInfo(name = "type") var type: String? = null) {
    constructor() : this(0, null, null, null)
}