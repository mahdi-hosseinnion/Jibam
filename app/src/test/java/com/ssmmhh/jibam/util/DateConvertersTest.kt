package com.ssmmhh.jibam.util

import com.ssmmhh.jibam.data.model.GregorianDateHolder
import com.ssmmhh.jibam.data.model.GregorianDateHolderWithWeekDay
import com.ssmmhh.jibam.data.model.SolarHijriDateHolder
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.Arguments.arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.*
import java.util.stream.Stream


class DateConvertersTest {
    //A constant TimeZone GMT+0000
    private val gmtTimeZone: TimeZone = TimeZone.getTimeZone("GMT")

    @ParameterizedTest
    @MethodSource("provideConvertGregorianDateToUnixTimeTestDate")
    fun convertGregorianDateToUnixTime_shouldReturnDatesUnixTime_whenWeGiveItADate(
        gregorianDate: GregorianDateHolder,
        expectedUnixTime: Long
    ) {

        //Act
        val actualResult = convertGregorianDateToUnixTime(gregorianDate, gmtTimeZone)
        //Assert
        assertEquals(expectedUnixTime, actualResult)
    }

    @ParameterizedTest
    @MethodSource("provideConvertSolarHijriToGregorianTestData")
    fun convertSolarHijriToGregorian_shouldReturnGregorianDateCorrespondingToSolarHijriDate(
        solarHijriDate: SolarHijriDateHolder,
        gregorianDate: GregorianDateHolder
    ) {
        //Act
        val actualResult = convertSolarHijriToGregorian(solarHijriDate)

        //Assert
        assertEquals(gregorianDate.year, actualResult.year)
        assertEquals(gregorianDate.month, actualResult.month)
        assertEquals(gregorianDate.day, actualResult.day)
    }

    @ParameterizedTest
    @MethodSource("provideConvertSolarHijriDateToUnixTimeTestData")
    fun convertSolarHijriDateToUnixTime_shouldReturnUnixTimeEquivalentToSolarHijriDate(
        date: SolarHijriDateHolder,
        unixTime: Long
    ) {
        //Act
        val actualResult = convertSolarHijriDateToUnixTime(
            date = date,
            timeZone = gmtTimeZone
        )
        //Assert
        assertEquals(unixTime, actualResult)
    }

    @ParameterizedTest
    @MethodSource("provideConvertUnixTimeToGregorianTestData")
    fun convertUnixTimeToGregorian_shouldReturnGregorianDateEquivalentToUnixTime(
        unixTime: Long,
        gregorianDate: GregorianDateHolderWithWeekDay
    ) {
        //Act
        val actualResult = convertUnixTimeToGregorian(
            unixTimeStamp = unixTime,
            timeZone = gmtTimeZone
        )
        //Assert
        assertEquals(gregorianDate.year, actualResult.year)
        assertEquals(gregorianDate.month, actualResult.month)
        assertEquals(gregorianDate.day, actualResult.day)
        assertEquals(gregorianDate.dayOfWeekNumber, actualResult.dayOfWeekNumber)
    }

    companion object {

        /**
         * Map GregorianDateHolder time to corresponding unix time in seconds.
         * Unix time converter source: epochconverter.com
         */
        @JvmStatic
        fun provideConvertGregorianDateToUnixTimeTestDate(): Stream<Arguments?>? = Stream.of(
            arguments(GregorianDateHolder(year = 2022, month = 4, day = 9), 1649462400),
            arguments(GregorianDateHolder(year = 2022, month = 4, day = 9), 1649462400),
            arguments(GregorianDateHolder(year = 2038, month = 10, day = 9), 2170195200),
            arguments(GregorianDateHolder(year = 2010, month = 1, day = 19), 1263859200),
            arguments(GregorianDateHolder(year = 2000, month = 11, day = 19), 974592000),
        )

        /**
         * Map SolarHijriDate time to corresponding GregorianDate.
         * Data source: time.ir
         */
        @JvmStatic
        fun provideConvertSolarHijriToGregorianTestData(): Stream<Arguments?>? = Stream.of(
            //A common year.
            arguments(
                SolarHijriDateHolder(1401, 1, 20),
                GregorianDateHolder(2022, 4, 9)
            ),
            //A leap year.
            arguments(
                SolarHijriDateHolder(1399, 11, 3),
                GregorianDateHolder(2021, 1, 22)
            ),
            //A 5 year leap.
            arguments(
                SolarHijriDateHolder(1408, 8, 15),
                GregorianDateHolder(2029, 11, 5)
            ),
            //A far away year in future.
            arguments(
                SolarHijriDateHolder(1415, 12, 29),
                GregorianDateHolder(2037, 3, 19)
            ),
            //A far away year in past.
            arguments(
                SolarHijriDateHolder(1380, 1, 3),
                GregorianDateHolder(2001, 3, 23)
            ),
        )

        /**
         * Map solar hijri date time to corresponding unix time at 00:00:00 in GMT.
         * Data source: timestamp.ir
         * Tip: timestamp.ir time zone is iran (Iran Standard Time) by default.
         */
        @JvmStatic
        fun provideConvertSolarHijriDateToUnixTimeTestData(): Stream<Arguments?>? = Stream.of(
            arguments(SolarHijriDateHolder(1401, 1, 21), 1649548800),
            arguments(SolarHijriDateHolder(1386, 11, 5), 1201219200),
            arguments(SolarHijriDateHolder(1422, 5, 9), 2321913600),
        )

        /**
         * Map unix time to corresponding gregorian time in GMT.
         * Data source: unixtimestamp.com
         */
        @JvmStatic
        fun provideConvertUnixTimeToGregorianTestData(): Stream<Arguments?>? = Stream.of(
            arguments(1649569481, GregorianDateHolderWithWeekDay(2022, 4, 10, 1)),
            arguments(1131165932, GregorianDateHolderWithWeekDay(2005, 11, 5, 7)),
            arguments(2030822103, GregorianDateHolderWithWeekDay(2034, 5, 9, 3)),
        )

    }
}