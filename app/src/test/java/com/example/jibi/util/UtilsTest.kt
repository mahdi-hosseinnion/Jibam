package com.example.jibi.util

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class UtilsTest {

    @Test
    fun convertFarsiDigitsToEnglishDigits() {
        val testCase = "۱۲۳۴۵۶۷۸۹۰"
        assertEquals("1234567890", testCase.convertFarsiDigitsToEnglishDigits())
        val testCase2 = "۱۲۳۴8۵8*۶۷۸۹۰+"
        assertEquals("1234858*67890+", testCase2.convertFarsiDigitsToEnglishDigits())
        val testCase3 = "1234567890"
        assertEquals("1234567890", testCase3.convertFarsiDigitsToEnglishDigits())
        val testCase4 = "12345۱۲۳۴67890"
        assertEquals("12345123467890", testCase4.convertFarsiDigitsToEnglishDigits())
    }

    @Test
    fun convertEnglishDigitsToFarsiDigits() {
        val testCase = "1234567890"
        assertEquals("۱۲۳۴۵۶۷۸۹۰", testCase.convertEnglishDigitsToFarsiDigits())
        val testCase2 = "1234۸5۸*67890+"
        assertEquals("۱۲۳۴۸۵۸*۶۷۸۹۰+", testCase2.convertEnglishDigitsToFarsiDigits())
        val testCase4 = "۱۲۳۴۵123۶۷۸۹۰"
        assertEquals("۱۲۳۴۵۱۲۳۶۷۸۹۰", testCase4.convertEnglishDigitsToFarsiDigits())
    }
}