package com.ssmmhh.jibam.persistence

import androidx.room.*
import com.ssmmhh.jibam.persistence.dtos.ChartDataDto
import com.ssmmhh.jibam.persistence.dtos.TransactionDto
import com.ssmmhh.jibam.persistence.entities.TransactionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RecordsDao {


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrReplace(transactionEntity: TransactionEntity): Long

    @Query("SELECT * FROM records WHERE rId = :id")
    suspend fun getRecordById(id: Int): TransactionEntity

    @Query(
        """
            SELECT records.*, 
            categories.category_Name as category_name, 
            categories.cId as cat_id, 
            category_images.image_res as category_image, 
            category_images.image_background_color as category_image_color 
            FROM records 
            LEFT JOIN categories ON records.cat_id = categories.cId 
            LEFT JOIN category_images ON categories.imageId = category_images.id 
            WHERE rId = :id
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

    @Query("DELETE FROM records WHERE rId = :id")
    suspend fun deleteRecord(id: Int): Int

    /*
        get records queries
     */
    @Query(
        """
                SELECT records.*, 
                categories.category_Name as category_name, 
                category_images.image_res as category_image, 
                category_images.image_background_color as category_image_color 
                FROM records 
                LEFT JOIN categories ON records.cat_id = categories.cId 
                LEFT JOIN category_images ON categories.imageId = category_images.id 
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
                SELECT records.*, 
                categories.category_Name as category_name, 
                category_images.image_res as category_image, 
                category_images.image_background_color as category_image_color 
                FROM records 
                LEFT JOIN categories ON records.cat_id = categories.cId 
                LEFT JOIN category_images ON categories.imageId = category_images.id 
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
                SELECT records.*, 
                categories.category_Name as category_name, 
                category_images.image_res as category_image, 
                category_images.image_background_color as category_image_color 
                FROM records 
                LEFT JOIN categories ON records.cat_id = categories.cId 
                LEFT JOIN category_images ON categories.imageId = category_images.id 
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
                SELECT records.*, 
                categories.category_Name as category_name, 
                category_images.image_res as category_image, 
                category_images.image_background_color as category_image_color 
                FROM records 
                LEFT JOIN categories ON records.cat_id = categories.cId 
                LEFT JOIN category_images ON categories.imageId = category_images.id 
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
    @Query("SELECT SUM(money) FROM records WHERE money < 0 ")
    fun returnTheSumOfAllExpenses(): Flow<Double?>

    @Query("SELECT SUM(money) FROM records WHERE money > 0 ")
    fun returnTheSumOfAllIncome(): Flow<Double?>

    //between dates
    @Query("SELECT SUM(money) FROM records WHERE (date BETWEEN :minDate AND :maxDate) AND(money < 0) ")
    fun returnTheSumOfExpensesBetweenDates(minDate: Int, maxDate: Int): Flow<Double>

    @Query("SELECT SUM(money) FROM records WHERE (date BETWEEN :minDate AND :maxDate) AND(money > 0) ")
    fun returnTheSumOfIncomeBetweenDates(minDate: Int, maxDate: Int): Flow<Double>

    //after than
    @Query("SELECT SUM(money) FROM records WHERE (date > :minDate) AND (money < 0) ")
    fun returnTheSumOfExpensesAfterThan(minDate: Int): Flow<Double>

    @Query("SELECT SUM(money) FROM records WHERE (date > :minDate) AND (money > 0) ")
    fun returnTheSumOfIncomeAfterThan(minDate: Int): Flow<Double>

    //before than
    @Query("SELECT SUM(money) FROM records WHERE (date < :maxDate) AND (money < 0) ")
    fun returnTheSumOfExpensesBeforeThan(maxDate: Int): Flow<Double>

    @Query("SELECT SUM(money) FROM records WHERE (date < :maxDate) AND (money > 0) ")
    fun returnTheSumOfIncomeBeforeThan(maxDate: Int): Flow<Double>

    @Query(
        """SELECT SUM(money) as sumOfMoney,
            categories.cId as categoryId, 
            categories.category_Name as category_name, 
            categories.type as categoryType, 
            category_images.image_res as category_image_res, 
            category_images.image_background_color as category_image_background_color 
            FROM records 
            LEFT JOIN categories ON records.cat_id = categories.cId 
            LEFT JOIN category_images ON categories.imageId = category_images.id 
            WHERE date BETWEEN :fromDate AND :toDate 
            GROUP BY cat_id 
            ORDER BY ABS(SUM(money)) DESC"""
    )
    suspend fun sumOfMoneyGroupByCategory(
        fromDate: Int,
        toDate: Int
    ): List<ChartDataDto>

    @Query(
        """SELECT records.*,  
            categories.category_Name as category_name, 
            category_images.image_res as category_image, 
            category_images.image_background_color as category_image_color 
            FROM records 
            LEFT JOIN categories ON records.cat_id = categories.cId 
            LEFT JOIN category_images ON categories.imageId = category_images.id 
            WHERE cat_id = :categoryId 
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

fun RecordsDao.getRecords(
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

fun RecordsDao.getSumOfIncome(
    minDate: Int? = null,
    maxDate: Int? = null
): Flow<Double?> {
    if (minDate != null && maxDate != null) {
        return returnTheSumOfIncomeBetweenDates(minDate, maxDate)
    }
    if (minDate == null && maxDate != null) {
        return returnTheSumOfIncomeBeforeThan(maxDate)
    }
    if (maxDate == null && minDate != null) {
        return returnTheSumOfIncomeAfterThan(minDate)
    }
    return returnTheSumOfAllIncome()
}

fun RecordsDao.getSumOfExpenses(
    minDate: Int? = null,
    maxDate: Int? = null
): Flow<Double?> {
    if (minDate != null && maxDate != null) {
        return returnTheSumOfExpensesBetweenDates(minDate, maxDate)
    }
    if (minDate == null && maxDate != null) {
        return returnTheSumOfExpensesBeforeThan(maxDate)
    }
    if (maxDate == null && minDate != null) {
        return returnTheSumOfExpensesAfterThan(minDate)
    }
    return returnTheSumOfAllExpenses()
}