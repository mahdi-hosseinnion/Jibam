package com.example.jibi.persistence

import com.example.jibi.TestUtil.CATEGORY1
import com.example.jibi.TestUtil.CATEGORY_NAME2
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Assert.assertEquals
import org.junit.Test

//@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
class CategoriesDaoTest : AppDatabaseTest() {
    private val TAG = "CategoriesDaoTest HELLO I AM HERE"

    /*
           Insert, Read, Delete
        */
//    @Test
//    fun insertReadAndDelete() = runBlockingTest {
//        //Act
//        val insertedRow = categoriesDao.insertOrReplace(CATEGORY1)
//
//        val returnedValue = categoriesDao.getCategories()[0]
//
//        categoriesDao.deleteCategory(CATEGORY1)
//
//        val returnedSize = categoriesDao.getCategories().size
//        //Assert
//        assertEquals(returnedValue, CATEGORY1)
//        assertEquals(0, returnedSize)
//        assertEquals(1, insertedRow)
//    }

    /*
           Insert, update, Read, Delete
        */
    @Test
    fun insertUpdateReadAndDelete() = runBlockingTest {
//        //Act
//        val insertedRow = categoriesDao.insertOrReplace(CATEGORY1)
//
//        val returnedValue = categoriesDao.getCategories()[0]
//
//        val updatedCategory = CATEGORY1.copy(name = CATEGORY_NAME2)
//        categoriesDao.updateCategory(updatedCategory)
//
//        val returnedUpdated = categoriesDao.getCategories()[0]
//
//        categoriesDao.deleteCategory(CATEGORY1)
//
//        val returnedSize = categoriesDao.getCategories().size
//        //Assert
//        assertEquals(updatedCategory, returnedUpdated)
//        assertEquals(CATEGORY1, returnedValue)
//        assertEquals(0, returnedSize)
//        assertEquals(1, insertedRow)

    }


}