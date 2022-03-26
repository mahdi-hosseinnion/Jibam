package com.ssmmhh.jibam.persistence.entities

import androidx.room.*
import com.ssmmhh.jibam.persistence.BigDecimalTypeConverter
import java.math.BigDecimal

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
@TypeConverters(BigDecimalTypeConverter::class)
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Int,
    @ColumnInfo(name = "money")
    val money: BigDecimal,
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