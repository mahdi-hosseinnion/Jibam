package com.ssmmhh.jibam.util

import com.ssmmhh.jibam.data.model.GregorianDateHolder
import com.ssmmhh.jibam.data.model.SolarHijriDateHolder
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.util.*

class DateConvertersTest {

    @Test
    fun convertGregorianDateToUnixTime_shouldReturnDatesUnixTime_whenWeGiveItADate() {
        //Arrange
        /**
         * Map GregorianDateHolder time to corresponding unix time in seconds.
         * Unix time converter source: epochconverter.com
         * TimeZone:  GMT+0000
         */
        val timeZone = TimeZone.getTimeZone("GMT")
        val testValues = mapOf<GregorianDateHolder, Long>(
            GregorianDateHolder(year = 2022, month = 4, day = 9) to 1649462400,
            GregorianDateHolder(year = 2038, month = 10, day = 9) to 2170195200,
            GregorianDateHolder(year = 2010, month = 1, day = 19) to 1263859200,
            GregorianDateHolder(year = 2000, month = 11, day = 19) to 974592000,
        )

        //Act and assert
        testValues.forEach {
            val actualResult = convertGregorianDateToUnixTime(it.key, timeZone)
            assertEquals(it.value, actualResult)
        }
    }

    @Test
    fun convertSolarHijriToGregorian_shouldReturnGregorianDateCorrespondingToSolarHijriDate() {
        //Arrange
        /**
         * Map SolarHijriDate time to corresponding GregorianDate.
         * Data source: time.ir
         */
        val testData = mapOf(
            //A common year.
            SolarHijriDateHolder(1401, 1, 20) to GregorianDateHolder(2022, 4, 9),
            //A leap year.
            SolarHijriDateHolder(1399, 11, 3) to GregorianDateHolder(2021, 1, 22),
            //A 5 year leap.
            SolarHijriDateHolder(1408, 8, 15) to GregorianDateHolder(2029, 11, 5),
            //A far away year in future.
            SolarHijriDateHolder(1415, 12, 29) to GregorianDateHolder(2037, 3, 19),
            //A far away year past future.
            SolarHijriDateHolder(1380, 1, 3) to GregorianDateHolder(2001, 3, 23),
        )
        //Act and assert
        testData.forEach {
            val actualResult = convertSolarHijriToGregorian(it.key)
            assertEquals(it.value.year, actualResult.year)
            assertEquals(it.value.month, actualResult.month)
            assertEquals(it.value.day, actualResult.day)
        }
    }
}