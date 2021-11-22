package com.ssmmhh.jibam.util

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.util.*


class UtilsTest {
    val testLocale = Locale("en", "US")

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

    @Test
    fun testSeparateCalculatorText3By3ForEnglishValues() {
        //a map of manually separated calculator text
        TextCalculator.Companion.operatorSymbols
        val testData = mapOf<String, String>(
            "123" to "123",
            "0" to "0",
            "123456789" to "123,456,789",
            "123+25+1-50×1÷153" to "123+25+1-50×1÷153",
            "123951+25+1000-50×1÷15003" to "123,951+25+1,000-50×1÷15,003",
            "4500+123" to "4,500+123",
            "451+1045623" to "451+1,045,623",
            "123456789-123" to "123,456,789-123",
            "1200" to "1,200",
            "112,125,354" to "112,125,354",
            "123,456+" to "123,456+",
            "123456+" to "123,456+",

            )

        for ((k, v) in testData) {
            val functionResult = separateCalculatorText3By3(k, testLocale)
            assertEquals(v, functionResult)
        }

    }

    @Test
    fun testSeparateCalculatorText3By3ForEnglishValuesPersianLang() {
        //a map of manually separated calculator text
        TextCalculator.Companion.operatorSymbols
        val testData = mapOf<String, String>(
            "۱۲۳" to "۱۲۳",
            "1234" to "۱,۲۳۴",
            "۰" to "۰",
            "۱۲۳۴۵۶۷۸۹" to "۱۲۳,۴۵۶,۷۸۹",
            "۱۲۳+۲۵+۱-۵۰×۱÷۱۵۳" to "۱۲۳+۲۵+۱-۵۰×۱÷۱۵۳",
            "۱۲۳۹۵۱+۲۵+۱۰۰۰-۵۰×۱÷۱۵۰۰۳" to "۱۲۳,۹۵۱+۲۵+۱,۰۰۰-۵۰×۱÷۱۵,۰۰۳",
            "۴۵۰۰+۱۲۳" to "۴,۵۰۰+۱۲۳",
            "۴۵۱+۱۰۴۵۶۲۳" to "۴۵۱+۱,۰۴۵,۶۲۳",
            "۱۲۳۴۵۶۷۸۹-۱۲۳" to "۱۲۳,۴۵۶,۷۸۹-۱۲۳",
            "۱۲۰۰" to "۱,۲۰۰",
            "۱۱۲,۱۲۵,۳۵۴" to "۱۱۲,۱۲۵,۳۵۴",
            "۱۲۳,۴۵۶+" to "۱۲۳,۴۵۶+",
            "۱۲۳۴۵۶+" to "۱۲۳,۴۵۶+",

            )

        for ((k, v) in testData) {
            val functionResult = separateCalculatorText3By3(k, Locale("fa"))
            assertEquals(v, functionResult)
        }

    }
}