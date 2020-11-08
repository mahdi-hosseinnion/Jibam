package com.example.jibi.persistence

import com.example.jibi.TestUtil.RECORD1
import com.example.jibi.models.Record
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Assert.assertEquals
import org.junit.Test

private const val TAG = "RecordsDaoTest"

@ExperimentalCoroutinesApi
class RecordsDaoTest : AppDatabaseTest() {


    @Test
    fun insertReadDelete() = runBlockingTest {
        //setup
        var recordsList: List<Record>? = null
        val job = launch {
            ensureActive()
            recordsDao.getAllRecords().collect {
                ensureActive()
                recordsList = it
            }
        }

        //insert
        val insertedRow = recordsDao.insertOrReplace(RECORD1)

        //read
        val returnedValue = recordsList
        assertEquals(1, returnedValue?.size)

        //delete
        recordsDao.deleteRecord(RECORD1)

        val deletedSize = recordsList?.size

        //Assert
        assertEquals(1, insertedRow)
        assertEquals(RECORD1, returnedValue?.get(0))
        assertEquals(0, deletedSize)
        job.cancel()
    }

    @Test
    fun insertUpdateReadDelete() = runBlockingTest {
        //setup

//        val recordsList = ArrayList<Record>()
        var recordsList: List<Record>? = null
        val job = launch {
            ensureActive()
            recordsDao.getAllRecords().collect {
                ensureActive()
                recordsList = it
//                recordsList.clear()
//                recordsList.addAll(it)
            }
        }
        //insert
        val insertedRow = recordsDao.insertOrReplace(RECORD1)
        //read
        val returnedValue = recordsList
        //update
        val updatedRecord = RECORD1.copy(memo = "askf", date = 329283)
        recordsDao.updateRecord(updatedRecord)


        val updatedValue = recordsList
        //delete
        recordsDao.deleteRecord(RECORD1)

        val deletedSize = recordsList?.size
        //Assert
        assertEquals(1, insertedRow)
        assertEquals(1, returnedValue?.size)
        assertEquals(RECORD1, returnedValue?.get(0))
        assertEquals(updatedRecord, updatedValue?.get(0))
        assertEquals(0, deletedSize)
        job.cancel()
    }

    fun now() = System.currentTimeMillis()
}