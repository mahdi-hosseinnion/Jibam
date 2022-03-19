package com.ssmmhh.jibam.persistence.entities

import androidx.room.*

@Entity(
    tableName = "records",
    foreignKeys = [
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = arrayOf(CategoryEntity.COLUMN_ID),
            childColumns = arrayOf(TransactionEntity.COLUMN_CATEGORY_ID),
            onDelete = ForeignKey.CASCADE
            )
    ]
)
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "rId")
    val id: Int,
    @ColumnInfo(name = "money")
    //TODO ("use bigDecimal for data")
    val money: Double,
    @ColumnInfo(name = "memo")
    val memo: String?,
    //category id exactly id
    @ColumnInfo(name = COLUMN_CATEGORY_ID)
    val cat_id: Int,
    //int can handle the time till 1/19/2038, 6:44:07 AM in millisecond
    @ColumnInfo(name = "date")
    //TODO ("use long for data")
    val date: Int,
) {
    companion object {
        const val COLUMN_CATEGORY_ID = "cat_id"
    }
}