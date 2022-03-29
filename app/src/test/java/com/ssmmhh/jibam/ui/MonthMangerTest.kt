package com.ssmmhh.jibam.ui

import android.content.Context
import android.content.SharedPreferences
import android.content.res.Resources
import androidx.test.core.app.ApplicationProvider
import com.ssmmhh.jibam.ui.main.transaction.feature_common.MonthManger
import com.ssmmhh.jibam.util.DateUtils.gregorianToUnixTimestamp
import com.ssmmhh.jibam.util.DateUtils.shamsiToUnixTimeStamp
import com.ssmmhh.jibam.util.PreferenceKeys
import com.ssmmhh.jibam.util.mahdiLog
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import java.util.*


class MonthMangerTest {
    private val TAG = "MonthMangerTest"

    private val testLocale = Locale("en", "IR")

    //system under test
    val sharedPref: SharedPreferences =
        ApplicationProvider.getApplicationContext<Context>()?.getSharedPreferences(
            PreferenceKeys.APP_MAIN_PREFERENCES, Context.MODE_PRIVATE
        )!!
    private val monthManager = MonthManger(testLocale, Resources.getSystem(), sharedPref)

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
        val testValue = gregorianToUnixTimestamp(input)
        val expectedValue = gregorianToUnixTimestamp(expected)

        val inputMonth = input.substring(3, 5)
        val inputYear = input.substring(6, 10)
        print(" input month: $inputMonth \n input year: $inputYear \n")
        Assertions.assertEquals(
            expectedValue,
            monthManager.getStartOfCurrentMonthGeorgian(
                inputMonth.toInt(), inputYear.toInt()
            )
        )
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
    fun testGetEndOfCurrentMonthGeorgian(value1: String) {
        val input = value1.substring(0, value1.indexOf('|'))
        val expected = value1.substring(value1.indexOf('|').plus(1))

        mahdiLog(TAG, "test is: $input and expected result is $expected")
        val testValue = gregorianToUnixTimestamp(input)
        val expectedValue = gregorianToUnixTimestamp(expected)
        val inputMonth = input.substring(3, 5)
        val inputYear = input.substring(6, 10)
        print(" input month: $inputMonth \n input year: $inputYear \n")
        Assertions.assertEquals(
            expectedValue,
            monthManager.getEndOfCurrentMonthGeorgian(inputMonth.toInt(), inputYear.toInt())
        )
        Assertions.assertEquals(
            expectedValue,
            monthManager.getEndOfCurrentMonthGeorgian(testValue)
        )

    }

    @ParameterizedTest
    @ValueSource(
        strings = [
            //input      | expected
            //different digits
            "12/10/1400  | 01/10/1400",
            "08/05/1399  | 01/05/1399",
            "12/09/1398  | 01/09/1398",
            "08/11/1395  | 01/11/1395",
            "29/12/1382  | 01/12/1382",
            //different month day count 31/30/29
            "31/01/1384  | 01/01/1384",//31
            "31/03/1405  | 01/03/1405", //31
            "01/05/1405  | 01/05/1405", //31

            "30/07/1401  | 01/07/1401",//30
            "30/11/1378  | 01/11/1378",//30
            "01/09/1387  | 01/09/1387",//30=---------------

            "29/12/1402  | 01/12/1402", //29
            "29/12/1409  | 01/12/1409",//29
            "01/12/1392  | 01/12/1392",//29

            "30/12/1399  | 01/12/1399", //30 kabise
            "30/12/1391  | 01/12/1391", //30 kabise
            "01/12/1403  | 01/12/1403", //30 kabise
            "12/12/1403  | 01/12/1403", //30 kabise
            //fist month of year
            "08/01/1381  | 01/01/1381",
            "20/01/1382  | 01/01/1382",
            "31/01/1374  | 01/01/1374",
            //last month of year
            "08/12/1397  | 01/12/1397",
            "20/12/1394  | 01/12/1394",
            "29/12/1398  | 01/12/1398",
        ]
    )
    fun testGetStartOfCurrentMonthShamsi(value1: String) {
        val input = value1.substring(0, value1.indexOf('|'))
        val expected = value1.substring(value1.indexOf('|').plus(1))

        mahdiLog(TAG, "test is: $input and expected result is $expected")
        val inputDay = input.substring(0, 2).toInt()
        val inputMonth = input.substring(3, 5).toInt()
        val inputYear = input.substring(6, 10).toInt()

        val testValue = shamsiToUnixTimeStamp(
            inputYear, inputMonth, inputDay
        )
        val expectedDay = expected.substring(1, 3).toInt()
        val expectedMonth = expected.substring(4, 6).toInt()
        val expectedYear = expected.substring(7, 11).toInt()

        val expectedValue = shamsiToUnixTimeStamp(
            expectedYear, expectedMonth, expectedDay
        )
        println(" input year: $inputYear  month: $inputMonth day: $inputDay")
        println(" expected year: $expectedYear  month: $expectedMonth day: $expectedDay")

        Assertions.assertEquals(
            expectedValue,
            monthManager.getStartOfCurrentMonthShamsi(
                inputMonth.toInt(), inputYear.toInt()
            )
        )
        Assertions.assertEquals(
            expectedValue,
            monthManager.getStartOfCurrentMonthShamsi(testValue)
        )

    }

    @ParameterizedTest
    @ValueSource(
        strings = [
            //input      | expected
            //different digits
            "12/10/1400  | 01/11/1400",
            "08/05/1399  | 01/06/1399",
            "12/09/1398  | 01/10/1398",
            "08/11/1395  | 01/12/1395",
            "29/12/1382  | 01/01/1383",
            //different month day count 31/30/29
            "31/01/1384  | 01/02/1384",//31
            "31/03/1405  | 01/04/1405", //31
            "01/05/1405  | 01/06/1405", //31

            "30/07/1401  | 01/08/1401",//30
            "30/11/1378  | 01/12/1378",//30
            "01/09/1387  | 01/10/1387",//30=---------------

            "29/12/1402  | 01/01/1403", //29
            "29/12/1405  | 01/01/1406",//29
            "01/12/1392  | 01/01/1393",//29

            "30/12/1399  | 01/01/1400", //30 kabise
            "30/12/1391  | 01/01/1392", //30 kabise
            "01/12/1403  | 01/01/1404", //30 kabise
            "12/12/1403  | 01/01/1404", //30 kabise
            //fist month of year
            "08/01/1381  | 01/02/1381",
            "20/01/1382  | 01/02/1382",
            "31/01/1374  | 01/02/1374",
            //last month of year
            "08/12/1397  | 01/01/1398",
            "20/12/1394  | 01/01/1395",
            "29/12/1398  | 01/01/1399",
        ]
    )
    fun testGetEndOfCurrentMonthShamsi(value1: String) {
        val input = value1.substring(0, value1.indexOf('|'))
        val expected = value1.substring(value1.indexOf('|').plus(1))

        mahdiLog(TAG, "test is: $input and expected result is $expected")

        val inputDay = input.substring(0, 2).toInt()
        val inputMonth = input.substring(3, 5).toInt()
        val inputYear = input.substring(6, 10).toInt()

        val testValue = shamsiToUnixTimeStamp(
            inputYear, inputMonth, inputDay
        )
        val expectedDay = expected.substring(1, 3).toInt()
        val expectedMonth = expected.substring(4, 6).toInt()
        val expectedYear = expected.substring(7, 11).toInt()

        val expectedValue = shamsiToUnixTimeStamp(
            expectedYear, expectedMonth, expectedDay
        )

        println(" input year: $inputYear  month: $inputMonth day: $inputDay")
        println(" expected year: $expectedYear  month: $expectedMonth day: $expectedDay")
        Assertions.assertEquals(
            expectedValue,
            monthManager.getEndOfCurrentMonthShamsi(inputMonth, inputYear)
        )
        Assertions.assertEquals(
            expectedValue,
            monthManager.getEndOfCurrentMonthShamsi(testValue)
        )

    }

}