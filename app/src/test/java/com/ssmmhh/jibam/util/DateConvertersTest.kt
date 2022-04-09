package com.ssmmhh.jibam.util

import com.ssmmhh.jibam.data.model.GregorianDateHolder
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.util.*

class DateConvertersTest {

    @Test
    fun shouldReturnDatesUnixTime_whenWeGiveItADate() {
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
}