package com.example.jibi.persistence

import android.util.Log
import com.example.jibi.TestUtil.RECORD1
import com.example.jibi.models.Record
import com.example.jibi.util.DEBUG
import com.example.jibi.util.isUnitTest
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Test
import kotlin.random.Random


@ExperimentalCoroutinesApi
class RecordsDaoTest : AppDatabaseTest() {
    private val TAG = "RecordsDaoTest"
/*
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

    @Test
    fun testQuery_loadAllRecordsAfterThan() = runBlockingTest {
        val minDate = 3
        val count = 10

        //setup getter for all inserted row
        var filteredRecordsList: List<Record>? = null
        var recordsList: List<Record>? = null
        val job = launch {
            ensureActive()
            launch {
                recordsDao.loadAllRecordsAfterThan(minDate).collect {
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
        for (i in 1..count) {
            recordsDao.insertOrReplace(RECORD1.copy(id = i + 100, date = i))
        }
        //update with an out range date into in range of fromData to toData
        val updatedDate = RECORD1.copy(id = 101, date = Random.nextInt(minDate, (count - 1)))
        recordsDao.updateRecord(updatedDate)
        //assert
        val lastValue = recordsList
        val expectedValues = lastValue?.filter { (it.date > minDate) }
        printList(expectedValues, "expectedValues")
        printList(filteredRecordsList, "outPutValue")
        assertArrayEquals(expectedValues?.toTypedArray(), filteredRecordsList?.toTypedArray())
        job.cancel()

    }

    @Test
    fun testQuery_loadAllRecordsBeforeThan() = runBlockingTest {
        val maxDate = 5
        val range = 10

        //setup getter for all inserted row
        var filteredRecordsList: List<Record>? = null
        var recordsList: List<Record>? = null
        val job = launch {
            ensureActive()
            launch {
                recordsDao.loadAllRecordsBeforeThan(maxDate).collect {
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
        val updatedDate = RECORD1.copy(id = 105, date = Random.nextInt(1, maxDate))
        recordsDao.updateRecord(updatedDate)
        //assert
        val lastValue = recordsList
        val expectedValues = lastValue?.filter { (it.date < maxDate) }
        printList(expectedValues, "expectedValues")
        printList(filteredRecordsList, "outPutValue")
        assertArrayEquals(expectedValues?.toTypedArray(), filteredRecordsList?.toTypedArray())
        job.cancel()

    }

    *//*
        ++TEST FOR SUM QUERIES
     *//*

    @Test
    fun testQuery_returnTheSumOfAllIncome() = runBlockingTest {
        //insert fake data
        val insertedList = insertFakeDataToDateBase()

        //setup getter for all inserted row
        var sumOfAllExpenses: Int? = null
        var sumOfAllIncome: Int? = null
        var recordsList: List<Record>? = null
        val job = launch {
            ensureActive()
            launch {
                recordsDao.returnTheSumOfAllExpenses().collect {
                    ensureActive()
                    printOnLog("SUM OF ALL EXPENSES = $it")
                    sumOfAllExpenses = it
                }
            }
            launch {
                recordsDao.returnTheSumOfAllIncome().collect {
                    ensureActive()
                    printOnLog("1.SUM OF ALL INCOME = $it")
                    sumOfAllIncome = it
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
        val insertedRecords = recordsList
        var expectedIncome = 0
        var expectedExpenses = 0
        for (item in insertedRecords!!) {
            if (item.money > 0) {
                expectedIncome += item.money
            } else {
                expectedExpenses += item.money
            }
        }
        assertEquals(expectedIncome, sumOfAllIncome)
        assertEquals(expectedExpenses, sumOfAllExpenses)
        //update
        val tempRecordForUpdatingIncome = insertedList[0]
        recordsDao.updateRecord(
            tempRecordForUpdatingIncome
                .copy(money = (tempRecordForUpdatingIncome.money + 60))
        )
        val tempRecordForUpdatingExpenses = insertedList[1]
        recordsDao.updateRecord(
            tempRecordForUpdatingExpenses
                .copy(money = (tempRecordForUpdatingExpenses.money + (-60)))
        )
        assertEquals(expectedIncome + 60, sumOfAllIncome)
        assertEquals(expectedExpenses + (-60), sumOfAllExpenses)

        val row = recordsDao.insertOrReplace(Record(152, 1000, "memo32", 0, 947243))

        assertEquals(152, row)
        assertEquals((expectedIncome + 1060), sumOfAllIncome)
        recordsDao.insertOrReplace(Record(152, 2000, "memo32", 0, 947243))
        assertEquals((expectedIncome + 2060), sumOfAllIncome)
        recordsDao.updateRecord(Record(152, 3000, "memo32", 0, 947243))
        assertEquals((expectedIncome + 3060), sumOfAllIncome)
        job.cancel()
    }


    @Test
    fun testQuery_returnTheSumOfIncomeAndExpensesBetweenDates() = runBlockingTest {
        val minDate = 100
        val maxDate = 500
        //insert fake data
        insertFakeDataToDateBase()

        //setup getter for all inserted row
        var sumOfAllIncome: Int? = null
        var sumOfAllExpenses: Int? = null
        var recordsList: List<Record>? = null
        val job = launch {
            ensureActive()
            launch {
                recordsDao.returnTheSumOfExpensesBetweenDates(minDate, maxDate).collect {
                    ensureActive()
                    printOnLog("SUM OF ALL EXPENSES = $it")
                    sumOfAllExpenses = it
                }
            }
            launch {
                recordsDao.returnTheSumOfIncomeBetweenDates(minDate, maxDate).collect {
                    ensureActive()
                    printOnLog("2.SUM OF ALL INCOME = $it")
                    sumOfAllIncome = it
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
        val insertedRecords = recordsList
        var expectedIncome = 0
        var expectedExpenses = 0
        var outOfRangeRecordExpenses: Record? = null
        var outOfRangeRecordIncome: Record? = null
        var inRangeRecordIncome: Record? = null
        for (item in insertedRecords!!) {
            if (
                item.date in minDate..maxDate
            ) {
                if (item.money > 0) {
                    inRangeRecordIncome = item
                    expectedIncome += item.money
                } else {
                    expectedExpenses += item.money
                }

            } else {
                if (item.money > 0) {
                    outOfRangeRecordIncome = item
                } else {
                    outOfRangeRecordExpenses = item
                }
            }
        }
        assertEquals(expectedIncome, sumOfAllIncome)
        assertEquals(expectedExpenses, sumOfAllExpenses)
        //update
        val tempRecordForUpdatingIncome = outOfRangeRecordIncome
        recordsDao.updateRecord(
            tempRecordForUpdatingIncome!!
                .copy(
                    money = (tempRecordForUpdatingIncome.money + 60),
                    date = (minDate + 1)
                )
        )
        val tempRecordForUpdatingExpenses = outOfRangeRecordExpenses
        recordsDao.updateRecord(
            tempRecordForUpdatingExpenses!!
                .copy(
                    money = (tempRecordForUpdatingExpenses.money + (-60)),
                    date = (minDate + 1)
                )
        )
        expectedIncome += 60 + tempRecordForUpdatingIncome.money
        expectedExpenses += +(-60) + tempRecordForUpdatingExpenses.money
        assertEquals(expectedIncome, sumOfAllIncome)
        assertEquals(expectedExpenses, sumOfAllExpenses)

        recordsDao.updateRecord(
            inRangeRecordIncome!!.copy(
                money = inRangeRecordIncome.money + 100
            )
        )
        expectedIncome += 100
        assertEquals(expectedIncome, sumOfAllIncome)

        job.cancel()
    }

    @Test
    fun testQuery_returnTheSumOfExpensesAndIncomeAfterThan() = runBlockingTest {
        val minDate = 300
        //insert fake data
        insertFakeDataToDateBase()

        //setup getter for all inserted row
        var sumOfAllIncome: Int? = null
        var sumOfAllExpenses: Int? = null
        var recordsList: List<Record>? = null
        val job = launch {
            ensureActive()
            launch {
                recordsDao.returnTheSumOfExpensesAfterThan(minDate).collect {
                    ensureActive()
                    printOnLog("SUM OF ALL EXPENSES = $it")
                    sumOfAllExpenses = it
                }
            }
            launch {
                recordsDao.returnTheSumOfIncomeAfterThan(minDate).collect {
                    ensureActive()
                    printOnLog("3.SUM OF ALL INCOME = $it")
                    sumOfAllIncome = it
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


        val insertedRecords = recordsList
        var expectedExpenses = 0
        var expectedIncome = 0
        //for update with outRange row
        var outOfRangeRecordExpenses: Record? = null
        var outOfRangeRecordIncome: Record? = null
        var inRangeRecordIncome: Record? = null
        for (item in insertedRecords!!) {
            if (item.date > minDate
            ) {
                if (item.money > 0) {
                    inRangeRecordIncome = item
                    expectedIncome += item.money
                } else {
                    expectedExpenses += item.money
                }
            } else {
                //for update
                if (item.money > 0) {
                    outOfRangeRecordIncome = item
                } else {
                    outOfRangeRecordExpenses = item
                }
            }
        }
        assertEquals(expectedIncome, sumOfAllIncome)
        assertEquals(expectedExpenses, sumOfAllExpenses)

        //update with outRange row
        val tempRecordForUpdatingIncome = outOfRangeRecordIncome
        recordsDao.updateRecord(
            tempRecordForUpdatingIncome!!
                .copy(
                    money = (tempRecordForUpdatingIncome.money + 60),
                    date = (minDate + 1)
                )
        )

        val tempRecordForUpdatingExpenses = outOfRangeRecordExpenses
        recordsDao.updateRecord(
            tempRecordForUpdatingExpenses!!
                .copy(
                    money = (tempRecordForUpdatingExpenses.money + (-60)),
                    date = (minDate + 1)
                )
        )
        expectedIncome += 60 + tempRecordForUpdatingIncome.money
        expectedExpenses += (-60) + tempRecordForUpdatingExpenses.money
        assertEquals(expectedIncome, sumOfAllIncome)
        assertEquals(expectedExpenses, sumOfAllExpenses)
        //update with in range row
        recordsDao.updateRecord(
            inRangeRecordIncome!!.copy(
                money = inRangeRecordIncome.money + 100
            )
        )
        expectedIncome += 100
        assertEquals(expectedIncome, sumOfAllIncome)
        job.cancel()
    }

    @Test
    fun testQuery_returnTheSumOfIncomeAndExpensesBeforeThan() = runBlockingTest {
        val maxDate = 300
        //insert fake data
        insertFakeDataToDateBase()

        //setup getter for all inserted row
        var sumOfAllIncome: Int? = null
        var sumOfAllExpenses: Int? = null
        var recordsList: List<Record>? = null
        val job = launch {
            ensureActive()
            launch {
                recordsDao.returnTheSumOfExpensesBeforeThan(maxDate).collect {
                    ensureActive()
                    printOnLog("SUM OF ALL EXPENSES = $it")
                    sumOfAllExpenses = it
                }
            }
            launch {
                recordsDao.returnTheSumOfIncomeBeforeThan(maxDate).collect {
                    ensureActive()
                    printOnLog("4.SUM OF ALL INCOME = $it")
                    sumOfAllIncome = it
                }
            }
            launch {
                recordsDao.getAllRecords().collect {
                    ensureActive()
                    printList(it, "1.Main Value")
                    recordsList = it
                }
            }
        }


        val insertedRecords = recordsList
        var expectedExpenses = 0
        var expectedIncome = 0
        //for update
        var outOfRangeRecordExpenses: Record? = null
        var outOfRangeRecordIncome: Record? = null
        var inRangeRecordIncome: Record? = null
        for (item in insertedRecords!!) {
            if (item.date < maxDate
            ) {
                if (item.money > 0) {
                    inRangeRecordIncome = item
                    expectedIncome += item.money
                } else {
                    expectedExpenses += item.money
                }
            } else {
                //for update
                if (item.money > 0) {
                    outOfRangeRecordIncome = item
                } else {
                    outOfRangeRecordExpenses = item
                }
            }
        }
        assertEquals(expectedIncome, sumOfAllIncome)
        assertEquals(expectedExpenses, sumOfAllExpenses)

        //update with outRange row
        val tempRecordForUpdatingIncome = outOfRangeRecordIncome
        recordsDao.updateRecord(
            tempRecordForUpdatingIncome!!
                .copy(
                    money = (tempRecordForUpdatingIncome.money + 60),
                    date = (maxDate - 5)
                )
        )

        val tempRecordForUpdatingExpenses = outOfRangeRecordExpenses
        recordsDao.updateRecord(
            tempRecordForUpdatingExpenses!!
                .copy(money = (tempRecordForUpdatingExpenses.money + (-60)), date = (maxDate - 5))
        )

        expectedIncome += 60 + tempRecordForUpdatingIncome.money
        expectedExpenses += (-60) + tempRecordForUpdatingExpenses.money

        assertEquals(expectedIncome, sumOfAllIncome)
        assertEquals(expectedExpenses, sumOfAllExpenses)
        recordsDao.updateRecord(
            inRangeRecordIncome!!.copy(
                money = inRangeRecordIncome.money + 100
            )
        )
        expectedIncome += 100
        assertEquals(expectedIncome, sumOfAllIncome)
        job.cancel()
    }


    @Test
    fun testFun_insertFakeDataToDateBase() = runBlockingTest {
        printOnLog("yes")
        var recordsList: List<Record>? = null
        val job = launch {
            ensureActive()
            recordsDao.getAllRecords().collect {
                ensureActive()
                recordsList = it
                printOnLog("yea")
                if (it.size == 150) {
                    printList(it, "TEST FAKE")
                }
            }
        }
        insertFakeDataToDateBase()

        val insertedValues = recordsList
        assertEquals(150, insertedValues?.size)
        job.cancel()
    }

    private suspend fun insertFakeDataToDateBase(): List<Record> {
        val result = ArrayList<Record>()
        for (i in 1..100) {

            //insert income
            //total sum of money: 5050
            val tempRecord = Record(
                0, i, "memo income", 2,
                i * Random.nextInt(100)
            )
            val row = recordsDao.insertOrReplace(tempRecord)
            printOnLog("row1 = $row")
            result.add(tempRecord.copy(id = (row.toInt())))
            if (i <= 50) {
                //insert expenses
                //total sum of money: -1275
                val tempRecord = Record(
                    0, (i * -1), "memo income", 4,
                    i * Random.nextInt(100)
                )
                val row = recordsDao.insertOrReplace(tempRecord)
                printOnLog("row2 = $row")
                result.add(tempRecord.copy(id = (row.toInt())))
            }

        }
        return result
    }

    @Test
    fun testDoubleInsertSameThing() = runBlockingTest {
        isUnitTest = true

        val record = Record(5, 13, "d", 2, 13134234)
        val job1 = launch {
            recordsDao.insertOrReplace(record)
        }
        val job2 = launch {
            recordsDao.insertOrReplace(record)
        }
        mahdiLo1g("asdy","yea called")
        job1.invokeOnCompletion { throwable ->

            if (throwable == null) {
                mahdiLo1g(TAG, "launchNewJob: Job: completed normally  ")
                return@invokeOnCompletion
            }
            if (throwable is CancellationException) {
                    //handle nonCancelable jobs
                    mahdiLo1g(
                        TAG,
                        "launchNewJob: Job1: added to unCancelable jobs stack msg: ${throwable.message} cuze: ${throwable.cause?.message}"
                    )

                    mahdiLo1g(
                        TAG,
                        "launchNewJob: Job1: cancelled normally  msg: ${throwable.message} cuze: ${throwable.cause?.message}"
                    )
                return@invokeOnCompletion
            }
        }
        job2.invokeOnCompletion { throwable ->

            if (throwable == null) {
                mahdiLo1g(TAG, "launchNewJob: Job1: completed normally  ")
                return@invokeOnCompletion
            }
            if (throwable is CancellationException) {
                    //handle nonCancelable jobs
                    mahdiLo1g(
                        TAG,
                        "launchNewJob: Job2: added to unCancelable jobs stack msg: ${throwable.message} cuze: ${throwable.cause?.message}"
                    )

                    mahdiLo1g(
                        TAG,
                        "launchNewJob: Job2: cancelled normally  msg: ${throwable.message} cuze: ${throwable.cause?.message}"
                    )
                return@invokeOnCompletion
            }
        }

    }
    fun mahdiLo1g(className: String?, message: String ) {
            println("195 $className: $message")
    }*/
}