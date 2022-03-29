package com.ssmmhh.jibam.data.source.local.dao

import androidx.room.*
import com.ssmmhh.jibam.data.source.local.dto.ChartDataDto
import com.ssmmhh.jibam.data.source.local.dto.TransactionDto
import com.ssmmhh.jibam.data.source.local.entity.TransactionEntity
import com.ssmmhh.jibam.data.source.local.typeconverter.BigDecimalTypeConverter
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
     * @param [transactionEntity], The transaction to be inserted.
     * @return the new rowId for the inserted item.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transactionEntity: TransactionEntity): Long

    /**
     * Update a transaction.
     *
     * @param [transactionEntity], The transaction to be updated.
     * @return The number of row updated successfully.
     */
    @Update
    suspend fun updateTransaction(transactionEntity: TransactionEntity): Int

    /**
     * Delete a transaction.
     *
     * @param [transactionEntity], The transaction or transactions to be deleted.
     * @return The number of row deleted successfully.
     */
    @Delete
    suspend fun deleteTransaction(vararg transactionEntity: TransactionEntity): Int

    /**
     * Delete a transaction with id.
     *
     * @param [id], The id of transaction to be deleted.
     * @return The number of row deleted successfully.
     */
    @Query("DELETE FROM transactions WHERE id = :id")
    suspend fun deleteTransaction(id: Int): Int

    /**
     * Get transaction with id.
     * Left join on categories and categoryImages table by transaction's categoryId and category's
     * id.
     *
     * @param [id], The id of transaction
     * @return Transaction with id or null if there is not any transaction with id.
     */
    @Query(
        value =
        """
            SELECT transactions.*, 
            categories.name as categoryName, 
            categories.id as categoryId, 
            categoryImages.resName as categoryImage, 
            categoryImages.backgroundColor as categoryImageColor 
            FROM transactions 
            LEFT JOIN categories ON transactions.categoryId = categories.id 
            LEFT JOIN categoryImages ON categories.imageId = categoryImages.id 
            WHERE transactions.id = :id 
        """
    )
    suspend fun getTransactionById(id: Int): TransactionDto?

    /**
     * Observe all of transactions with date between [fromDate] and [toDate] and money or memo like
     * [query], order by date descending.
     * Left join on categories and categoryImages table by transaction's categoryId and category's
     * id.
     *
     * @param [fromDate], The min date in unix timestamp.
     * @param [toDate], The max date in unix timestamp.
     * @param [query], The query that will be matched with money or memo
     * @return transactions with date between [fromDate] and [toDate] and money or memo like [query].
     */
    @Query(
        value =
        """
            SELECT transactions.*, 
            categories.name as categoryName, 
            categoryImages.resName as categoryImage, 
            categoryImages.backgroundColor as categoryImageColor 
            FROM transactions 
            LEFT JOIN categories ON transactions.categoryId = categories.id 
            LEFT JOIN categoryImages ON categories.imageId = categoryImages.id 
            WHERE date BETWEEN :fromDate AND :toDate 
            AND 
            ( 
            money LIKE '%' || :query || '%' 
            OR 
            memo LIKE '%' || :query || '%' 
            ) 
            ORDER BY date DESC 
        """
    )
    fun observeAllOfTransactionsBetweenDates(
        fromDate: Long,
        toDate: Long,
        query: String
    ): Flow<List<TransactionDto>>

    /**
     *  Observe the sum of expense transaction's money with date between [fromDate] and [toDate].
     *
     *  @param [fromDate], The min date in unix timestamp.
     *  @param [toDate], The max date in unix timestamp.
     *  @return The sum of expense transaction's money with date between [fromDate] and [toDate].
     */
    @Query(
        value =
        """
        SELECT SUM(money) 
        FROM transactions 
        WHERE (date BETWEEN :fromDate AND :toDate) 
        AND (money < 0) 
        """
    )
    fun observeSumOfExpensesBetweenDates(fromDate: Long, toDate: Long): Flow<BigDecimal>

    /**
     *  Observe the sum of income transaction's money with date between [fromDate] and [toDate].
     *
     *  @param [fromDate], The min date in unix timestamp.
     *  @param [toDate], The max date in unix timestamp.
     *  @return The sum of income transaction's money with date between [fromDate] and [toDate].
     */
    @Query(
        value =
        """
        SELECT SUM(money) 
        FROM transactions 
        WHERE (date BETWEEN :fromDate AND :toDate) 
        AND (money > 0) 
        """
    )
    fun observeSumOfIncomesBetweenDates(fromDate: Long, toDate: Long): Flow<BigDecimal>

    /**
     * Observe all of transaction with [categoryId] and date between [fromDate] and [toDate], order
     * by order by absolute value of money descending.
     * Left join on categories and categoryImages table by transaction's categoryId and category's
     * id
     *
     * @param [categoryId], The category id.
     * @param [fromDate], The min date in unix timestamp.
     * @param [toDate], The max date in unix timestamp.
     * @return all of transaction with [categoryId] and date between [fromDate] and [toDate].
     */
    @Query(
        value =
        """
            SELECT transactions.*, 
            categories.name as categoryName, 
            categoryImages.resName as categoryImage, 
            categoryImages.backgroundColor as categoryImageColor 
            FROM transactions 
            LEFT JOIN categories ON transactions.categoryId = categories.id 
            LEFT JOIN categoryImages ON categories.imageId = categoryImages.id 
            WHERE categoryId = :categoryId 
            AND date BETWEEN :fromDate AND :toDate 
            ORDER BY ABS(money) DESC 
        """
    )
    fun observeAllOfTransactionsWithCategoryId(
        categoryId: Int,
        fromDate: Long,
        toDate: Long
    ): Flow<List<TransactionDto>>

    /**
     * Get list of categories with sum of corresponding transaction's money, order by absolute
     * value of sum of moneys, with date between [fromDate] and [toDate].
     * Left join on categories and categoryImages table by transaction's categoryId and category's
     * id
     *
     * @param [fromDate], The min date in unix timestamp.
     * @param [toDate], The max date in unix timestamp.
     * @return a list of [ChartDataDto] with sum of transaction's money.
     */
    @Query(
        value =
        """
            SELECT SUM(money) as sumOfMoney,
            categories.id as categoryId, 
            categories.name as categoryName, 
            categories.type as categoryType, 
            categoryImages.resName as categoryImageRes, 
            categoryImages.backgroundColor as categoryImageBackgroundColor 
            FROM transactions 
            LEFT JOIN categories ON transactions.categoryId = categories.id 
            LEFT JOIN categoryImages ON categories.imageId = categoryImages.id 
            WHERE date BETWEEN :fromDate AND :toDate 
            GROUP BY categoryId 
            ORDER BY ABS(SUM(money)) DESC 
            """
    )
    suspend fun getSumOfEachCategoryMoney(
        fromDate: Long,
        toDate: Long
    ): List<ChartDataDto>
}
