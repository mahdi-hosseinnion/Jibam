package com.ssmmhh.jibam.persistence

import androidx.room.*
import com.ssmmhh.jibam.models.PieChartData
import com.ssmmhh.jibam.models.Transaction
import com.ssmmhh.jibam.models.TransactionEntity
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
            categories.img_res as category_image 
            FROM records LEFT JOIN categories ON records.cat_id = categories.cId 
            WHERE rId = :id
        """
    )
    suspend fun getTransactionById(id: Int): Transaction?


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
        "SELECT records.*, " +
                "categories.category_Name as category_name, " +
                "categories.img_res as category_image " +
                "FROM records LEFT JOIN categories ON records.cat_id = categories.cId " +
                "WHERE " +
                "( " +
                "money LIKE '%' || :query || '%' " +
                "OR memo LIKE '%' || :query || '%' " +
                ") " + ORDER_BY_DATE
    )
    fun getAllRecords(query: String): Flow<List<Transaction>>

    //fromDate and toDate count in the result >=
    @Query(
        "SELECT records.*, " +
                "categories.category_Name as category_name, " +
                "categories.img_res as category_image " +
                "FROM records LEFT JOIN categories ON records.cat_id = categories.cId " +
                "WHERE date BETWEEN :minDate AND :maxDate " +
                "AND " +
                "( " +
                "money LIKE '%' || :query || '%' " +
                "OR memo LIKE '%' || :query || '%' " +
                ") " +
                ORDER_BY_DATE

//                ORDER BY date_updated DESC LIMIT (:page * :pageSize)
    )
    fun loadAllRecordsBetweenDates(
        minDate: Int,
        maxDate: Int,
        query: String
    ): Flow<List<Transaction>>

    //TODO CHANGE DATE > MIN DATE TO DATE >=MINDATE
    @Query(
        "SELECT records.*, " +
                "categories.category_Name as category_name, " +
                "categories.img_res as category_image " +
                "FROM records LEFT JOIN categories ON records.cat_id = categories.cId " +
                "WHERE date > :minDate " +
                "AND " +
                "( " +
                "money LIKE '%' || :query || '%' " +
                "OR memo LIKE '%' || :query || '%' " +
                ")" +
                ORDER_BY_DATE
    )
    fun loadAllRecordsAfterThan(
        minDate: Int, query: String
    ): Flow<List<Transaction>>

    @Query(
        "SELECT records.*, " +
                "categories.category_Name as category_name, " +
                "categories.img_res as category_image " +
                "FROM records LEFT JOIN categories ON records.cat_id = categories.cId " +
                "WHERE date < :maxDate " +
                "AND " +
                "( " +
                "money LIKE '%' || :query || '%' " +
                "OR memo LIKE '%' || :query || '%' " +
                ")" +
                ORDER_BY_DATE
    )
    fun loadAllRecordsBeforeThan(
        maxDate: Int, query: String
    ): Flow<List<Transaction>>

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

    //chart fragment query
//    @Query("SELECT SUM(money) as sumOfMoney ,cat_id as cat_id FROM records GROUP BY cat_id")
//    suspend fun sumOfMoneyGroupByCountry(): List<PieChartData>
    //TODO test this JOIN
    //TODO https://www.w3schools.com/sql/sql_join.asp
    @Query(
        """SELECT SUM(money) as sumOfMoney,
            categories.cId as categoryId, 
            categories.category_Name as category_name, 
            categories.type as categoryType, 
            categories.img_res as category_image 
            FROM records LEFT JOIN categories ON records.cat_id=categories.cId 
            WHERE date BETWEEN :fromDate AND :toDate 
            GROUP BY cat_id 
            ORDER BY ABS(SUM(money)) DESC"""
    )
    suspend fun sumOfMoneyGroupByCategory(
        fromDate: Int,
        toDate: Int
    ): List<PieChartData>

    @Query(
        """SELECT records.*,  
            categories.category_Name as category_name, 
            categories.type as categoryType, 
            categories.img_res as category_image 
            FROM records LEFT JOIN categories ON records.cat_id=categories.cId 
            WHERE cat_id = :categoryId 
            AND 
            date BETWEEN :fromDate AND :toDate 
            ORDER BY ABS(money) DESC"""
    )
    fun getAllTransactionByCategoryId(
        categoryId: Int,
        fromDate: Int,
        toDate: Int
    ): Flow<List<Transaction>>

    companion object {
        const val ORDER_BY_DATE = "ORDER BY date DESC"
    }
}