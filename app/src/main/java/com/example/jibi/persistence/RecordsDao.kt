package com.example.jibi.persistence

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.jibi.models.Record

interface RecordsDao {

    @Query(
        """
        SELECT * FROM records
    """
    )
    fun getRecords(): LiveData<List<Record>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertOrReplace(record: Record):Long

    @Update
    fun updateRecord(record: Record)

    @Delete
    fun deleteRecord(record: Record)
}