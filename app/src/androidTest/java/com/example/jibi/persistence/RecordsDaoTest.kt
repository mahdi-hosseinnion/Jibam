package com.example.jibi.persistence

import com.example.jibi.TestUtil.RECORD1
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Assert.assertEquals
import org.junit.Test

@ExperimentalCoroutinesApi
class RecordsDaoTest : AppDatabaseTest() {


    @Test
    fun insertReadDelete() = runBlockingTest {
        //insert
        val insertedRow = recordsDao.insertOrReplace(RECORD1)
        //read
        val returnedValue = recordsDao.getRecords()
        //delete
        recordsDao.deleteRecord(RECORD1)
        val deletedSize = recordsDao.getRecords().size
        //Assert
        assertEquals(1, insertedRow)
        assertEquals(1, returnedValue.size)
        assertEquals(RECORD1, returnedValue[0])
        assertEquals(0, deletedSize)
    }

    @Test
    fun insertUpdateReadDelete() = runBlockingTest {
        //insert
        val insertedRow = recordsDao.insertOrReplace(RECORD1)
        //read
        val returnedValue = recordsDao.getRecords()
        //update
        val updatedRecord = RECORD1.copy(memo = "askf", date = 329283)
        recordsDao.updateRecord(updatedRecord)
        val updatedValue = recordsDao.getRecords()
        //delete
        recordsDao.deleteRecord(RECORD1)
        val deletedSize = recordsDao.getRecords().size
        //Assert
        assertEquals(1, insertedRow)
        assertEquals(1, returnedValue.size)
        assertEquals(RECORD1, returnedValue[0])
        assertEquals(updatedRecord, updatedValue[0])
        assertEquals(0, deletedSize)
    }

}