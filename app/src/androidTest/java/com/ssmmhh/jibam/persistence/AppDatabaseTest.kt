package com.ssmmhh.jibam.persistence

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.platform.app.InstrumentationRegistry
import com.ssmmhh.jibam.util.TestUtil
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.asExecutor
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.TestCoroutineScope
import org.junit.After
import org.junit.Before
@ExperimentalCoroutinesApi
abstract class AppDatabaseTest :TestUtil(){

    //system under test
    lateinit var appDatabase: AppDatabase

    private val testDispatcher = TestCoroutineDispatcher()
    private val testScope = TestCoroutineScope(testDispatcher)

    val categoriesDao: CategoriesDao
        get() = appDatabase.getCategoriesDao()

    val recordsDao: RecordsDao
        get() = appDatabase.getRecordsDao()
    lateinit var instrumentationContext: Context

    @Before
    fun init() {
        instrumentationContext = InstrumentationRegistry.getInstrumentation().context
        appDatabase = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        )   .setTransactionExecutor(testDispatcher.asExecutor())
            .setQueryExecutor(testDispatcher.asExecutor())
            .build()
    }

    @After
    fun finish() {
        appDatabase.close()
    }
}