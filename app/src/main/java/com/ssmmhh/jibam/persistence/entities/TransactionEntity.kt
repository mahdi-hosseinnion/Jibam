package com.ssmmhh.jibam.persistence.entities

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
    //category id exactly id
    @ColumnInfo(name = "cat_id")
    val cat_id: Int,
    //int can handle the time till 1/19/2038, 6:44:07 AM in millisecond
    @ColumnInfo(name = "date")
    val date: Int,
)