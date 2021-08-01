package com.example.jibi.models

import android.graphics.Bitmap
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey

@Entity(tableName = "records")

data class TransactionEntity(

    @PrimaryKey(autoGenerate = true)
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
    //handle the income for repository
    @Ignore val incomeSum: Double?
){
    constructor(id: Int, money: Double, memo: String?,cat_id: Int,date: Int) : this(id,money,memo,cat_id,date,null)
}