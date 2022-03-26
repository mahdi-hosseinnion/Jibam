package com.ssmmhh.jibam.persistence.daos

import androidx.room.*
import com.ssmmhh.jibam.persistence.dtos.ChartDataDto
import com.ssmmhh.jibam.persistence.dtos.TransactionDto
import com.ssmmhh.jibam.persistence.entities.TransactionEntity
import com.ssmmhh.jibam.persistence.typeconverters.BigDecimalTypeConverter
import kotlinx.coroutines.flow.Flow
import java.math.BigDecimal

/**
 * Data access object for transaction table.
 */
@Dao
@TypeConverters(BigDecimalTypeConverter::class)
interface TransactionDao {

    /**
     * Insert new transaction or replace existence one if transaction already exist in database.
     *
     * @return the new rowId for the inserted item.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transactionEntity: TransactionEntity): Long

    /**
     * Update a transaction.
     *
     * @return The number of row updated successfully.
     */
    @Update
    suspend fun updateTransaction(transactionEntity: TransactionEntity): Int

    /**
     * Delete a transaction.
     *
     * @return The number of row deleted successfully.
     */
    @Delete
    suspend fun deleteTransaction(vararg transactionEntity: TransactionEntity): Int

    /**
     * Delete a transaction with id.
     *
     * @return The number of row deleted successfully.
     */
    @Query("DELETE FROM transactions WHERE id = :id")
    suspend fun deleteTransaction(id: Int): Int

    /**
     * Get transaction with id.
     * Left join on categories and categoryImages table.
     *
     * @return transaction with id or null if there is not any transaction with id.
     */
    @Query(
        value =
        """
            SELECT transactions.*, 
            categories.name as category_name, 
            categories.id as categoryId, 
            categoryImages.resName as category_image, 
            categoryImages.backgroundColor as category_image_color 
            FROM transactions 
            LEFT JOIN categories ON transactions.categoryId = categories.id 
            LEFT JOIN categoryImages ON categories.imageId = categoryImages.id 
            WHERE transactions.id = :id
        """
    )
    suspend fun getTransactionById(id: Int): TransactionDto?

    /**
     * Observe all of transactions with date between [minDate] and [maxDate] and money or memo like
     * [query], order by date descending.
     * Left join on categories and categoryImages table.
     *
     * @return transactions with date between [minDate] and [maxDate] and money or memo like [query].
     */
    @Query(
        value =
        """
            SELECT transactions.*, 
            categories.name as category_name, 
            categoryImages.resName as category_image, 
            categoryImages.backgroundColor as category_image_color 
            FROM transactions 
            LEFT JOIN categories ON transactions.categoryId = categories.id 
            LEFT JOIN categoryImages ON categories.imageId = categoryImages.id 
            WHERE date BETWEEN :minDate AND :maxDate 
            AND 
            ( 
            money LIKE '%' || :query || '%' 
            OR 
            memo LIKE '%' || :query || '%' 
            ) 
            ORDER BY date DESC 
        """
    )
    fun getAllOfTransactionsBetweenDates(
        minDate: Int,
        maxDate: Int,
        query: String
    ): Flow<List<TransactionDto>>

    /**
     *  Observe a list of transaction.money with date between [minDate] and [maxDate].
     *
     *  @return a list of transaction.money with date between [minDate] and [maxDate].
     */
    @Query("SELECT money FROM transactions WHERE (date BETWEEN :minDate AND :maxDate)")
    fun getListOfMoneyBetweenDates(minDate: Int, maxDate: Int): Flow<List<BigDecimal>>


    @Query(
        value =
        """
            SELECT transactions.*, 
            categories.name as category_name, 
            categoryImages.resName as category_image, 
            categoryImages.backgroundColor as category_image_color 
            FROM transactions 
            LEFT JOIN categories ON transactions.categoryId = categories.id 
            LEFT JOIN categoryImages ON categories.imageId = categoryImages.id 
            WHERE categoryId = :categoryId 
            AND 
            date BETWEEN :fromDate AND :toDate 
            ORDER BY ABS(money) DESC 
        """
    )
    fun getAllTransactionByCategoryId(
        categoryId: Int,
        fromDate: Int,
        toDate: Int
    ): Flow<List<TransactionDto>>


    @Query(
        """SELECT money as money, 
            categories.id as categoryId, 
            categories.name as category_name, 
            categories.type as categoryType, 
            categoryImages.resName as category_image_res, 
            categoryImages.backgroundColor as category_image_background_color 
            FROM transactions 
            LEFT JOIN categories ON transactions.categoryId = categories.id 
            LEFT JOIN categoryImages ON categories.imageId = categoryImages.id 
            WHERE date BETWEEN :fromDate AND :toDate 
            """
    )
    suspend fun sumOfMoneyGroupByCategory(
        fromDate: Int,
        toDate: Int
    ): List<ChartDataDto>
}
