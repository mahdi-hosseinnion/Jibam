package com.ssmmhh.jibam.persistence

import androidx.room.*
import com.ssmmhh.jibam.persistence.dtos.ChartDataDto
import com.ssmmhh.jibam.persistence.dtos.TransactionDto
import com.ssmmhh.jibam.persistence.entities.TransactionEntity
import kotlinx.coroutines.flow.Flow
import java.math.BigDecimal

@Dao
@TypeConverters(BigDecimalTypeConverter::class)
interface TransactionsDao {


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrReplace(transactionEntity: TransactionEntity): Long

    @Query("SELECT * FROM transactions WHERE id = :id")
    suspend fun getRecordById(id: Int): TransactionEntity

    @Query(
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
     * To delete one or more transaction.
     * Returns number of rows deleted. 0 if no row deleted.
     */
    @Update
    suspend fun updateRecord(transactionEntity: TransactionEntity): Int

    @Delete
    suspend fun deleteRecord(vararg transactionEntity: TransactionEntity): Int

    @Query("DELETE FROM transactions WHERE id = :id")
    suspend fun deleteRecord(id: Int): Int

    /*
        get transactions queries
     */
    @Query(
        """
                SELECT transactions.*, 
                categories.name as category_name, 
                categoryImages.resName as category_image, 
                categoryImages.backgroundColor as category_image_color 
                FROM transactions 
                LEFT JOIN categories ON transactions.categoryId = categories.id 
                LEFT JOIN categoryImages ON categories.imageId = categoryImages.id 
                WHERE 
                ( 
                money LIKE '%' || :query || '%' 
                OR memo LIKE '%' || :query || '%' 
                ) 
                $ORDER_BY_DATE
                """
    )
    fun getAllRecords(query: String): Flow<List<TransactionDto>>

    //fromDate and toDate count in the result >=
    @Query(
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
                OR memo LIKE '%' || :query || '%' 
                ) 
                $ORDER_BY_DATE
                """

    )
    fun loadAllRecordsBetweenDates(
        minDate: Int,
        maxDate: Int,
        query: String
    ): Flow<List<TransactionDto>>

    //TODO CHANGE DATE > MIN DATE TO DATE >=MINDATE
    @Query(
        """
                SELECT transactions.*, 
                categories.name as category_name, 
                categoryImages.resName as category_image, 
                categoryImages.backgroundColor as category_image_color 
                FROM transactions 
                LEFT JOIN categories ON transactions.categoryId = categories.id 
                LEFT JOIN categoryImages ON categories.imageId = categoryImages.id 
                WHERE date > :minDate 
                AND 
                ( 
                money LIKE '%' || :query || '%' 
                OR memo LIKE '%' || :query || '%' 
                ) 
                $ORDER_BY_DATE
                """
    )
    fun loadAllRecordsAfterThan(
        minDate: Int, query: String
    ): Flow<List<TransactionDto>>

    @Query(
        """
                SELECT transactions.*, 
                categories.name as category_name, 
                categoryImages.resName as category_image, 
                categoryImages.backgroundColor as category_image_color 
                FROM transactions 
                LEFT JOIN categories ON transactions.categoryId = categories.id 
                LEFT JOIN categoryImages ON categories.imageId = categoryImages.id 
                WHERE date < :maxDate 
                AND 
                ( 
                money LIKE '%' || :query || '%' 
                OR memo LIKE '%' || :query || '%' 
                )
                $ORDER_BY_DATE
                """
    )
    fun loadAllRecordsBeforeThan(
        maxDate: Int, query: String
    ): Flow<List<TransactionDto>>

    /*
        sum queries
     */

    //return all

    @Query("SELECT money FROM transactions ")
    fun getListOfAllOfMoney(): Flow<List<BigDecimal>>

    //between dates
    @Query("SELECT money FROM transactions WHERE (date BETWEEN :minDate AND :maxDate)")
    fun getListOfMoneyBetweenDates(minDate: Int, maxDate: Int): Flow<List<BigDecimal>>

    //after than
    @Query("SELECT money FROM transactions WHERE (date > :minDate)")
    fun getListOfMoneyAfterThan(minDate: Int): Flow<List<BigDecimal>>

    //before than
    @Query("SELECT money FROM transactions WHERE (date < :maxDate)")
    fun getListOfMoneyBeforeThan(maxDate: Int): Flow<List<BigDecimal>>

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
    //TODO ADD ORDER TO IT(ORDER BY ABS(SUM(money)) DESC)
    suspend fun sumOfMoneyGroupByCategory(
        fromDate: Int,
        toDate: Int
    ): List<ChartDataDto>

    @Query(
        """SELECT transactions.*,  
            categories.name as category_name, 
            categoryImages.resName as category_image, 
            categoryImages.backgroundColor as category_image_color 
            FROM transactions 
            LEFT JOIN categories ON transactions.categoryId = categories.id 
            LEFT JOIN categoryImages ON categories.imageId = categoryImages.id 
            WHERE categoryId = :categoryId 
            AND 
            date BETWEEN :fromDate AND :toDate 
            ORDER BY ABS(money) DESC"""
    )
    fun getAllTransactionByCategoryId(
        categoryId: Int,
        fromDate: Int,
        toDate: Int
    ): Flow<List<TransactionDto>>

    companion object {
        const val ORDER_BY_DATE = "ORDER BY date DESC"
    }
}

fun TransactionsDao.getRecords(
    minDate: Int? = null,
    maxDate: Int? = null,
    query: String = ""
): Flow<List<TransactionDto>> {
    if (minDate != null && maxDate != null) {
        return loadAllRecordsBetweenDates(minDate, maxDate, query)
    }
    if (minDate == null && maxDate != null) {
        return loadAllRecordsBeforeThan(maxDate, query)
    }
    if (maxDate == null && minDate != null) {
        return loadAllRecordsAfterThan(minDate, query)
    }
    return getAllRecords(query)
}

fun TransactionsDao.getListOfMoney(
    minDate: Int? = null,
    maxDate: Int? = null
): Flow<List<BigDecimal>> {
    if (minDate != null && maxDate != null) {
        return getListOfMoneyBetweenDates(minDate, maxDate)
    }
    if (minDate == null && maxDate != null) {
        return getListOfMoneyBeforeThan(maxDate)
    }
    if (maxDate == null && minDate != null) {
        return getListOfMoneyAfterThan(minDate)
    }
    return getListOfAllOfMoney()
}
