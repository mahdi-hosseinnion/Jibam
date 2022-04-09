package com.ssmmhh.jibam.util

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

class DateUtilsTest {

/*    @ParameterizedTest
    @ValueSource(
        strings = [
            //input      | expected
            "1375-06-12  | 841606200",
            "1410-09-26  | 1955219400",
            "1389-06-12  | 1283455800",
            "1399-04-24  | 1594668600",
            "1400-04-22  | 1626118200",
            "1375-12-12  | 857248200",
            "1384-08-15  | 1131222600",
            "1394-01-27  | 1429126200",
        ]
    )
    fun test_shamsi_to_unixTimeStamp(value: String) {
        val input = value.substring(0, value.indexOf('|'))
        val expected = value.substring(value.indexOf('|').plus(2))
        val year = input.substring(0, 4).toInt()
        val month = input.substring(5, 7).toInt()
        val day = input.substring(8, 10).toInt()
        assertEquals(
            expected,
            DateUtils.(year, month, day).div(1_000).toString()
        )
    }*/

}