package com.example.jibi.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "categories")
data class Category(

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "cId")
    val id: Int,
    /*
    1 => expenses
    2 => income
     */
    @ColumnInfo(name = "type")
    val type: Int,
    @ColumnInfo(name = "Category_Name")
    val name: String,
    @ColumnInfo(name = "img_res")
    val img_res: String,
    @ColumnInfo(name = "cat_order")
    val cat_order: Int
)