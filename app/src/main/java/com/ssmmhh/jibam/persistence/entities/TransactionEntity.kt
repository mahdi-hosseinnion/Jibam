package com.ssmmhh.jibam.persistence.entities

import androidx.room.*

@Entity(
    tableName = "transactions",
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
    @ColumnInfo(name = "id")
    val id: Int,
    @ColumnInfo(name = "money")
    //TODO ("use bigDecimal for data")
    val money: Double,
    @ColumnInfo(name = "memo")
    val memo: String?,
    //category id exactly id
    @ColumnInfo(name = COLUMN_CATEGORY_ID)
    val cat_id: Int,
    @ColumnInfo(name = "date")
    val date: Long,
) {
    companion object {
        const val COLUMN_CATEGORY_ID = "categoryId"
    }
}