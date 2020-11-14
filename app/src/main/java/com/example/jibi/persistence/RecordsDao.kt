package com.example.jibi.persistence

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.jibi.models.Record
import kotlinx.coroutines.flow.Flow

@Dao
interface RecordsDao {


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrReplace(record: Record): Long

    @Update
    suspend fun updateRecord(record: Record)

    @Delete
    suspend fun deleteRecord(record: Record)

    /*
        get records queries
     */
    @Query("SELECT * FROM records")
    fun getAllRecords(): Flow<List<Record>>

    //fromDate and toDate count in the result >=
    @Query("SELECT * FROM records WHERE date BETWEEN :minDate AND :maxDate")
    fun loadAllRecordsBetweenDates(minDate: Int, maxDate: Int): Flow<List<Record>>

    @Query("SELECT * FROM records WHERE date > :minDate")
    fun loadAllRecordsAfterThan(minDate: Int): Flow<List<Record>>

    @Query("SELECT * FROM records WHERE date < :maxDate")
    fun loadAllRecordsBeforeThan(maxDate: Int): Flow<List<Record>>

    /*
        sum queries
     */

    //return all
    @Query("SELECT SUM(money) FROM records WHERE money < 0 ")
    fun returnTheSumOfAllExpenses(): Flow<Int>

    @Query("SELECT SUM(money) FROM records WHERE money > 0 ")
    fun returnTheSumOfAllIncome(): Flow<Int>

    //between dates
    @Query("SELECT SUM(money) FROM records WHERE (date BETWEEN :minDate AND :maxDate) AND(money < 0) ")
    fun returnTheSumOfExpensesBetweenDates(minDate: Int, maxDate: Int): Flow<Int>

    @Query("SELECT SUM(money) FROM records WHERE (date BETWEEN :minDate AND :maxDate) AND(money > 0) ")
    fun returnTheSumOfIncomeBetweenDates(minDate: Int, maxDate: Int): Flow<Int>

    //after than
    @Query("SELECT SUM(money) FROM records WHERE (date > :minDate) AND (money < 0) ")
    fun returnTheSumOfExpensesAfterThan(minDate: Int): Flow<Int>

    @Query("SELECT SUM(money) FROM records WHERE (date > :minDate) AND (money > 0) ")
    fun returnTheSumOfIncomeAfterThan(minDate: Int): Flow<Int>

    //before than
    @Query("SELECT SUM(money) FROM records WHERE (date < :maxDate) AND (money < 0) ")
    fun returnTheSumOfExpensesBeforeThan(maxDate: Int): Flow<Int>

    @Query("SELECT SUM(money) FROM records WHERE (date < :maxDate) AND (money > 0) ")
    fun returnTheSumOfIncomeBeforeThan( maxDate: Int): Flow<Int>
}