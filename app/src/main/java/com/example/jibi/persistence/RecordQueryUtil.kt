package com.example.jibi.persistence

import com.example.jibi.models.TransactionEntity
import com.example.jibi.models.SearchModel
import com.example.jibi.models.Transaction
import kotlinx.coroutines.flow.Flow

class RecordQueryUtil {
    companion object {

    }
}

fun RecordsDao.getRecords(
    minDate: Int? = null,
    maxDate: Int? = null,
    query: String = ""
): Flow<List<Transaction>> {
    if (minDate != null && maxDate != null) {
        return loadAllRecordsBetweenDates(minDate, maxDate, query)
    }
    if (minDate == null && maxDate != null) {
        return loadAllRecordsBeforeThan(maxDate, query)
    }
    if (maxDate == null && minDate != null) {
        return loadAllRecordsAfterThan(minDate, query)
    }
    return getAllRecords(query)
}

fun RecordsDao.getSumOfIncome(
    minDate: Int? = null,
    maxDate: Int? = null
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
    minDate: Int? = null,
    maxDate: Int? = null
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