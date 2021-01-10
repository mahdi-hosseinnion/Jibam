package com.example.jibi.repository.main

import android.util.Log
import com.example.jibi.models.Record
import com.example.jibi.persistence.CategoriesDao
import com.example.jibi.persistence.RecordsDao
import com.example.jibi.ui.main.transaction.state.TransactionStateEvent
import com.example.jibi.ui.main.transaction.state.TransactionStateEvent.OneShotOperationsTransactionStateEvent
import com.example.jibi.ui.main.transaction.state.TransactionStateEvent.OneShotOperationsTransactionStateEvent.*
import com.example.jibi.util.Constants.Companion.CACHE_TIMEOUT
import com.example.jibi.util.DataState
import com.example.jibi.util.MessageType
import io.mockk.*
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import java.util.*
import kotlin.collections.ArrayList


@InternalCoroutinesApi
@ExperimentalCoroutinesApi
class MainRepositoryTest {
    private val ERROR_MESSAGE = "SOMETHING GOES WRUNG"
    private val RECORD_1 = Record(3, 32, "32a3g", 1, 32232345)

    //system under test
    lateinit var mainRepository: MainRepository

    @MockK
    lateinit var recordsDao: RecordsDao

    @MockK
    lateinit var categoriesDao: CategoriesDao

    @BeforeEach
    fun init() {
        MockKAnnotations.init(this)
        mainRepository = MainRepository(
            recordsDao, categoriesDao, Locale.CANADA
        )
        //mock log.e for flow.catch in asDataState()
        mockkStatic(Log::class)
        every { Log.e(any(), any(), any()) } answers {
            println("Log.e from ${this.args[0]}, Message: ${this.args[1]}, Throwable: ${this.args[2]}")
            0
        }
    }

    @AfterEach
    fun finish() {
        unmockkAll()
    }


    @Test
    fun getSumOfIncomeAndAsDataState() = runBlocking {
        //setup
        every { recordsDao.returnTheSumOfAllIncome() } returns flow {
            delay(500)
            emit(50)
            delay(500)
            emit(56)
            delay(500)
            throw Exception(ERROR_MESSAGE)
        }.catch { t ->
            assertEquals(ERROR_MESSAGE, t.message)
        }
        val listOfResult = ArrayList<Int?>()
        mainRepository.getSumOfIncome().collect {
            println(it.toString())
            listOfResult.add(it)
        }
        //assert
        println("run assertes")
        assertEquals(50, listOfResult[0])
        assertEquals(56, listOfResult[1])
    }

    @Test
    fun getSumOfExpensesAndAsDataState() = runBlocking {
        var minDate = 0
        var lastDate = 100
        //setup
        every { recordsDao.returnTheSumOfExpensesBetweenDates(minDate, lastDate) } returns flow {
            delay(500)
            emit(50)
            delay(500)
            emit(56)
            delay(500)
            throw Exception(ERROR_MESSAGE)
        }.catch { t ->
            assertEquals(ERROR_MESSAGE, t.message)
        }
        val listOfResult = ArrayList<Int?>()
        mainRepository.getSumOfExpenses(minDate, lastDate).collect {
            println(it.toString())
            listOfResult.add(it)
        }
        //assert
        println("run assertes")
        assertEquals(50, listOfResult[0])
        assertEquals(56, listOfResult[1])
    }

    /*
    dataBase main dao
     */
    @Test
    fun testInsertTransaction_AndSafeCacheCall_WrongReturned_Success(): Unit = runBlocking {
        //arrange
        //Tip: you should use coEvery instead of every for suspending functions
        coEvery { recordsDao.insertOrReplace(any()) } returns 1L
        //act
        val result = mainRepository.insertTransaction(InsertTransaction(RECORD_1))

        //assert
        print(result)
        //assert response message
        assertTrue(
            result.stateMessage!!.response.message
            !!.contains("Successfully")
        )

    }

    @ParameterizedTest
    @ValueSource(longs = [0, -1])
    fun testInsertTransaction_AndSafeCacheCall_WrongReturned_Fail(returnedValue: Long): Unit =
        runBlocking {
            //arrange
            //Tip: you should use coEvery instead of every for suspending functions
            coEvery { recordsDao.insertOrReplace(any()) } returns returnedValue
            //act
            val result = mainRepository.insertTransaction( InsertTransaction(RECORD_1))
            //assert
            print(result)
            assertEquals(MessageType.Error, result.stateMessage!!.response.messageType)
            //assert response message
            val actualString =
                assertTrue(
                    result.stateMessage!!.response.message
                    !!.contains("Error")
                )

        }

    @Test
    fun testDeleteTransaction_AndSafeCacheCall_WrongReturned_Success(): Unit = runBlocking {
        //arrange
        //Tip: you should use coEvery instead of every for suspending functions
        coEvery { recordsDao.deleteRecord(any()) } returns 1
        //act
        val result = mainRepository.deleteTransaction( DeleteTransaction(RECORD_1))
        //assert
        //assert response message
        assertTrue(
            result.stateMessage!!.response.message
            !!.contains("Successfully")
        )

    }

    @ParameterizedTest
    @ValueSource(ints = [0, -1])
    fun testDeleteTransaction_AndSafeCacheCall_WrongReturned_Fail(returnedValue: Int): Unit =
        runBlocking {
            //arrange
            //Tip: you should use coEvery instead of every for suspending functions
            coEvery { recordsDao.deleteRecord(any()) } returns returnedValue
            //act
            val result = mainRepository.deleteTransaction(DeleteTransaction(RECORD_1))
            //assert
            print(result)
            assertEquals(MessageType.Error, result.stateMessage!!.response.messageType)
            //assert response message
            assertTrue(
                result.stateMessage!!.response.message
                !!.contains("Error")
            )

        }

    @Test
    fun testDeleteTransaction_AndSafeCacheCall_TIMEOUT_Success(): Unit = runBlocking {
        //arrange
        //Tip: you should use coEvery instead of every for suspending functions
        coEvery { recordsDao.deleteRecord(any()) } coAnswers {
            delay(CACHE_TIMEOUT + 500)
            1
        }
        //act
        val result = mainRepository.deleteTransaction(DeleteTransaction(RECORD_1))

        //assert
        assertEquals(MessageType.Error, result.stateMessage!!.response.messageType)
        //assert response message
        assertTrue(
            result.stateMessage!!.response.message
            !!.contains("timeout")
        )

    }


}