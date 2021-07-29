package com.example.jibi.models

import androidx.room.ColumnInfo
import androidx.room.PrimaryKey

data class Transaction(
    @ColumnInfo(name = "rId")
    val id: Int,
    @ColumnInfo(name = "money")
    val money: Double,
    @ColumnInfo(name = "memo")
    val memo: String?,
    @ColumnInfo(name = "cat_id")
    //category id exactly id
    val cat_id: Int,
    @ColumnInfo(name = "date")
    //int can handle the time till 1/19/2038, 6:44:07 AM in millisecond
    val date: Int,
    val categoryImage: String,
    val percentage: Double
)
