package com.example.jibi.persistence

import android.util.Log
import com.example.jibi.TestUtil.RECORD1
import com.example.jibi.models.Record
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Assert
import org.junit.Assert.assertEquals
import org.junit.Test

private const val TAG = "RecordsDaoTest"

@ExperimentalCoroutinesApi
class RecordsDaoTest : AppDatabaseTest() {


    @Test
    fun insertReadDelete() = runBlockingTest {
        //setup
        val recordsList = ArrayList<Record>()
        val job = launch {
            ensureActive()
            recordsDao.getAllRecords().collect {
                ensureActive()
                if (it.isEmpty())
                    recordsList.clear()
                else
                    recordsList.addAll(it)
            }
        }

        //insert
        val insertedRow = recordsDao.insertOrReplace(RECORD1)

        //read
        val returnedValue = ArrayList<Record>(recordsList)
        assertEquals(1, returnedValue.size)

        //delete
        recordsDao.deleteRecord(RECORD1)

        val deletedSize = recordsList.size

        //Assert
        assertEquals(1, insertedRow)
        assertEquals(RECORD1, returnedValue[0])
        assertEquals(0, deletedSize)
        job.cancel()
    }

    @Test
    fun insertUpdateReadDelete() = runBlockingTest {
        //setup
        val recordsList = ArrayList<Record>()
        val job = launch {
            ensureActive()
            recordsDao.getAllRecords().collect {
                ensureActive()
                recordsList.clear()
                recordsList.addAll(it)
            }
        }
        //insert
        val insertedRow = recordsDao.insertOrReplace(RECORD1)
        //read
        val returnedValue = ArrayList<Record>(recordsList)
        //update
        val updatedRecord = RECORD1.copy(memo = "askf", date = 329283)
        recordsDao.updateRecord(updatedRecord)


        val updatedValue = ArrayList<Record>(recordsList)
        //delete
        recordsDao.deleteRecord(RECORD1)

        val deletedSize = recordsList.size
        //Assert
        assertEquals(1, insertedRow)
        assertEquals(1, returnedValue.size)
        assertEquals(RECORD1, returnedValue[0])
        assertEquals(updatedRecord, updatedValue[0])
        assertEquals(0, deletedSize)
        job.cancel()
    }

    fun now() = System.currentTimeMillis()
}