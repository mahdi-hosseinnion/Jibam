package com.example.jibi.repository.main

import android.util.Log
import com.example.jibi.persistence.CategoriesDao
import com.example.jibi.persistence.RecordsDao
import com.example.jibi.persistence.getSumOfIncome
import com.example.jibi.util.DataState
import com.example.jibi.util.Response
import com.example.jibi.util.ResponseType
import io.mockk.*
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.RelaxedMockK
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runBlockingTest
import org.junit.After
import org.junit.Test

import org.junit.Assert.*
import org.junit.Before

@ExperimentalCoroutinesApi
class MainRepositoryTest {
    private val ERROR_MESSAGE = "SOMETHING GOES WRUNG"

    //system under test
    lateinit var mainRepository: MainRepository

    @MockK
    lateinit var recordsDao: RecordsDao

    @MockK
    lateinit var categoriesDao: CategoriesDao

    @Before
    fun init() {
        MockKAnnotations.init(this)
        mainRepository = MainRepository(
            recordsDao, categoriesDao
        )
        //mock log.e for flow.catch in asDataState()
        mockkStatic(Log::class)
        every { Log.e(any(), any(), any()) } returns 0
    }

    @After
    fun finish() {
        unmockkAll()
    }


    @Test
    fun getSumOfIncomeAndAsLiveData() = runBlocking {
        //setup
        every { recordsDao.returnTheSumOfAllIncome() } returns flow {
            delay(500)
            emit(50)
            delay(500)
            emit(56)
            delay(500)
            throw Exception(ERROR_MESSAGE)
        }
        val listOfResult = ArrayList<DataState<Int?>>()
        mainRepository.getSumOfIncome().collect {
            println(it.toString())
            listOfResult.add(it)
        }
        //assert
        println("run assertes")
        assertEquals(listOfResult[0], DataState.loading<Int?>(isLoading = true))
        assertEquals(listOfResult[1].data?.data?.peekContent(), 50)
        assertEquals(listOfResult[2].data?.data?.peekContent(), 56)
        assertEquals(listOfResult[3].error?.peekContent()?.response?.message, ERROR_MESSAGE)
    }

}