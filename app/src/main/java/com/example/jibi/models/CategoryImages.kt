package com.example.jibi.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey


@Entity(tableName = "category_images")
data class CategoryImages(

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Int,
    @ColumnInfo(name = "image_res")
    val image_res: String,
    @ColumnInfo(name = "group_name")
    val group_name: String
)