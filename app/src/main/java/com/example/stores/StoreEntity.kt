package com.example.stores

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "StoreEntity")
data class StoreEntity(@PrimaryKey(autoGenerate = true)
                       var id : Long = 0, var name : String,
                       var phone: String,
                       var website: String = "",
                       val photoUrl: String,
                       var isFavourite: Boolean = false)
