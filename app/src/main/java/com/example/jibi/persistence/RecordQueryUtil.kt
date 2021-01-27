package com.example.jibi.persistence

import com.example.jibi.models.Record
import kotlinx.coroutines.flow.Flow
import kotlin.math.max
import kotlin.math.min

class RecordQueryUtil {
    companion object {

    }
}

fun RecordsDao.getRecords(
    minDate: Int?=null,
    maxDate: Int?=null
): Flow<List<Record>> {
    if (minDate != null && maxDate != null) {
        return loadAllRecordsBetweenDates(minDate, maxDate)
    }
    if (minDate == null && maxDate != null) {
        return loadAllRecordsBeforeThan(maxDate)
    }
    if (maxDate == null && minDate != null) {
        return loadAllRecordsAfterThan(minDate)
    }
    return getAllRecords()
}

fun RecordsDao.getSumOfIncome(
    minDate: Int?=null,
    maxDate: Int?=null
): Flow<Double?> {
    if (minDate != null && maxDate != null) {
        return returnTheSumOfIncomeBetweenDates(minDate, maxDate)
    }
    if (minDate == null && maxDate != null) {
        return returnTheSumOfIncomeBeforeThan(maxDate)
    }
    if (maxDate == null && minDate != null) {
        return returnTheSumOfIncomeAfterThan(minDate)
    }
    return returnTheSumOfAllIncome()
}

fun RecordsDao.getSumOfExpenses(
    minDate: Int?=null,
    maxDate: Int?=null
): Flow<Double?> {
    if (minDate != null && maxDate != null) {
        return returnTheSumOfExpensesBetweenDates(minDate, maxDate)
    }
    if (minDate == null && maxDate != null) {
        return returnTheSumOfExpensesBeforeThan(maxDate)
    }
    if (maxDate == null && minDate != null) {
        return returnTheSumOfExpensesAfterThan(minDate)
    }
    return returnTheSumOfAllExpenses()
}