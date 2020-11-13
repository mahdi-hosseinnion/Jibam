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

    //queries
    @Query(
        """
        SELECT * FROM records
    """
    )
    fun getAllRecords(): Flow<List<Record>>

    @Query("SELECT * FROM records WHERE date BETWEEN :fromDate AND :toDate")
    fun loadAllRecordsBetweenDates(fromDate: Int, toDate: Int): Flow<List<Record>>

    @Query("SELECT * FROM records WHERE date > :minDate")
    fun loadAllRecordsAfterThan(minDate: Int): Flow<List<Record>>

    @Query("SELECT * FROM records WHERE date < :maxDate")
    fun loadAllRecordsBeforeThan(maxDate: Int): Flow<List<Record>>
    /*
    //pashmama
    //in query ro nega mitone jam bezane
    @Query("SELECT SUM(date) FROM records WHERE date BETWEEN :fromDate AND :toDate")
    fun loadAllRecordsBetweenDates(fromDate: Int, toDate: Int): Flow<List<Record>>*/
}