package com.example.jibi.persistence

import androidx.room.*
import com.example.jibi.models.Record
import kotlinx.coroutines.flow.Flow
import java.util.logging.Filter

@Dao
interface RecordsDao {


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrReplace(record: Record): Long

    @Query("SELECT * FROM records WHERE rId = :id")
    suspend fun getRecordById(id: Int): Record


    /**
     * To delete one or more products.
     * Returns number of rows deleted. 0 if no row deleted.
     */
    @Update
    suspend fun updateRecord(vararg record: Record): Int

    @Delete
    suspend fun deleteRecord(vararg record: Record): Int

    /*
        get records queries
     */
    @Query(
        "SELECT * FROM records " +
                "WHERE " +
                "( " +
                "money LIKE '%' || :query || '%' " +
                "OR memo LIKE '%' || :query || '%' " +
                ") " + ORDER_BY_DATE
    )
    fun getAllRecords(query: String): Flow<List<Record>>

    //fromDate and toDate count in the result >=
    @Query(
        "SELECT * FROM records WHERE date BETWEEN :minDate AND :maxDate " +
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
    ): Flow<List<Record>>

    @Query(
        "SELECT * FROM records WHERE date > :minDate " +
                "AND " +
                "( " +
                "money LIKE '%' || :query || '%' " +
                "OR memo LIKE '%' || :query || '%' " +
                ")" +
                ORDER_BY_DATE
    )
    fun loadAllRecordsAfterThan(
        minDate: Int, query: String
    ): Flow<List<Record>>

    @Query(
        "SELECT * FROM records WHERE date < :maxDate " +
                "AND " +
                "( " +
                "money LIKE '%' || :query || '%' " +
                "OR memo LIKE '%' || :query || '%' " +
                ")" +
                ORDER_BY_DATE
    )
    fun loadAllRecordsBeforeThan(
        maxDate: Int, query: String
    ): Flow<List<Record>>

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

    companion object {
        const val ORDER_BY_DATE = "ORDER BY date DESC"
    }
}