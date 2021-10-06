package com.ssmmhh.jibam.persistence

class RecordQueryUtilTest {
    //system under test ("RecordQueryUtil")
/*    val recordsDao = mockk<RecordsDao>()

    @Before
    fun init() {
        every { recordsDao.getAllRecords() } returns fakeFlow()
        every { recordsDao.loadAllRecordsAfterThan(any()) } returns fakeFlow()
        every { recordsDao.loadAllRecordsBeforeThan(any()) } returns fakeFlow()
        every { recordsDao.loadAllRecordsBetweenDates(any(), any()) } returns fakeFlow()
        every { recordsDao.returnTheSumOfAllIncome() } returns fakeFlow()
        every { recordsDao.returnTheSumOfIncomeAfterThan(any()) } returns fakeFlow()
        every { recordsDao.returnTheSumOfIncomeBeforeThan(any()) } returns fakeFlow()
        every { recordsDao.returnTheSumOfIncomeBetweenDates(any(), any()) } returns fakeFlow()
        every { recordsDao.returnTheSumOfAllExpenses() } returns fakeFlow()
        every { recordsDao.returnTheSumOfExpensesAfterThan(any()) } returns fakeFlow()
        every { recordsDao.returnTheSumOfExpensesBeforeThan(any()) } returns fakeFlow()
        every { recordsDao.returnTheSumOfExpensesBetweenDates(any(), any()) } returns fakeFlow()
    }

    fun <T> fakeFlow(): Flow<T> = flow {}

    @Test
    fun testFun_getRecords() {
        recordsDao.getRecords(null, null)
        verify { recordsDao.getAllRecords() }
        recordsDao.getRecords()
        verify { recordsDao.getAllRecords() }
        recordsDao.getRecords(minDate = 3)
        verify { recordsDao.loadAllRecordsAfterThan(3) }
        recordsDao.getRecords(maxDate = 8)
        verify { recordsDao.loadAllRecordsBeforeThan(8) }
        recordsDao.getRecords(minDate = 5, maxDate = 10)
        verify { recordsDao.loadAllRecordsBetweenDates(5, 10) }
    }

    @Test
    fun testFun_getSumOfIncome() {
        recordsDao.getSumOfIncome(null, null)
        verify { recordsDao.returnTheSumOfAllIncome() }
        recordsDao.getSumOfIncome()
        verify { recordsDao.returnTheSumOfAllIncome() }
        recordsDao.getSumOfIncome(minDate = 3)
        verify { recordsDao.returnTheSumOfIncomeAfterThan(3) }
        recordsDao.getSumOfIncome(maxDate = 8)
        verify { recordsDao.returnTheSumOfIncomeBeforeThan(8) }
        recordsDao.getSumOfIncome(minDate = 5, maxDate = 10)
        verify { recordsDao.returnTheSumOfIncomeBetweenDates(5, 10) }
    }

    @Test
    fun testFun_getSumOfExpenses() {
        recordsDao.getSumOfExpenses(null, null)
        verify { recordsDao.returnTheSumOfAllExpenses() }
        recordsDao.getSumOfExpenses()
        verify { recordsDao.returnTheSumOfAllExpenses() }
        recordsDao.getSumOfExpenses(minDate = 3)
        verify { recordsDao.returnTheSumOfExpensesAfterThan(3) }
        recordsDao.getSumOfExpenses(maxDate = 8)
        verify { recordsDao.returnTheSumOfExpensesBeforeThan(8) }
        recordsDao.getSumOfExpenses(minDate = 5, maxDate = 10)
        verify { recordsDao.returnTheSumOfExpensesBetweenDates(5, 10) }
    }*/
}