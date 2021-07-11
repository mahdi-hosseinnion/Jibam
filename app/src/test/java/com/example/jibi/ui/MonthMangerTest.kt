package com.example.jibi.ui

import com.example.jibi.ui.main.transaction.MonthManger
import com.example.jibi.util.mahdiLog
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvFileSource
import org.junit.jupiter.params.provider.CsvSource
import org.junit.jupiter.params.provider.ValueSource
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.exp


class MonthMangerTest {
    private val TAG = "MonthMangerTest"

    private val testLocale = Locale("en", "IR")

    //system under test
    private val monthManager = MonthManger(testLocale)

    @ParameterizedTest
    @ValueSource(
        strings = [
            //input      | expected
            //different digits
            "12/10/2021  | 01/10/2021",
            "08/05/1999  | 01/05/1999",
            "12/09/2010  | 01/09/2010",
            "08/11/2015  | 01/11/2015",
            "30/12/2017  | 01/12/2017",
            //different month day count 31/30/29
            "31/03/2021  | 01/03/2021",//31
            "31/12/2021  | 01/12/2021", //31
            "01/12/2021  | 01/12/2021", //31

            "30/04/2021  | 01/04/2021",//30
            "30/11/2021  | 01/11/2021",//30
            "01/08/2021  | 01/08/2021",//30=---------------

            "29/02/2020  | 01/02/2020", //29
            "29/02/2016  | 01/02/2016",//29
            "01/02/2016  | 01/02/2016",//29

            "28/02/2021  | 01/02/2021", //28
            "28/02/2019  | 01/02/2019", //28
            "01/02/2019  | 01/02/2019", //28
            //fist month of year
            "08/01/2021  | 01/01/2021",
            "20/01/2019  | 01/01/2019",
            "31/01/2019  | 01/01/2019",
            //last month of year
            "08/12/2021  | 01/12/2021",
            "20/12/2019  | 01/12/2019",
            "31/12/2019  | 01/12/2019",
        ]
    )
    fun testGetStartOfCurrentMonthGeorgian(value1: String) {
        val input = value1.substring(0, value1.indexOf('|'))
        val expected = value1.substring(value1.indexOf('|').plus(1))

        mahdiLog(TAG, "test is: $input and expected result is $expected")
        val testValue = convertStringDateToUnixTimeStamp(input)
        val expectedValue = convertStringDateToUnixTimeStamp(expected)

        Assertions.assertEquals(
            expectedValue,
            monthManager.getStartOfCurrentMonthGeorgian(testValue)
        )

    }
    @ParameterizedTest
    @ValueSource(
        strings = [
            //input      | expected
            //different digits
            "12/10/2021  | 01/11/2021",
            "08/05/1999  | 01/06/1999",
            "12/09/2010  | 01/10/2010",
            "08/11/2015  | 01/12/2015",
            "30/12/2017  | 01/01/2018",
            //different month day count 31/30/29
            "31/03/2021  | 01/04/2021",//31
            "31/12/2021  | 01/01/2022", //31
            "01/12/2021  | 01/01/2022", //31

            "30/04/2021  | 01/05/2021",//30
            "30/11/2021  | 01/12/2021",//30
            "01/08/2021  | 01/09/2021",//30=---------------

            "29/02/2020  | 01/03/2020", //29
            "29/02/2016  | 01/03/2016",//29
            "01/02/2016  | 01/03/2016",//29

            "28/02/2021  | 01/03/2021", //28
            "28/02/2019  | 01/03/2019", //28
            "01/02/2019  | 01/03/2019", //28
            //fist month of year
            "08/01/2021  | 01/02/2021",
            "20/01/2019  | 01/02/2019",
            "31/01/2019  | 01/02/2019",
            //last month of year
            "08/12/2021  | 01/01/2022",
            "20/12/2019  | 01/01/2020",
            "31/12/2019  | 01/01/2020",
        ]
    )
    fun testGetStartOfNextMonthGeorgian(value1: String) {
        val input = value1.substring(0, value1.indexOf('|'))
        val expected = value1.substring(value1.indexOf('|').plus(1))

        mahdiLog(TAG, "test is: $input and expected result is $expected")
        val testValue = convertStringDateToUnixTimeStamp(input)
        val expectedValue = convertStringDateToUnixTimeStamp(expected)

        Assertions.assertEquals(
            expectedValue,
            monthManager.getStartOfNextMonthGeorgian(testValue)
        )

    }

    private fun convertStringDateToUnixTimeStamp(strDate: String): Long {
        val formatter: DateFormat = SimpleDateFormat("dd/MM/yyyy", testLocale)
        val date = formatter.parse(strDate) as Date
        return date.time
    }
}