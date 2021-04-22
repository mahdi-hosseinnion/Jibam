package com.example.jibi.models

import androidx.test.platform.app.InstrumentationRegistry
import com.example.jibi.R
import org.junit.Assert
import org.junit.Test


class CategoryTest {
    private val testStringId = R.string.testing_string
    private val testStringName = "testing_string"

    //system under test
    private val appContext = InstrumentationRegistry.getInstrumentation().targetContext

    @Test
    fun testGetCategoryName_success() {
        val category= Category(1, 1, testStringName, "funny", 1)
        val actualString =
            category.getCategoryNameFromStringFile(appContext.resources, appContext.packageName) {
                " THIS SHOULD NOT BE HAPPEND"
            }

        Assert.assertEquals(appContext.getString(testStringId), actualString)
    }

    @Test
    fun testGetCategoryName_failure() {
        val errorMarker= "ERROR MAKER >SLJ"
        val category = Category(1, 1, "undefinedString", "funny", 1)
        val actualString =
            category.getCategoryNameFromStringFile(appContext.resources, appContext.packageName) {
                errorMarker
            }

        Assert.assertEquals(errorMarker, actualString)
    }
}