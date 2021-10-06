package com.ssmmhh.jibam.util

import android.content.res.Resources
import com.ssmmhh.jibam.util.SolarCalendar.ShamsiPatterns.TEST
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import java.text.SimpleDateFormat
import java.util.*

//sources
// https://programchi.ir/2018/04/27/%D8%A2%D9%85%D9%88%D8%B2%D8%B4-%D8%AA%D8%A7%D8%B1%DB%8C%D8%AE-%D8%B4%D9%85%D8%B3%DB%8C-%D8%AF%D8%B1-%D8%A8%D8%B1%D9%86%D8%A7%D9%85%D9%87-%D9%86%D9%88%DB%8C%D8%B3%DB%8C-%D8%A7%D9%86%D8%AF%D8%B1%D9%88/
class SolarCalendarTest {
    private val testLocale = Locale("en", "IR")
    private val resources = Resources.getSystem()
    //system under test
    val solarCalender = SolarCalendar

    val testCases = mapOf<Long, String>(//the unix time should be second
        3728817042 to "1466/12/09/اسفند/شنبه",
        3870941442 to "1471/06/09/شهريور/شنبه",
        683878683L to "1370/06/12/شهريور/سه شنبه",
        4439534407 to "1489/06/16/شهريور/يکشنبه",
        723297607L to "1371/09/11/آذر/چهارشنبه",
        1708695597L to "1402/12/04/اسفند/جمعه",
        1725345681L to "1403/06/13/شهريور/سه شنبه",
        1754721681L to "1404/05/18/مرداد/شنبه",
        1916055021L to "1409/06/28/شهريور/پنج شنبه",
        1852983021L to "1407/06/29/شهريور/سه شنبه",
        1646663421L to "1400/12/16/اسفند/دوشنبه",
        //WRONG ONES
        1884519021L to "1408/06/29/شهریور/چهارشنبه"//۱۴۰۸/۰۶/۲۹
    )

    @ParameterizedTest
    @ValueSource(
        longs = [3728817042, 3870941442, 683878683L, 4439534407, 723297607L, 1708695597L, 1725345681L, 1754721681L,
            1916055021L,
            1852983021L,
            1646663421L,
            //WRONG ONES
            1884519021L]
    )
    fun testSolarCalender(k: Long) {

        val unixDate = k.times(1000L)

        val date = Date(unixDate)
        val actualResult1 = solarCalender.calcSolarCalendar(date, TEST,resources,testLocale)
        val actualResult2 = solarCalender.calcSolarCalendar(unixDate,TEST,resources,testLocale)
        println("CHECK $k")
        println(
            "Georgian date -> " +
                    SimpleDateFormat("yyyy-MM-dd--HH--mm").format(date)
        )
        println(
            "Jalali date -> " +
                    solarCalender.calcSolarCalendar(unixDate,TEST,resources,testLocale)
        )

        assertEquals(
            testCases[k], actualResult1
        )
        assertEquals(
            testCases[k], actualResult2
        )


    }
}
