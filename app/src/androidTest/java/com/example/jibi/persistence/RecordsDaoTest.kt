package com.example.jibi.persistence

import android.util.Log
import com.example.jibi.TestUtil.RECORD1
import com.example.jibi.models.Record
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Test
import kotlin.random.Random

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

    @Test
    fun testQuery_LoadAllRecordsBetweenDates() = runBlockingTest {
        val fromDate = 3
        val toDate = 8
        val range = 10

        //setup getter for all inserted row
        var filteredRecordsList: List<Record>? = null
        var recordsList: List<Record>? = null
        val job = launch {
            ensureActive()
            launch {
                recordsDao.loadAllRecordsBetweenDates(fromDate, toDate).collect {
                    ensureActive()
                    printList(it, "Filtered Value")
                    filteredRecordsList = it
                }
            }
            launch {
                recordsDao.getAllRecords().collect {
                    ensureActive()
                    printList(it, "Main Value")
                    recordsList = it
                }
            }
        }
        //insert sum dummy data
        for (i in 1..range) {
            recordsDao.insertOrReplace(RECORD1.copy(id = i + 100, date = i))
        }
        //update with an out range date into in range of fromData to toData
        val updatedDate = RECORD1.copy(id = 101, date = Random.nextInt(fromDate, toDate))
        recordsDao.updateRecord(updatedDate)
        //assert
        val lastValue = recordsList
        val expectedValues = lastValue?.filter { (it.date in (fromDate..toDate)) }
        printList(expectedValues, "expectedValues")
        printList(filteredRecordsList, "outPutValue")
        assertArrayEquals(expectedValues?.toTypedArray(), filteredRecordsList?.toTypedArray())
        job.cancel()

    }


    private fun <T> printList(
        data: List<T>?,
        msg: String = ""

    ) {

        if (data == null) {
            printOnLog("$msg +++++++++++++++++ size = null")
            return
        }
        printOnLog("$msg +++++++++++++++++ size = ${data.size}")

        for (item in data) {
            printOnLog(msg + item.toString())
        }
    }

    private fun printOnLog(msg: String) = Log.d(TAG, "printOnLog: mahdi -> $msg")
    private fun now() = System.currentTimeMillis()
}