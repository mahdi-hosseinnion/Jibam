package com.example.jibi.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
@Entity(tableName = "records")
data class Record(

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "rId")
    val id: Int,
    @ColumnInfo(name = "money")
    val money: Int,
    @ColumnInfo(name = "memo")
    val memo: String?,
    @ColumnInfo(name = "cat_id")
    //category id exactly id
    val cat_id: Int,
    @ColumnInfo(name = "date")
    val date: Int
)