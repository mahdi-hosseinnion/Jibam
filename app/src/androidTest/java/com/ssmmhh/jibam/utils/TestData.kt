package com.ssmmhh.jibam.utils

import android.content.res.Resources
import com.ssmmhh.jibam.persistence.entities.TransactionEntity
import com.ssmmhh.jibam.persistence.CategoriesDao
import com.ssmmhh.jibam.util.NUMBER_SEPARATOR

object TestData {

    object ChartPageTestData {
        val entities = listOf(
            TransactionEntity(1, -240.4, "hey im memo", 15, 1627070114),
            TransactionEntity(6, -123.4, "", 17, 1688648079),
            TransactionEntity(7, 780.0, null, 37, 1691762573),
            TransactionEntity(48, -134.15, "hey ", 1, 1603454797),
            TransactionEntity(97, -52.3, null, 15, 1690654732),
            TransactionEntity(2, 510.0, " ", 32, 1641402114),
            TransactionEntity(79, -14.242, "then what is wrong with yo", 12, 1651686465),
            TransactionEntity(71, 13.221, "funniest thing ever", 40, 1606253099),
            TransactionEntity(10, 250.0, null, 37, 1641402115),
            TransactionEntity(11, 1.0, "what sup", 36, 1609401076),

            )
        //EXPENSES

        val largestExpensesCategoryMoney = "292.7"
        val largestExpensesCategoryId = 15
        suspend fun largestExpensesCategoryName(
            resources: Resources,
            packageName: String,
            categoriesDao: CategoriesDao,
        ): String = categoriesDao.getCategoryById(
            largestExpensesCategoryId
        )!!.toCategory().getCategoryNameFromStringFile(
            instrumentationContext
        )

        //INCOME
        //separate numbers 3 by 3 convert '1030' to '1,030'
        val largestIncomeCategoryMoney = "1${NUMBER_SEPARATOR}030"
        val largestIncomeCategoryId = 37
        suspend fun largestIncomeCategoryName(
            resources: Resources,
            packageName: String,
            categoriesDao: CategoriesDao,
        ): String = categoriesDao.getCategoryById(
            largestIncomeCategoryId
        )!!.toCategory().getCategoryNameFromStringFile(
            instrumentationContext
        )
    }


}
