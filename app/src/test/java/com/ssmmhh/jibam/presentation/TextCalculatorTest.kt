package com.ssmmhh.jibam.presentation

import android.util.Log
import com.ssmmhh.jibam.presentation.addedittransaction.CalculatorKeyboard
import com.ssmmhh.jibam.presentation.addedittransaction.CalculatorKeyboard.Companion.DIVISION
import com.ssmmhh.jibam.presentation.addedittransaction.CalculatorKeyboard.Companion.MINES
import com.ssmmhh.jibam.presentation.addedittransaction.CalculatorKeyboard.Companion.PLUS
import com.ssmmhh.jibam.presentation.addedittransaction.CalculatorKeyboard.Companion.TIMES
import com.ssmmhh.jibam.util.TextCalculator
import io.mockk.every
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.*
import java.text.DecimalFormat
import java.text.NumberFormat
import java.util.*
import kotlin.random.Random

class TextCalculatorTest {

    //system under test
    var textCalculator: TextCalculator? = null

    val maxRandomNumber = 1_000_000L
    val minRandomNumber = 1L//should not be 0


    @BeforeEach
    fun before() {
        textCalculator = TextCalculator()
        Double.MAX_VALUE
        //mock log.e for flow.catch in asDataState()
        mockkStatic(Log::class)
        every { Log.e(any(), any(), any()) } answers {
            println("Log.e from ${this.args[0]}, Message: ${this.args[1]}, Throwable: ${this.args[2]}")
            0
        }
        every { Log.d(any(), any()) } answers {
            println("Log.d from ${this.args[0]}, Message: ${this.args[1]}")
            0
        }
    }

    @AfterEach
    fun after() {
        textCalculator = null
        unmockkAll()
    }

    @RepeatedTest(value = 10)
    fun test_Time_Operator_success() = runBlocking {
        val randomX = Random.nextLong(minRandomNumber, maxRandomNumber).convertToRandomDouble()
        val randomY = Random.nextLong(minRandomNumber, maxRandomNumber).convertToRandomDouble()
        val textForCalculate = "$randomX${CalculatorKeyboard.TIMES}$randomY"
        val expectedResult = randomX.times(randomY)

        val actualResult = textCalculator!!.calculateResult(textForCalculate)

        println("expect: $expectedResult")
        println("actual: $actualResult")
        assertEquals(expectedResult.toString(), actualResult)
    }

    @RepeatedTest(value = 10)
    fun test_Division_Operator_success() = runBlocking {
        val randomX = Random.nextLong(minRandomNumber, maxRandomNumber).convertToRandomDouble()
        val randomY = Random.nextLong(minRandomNumber, maxRandomNumber).convertToRandomDouble()
        val textForCalculate = "$randomX${CalculatorKeyboard.DIVISION}$randomY"
        val expectedResult = randomX.div(randomY)

        val actualResult = textCalculator!!.calculateResult(textForCalculate)

        println("expect: $expectedResult")
        println("actual: $actualResult")
        assertEquals(expectedResult.toString(), actualResult)
    }

    @RepeatedTest(value = 10)
    fun test_Plus_Operator_success() = runBlocking {
        val randomX = Int.MAX_VALUE.toLong().convertToRandomDouble()
        val randomY = Int.MAX_VALUE.minus(1).toLong().convertToRandomDouble()
        val textForCalculate = "$randomX${CalculatorKeyboard.PLUS}$randomY"
        val expectedResult = randomX.plus(randomY)

        val actualResult = textCalculator!!.calculateResult(textForCalculate)

        println("expect: $expectedResult")
        println("actual: $actualResult")
        assertEquals(expectedResult.toString(), actualResult)
    }

    @RepeatedTest(value = 10)
    fun test_Minus_Operator_success() = runBlocking {
        val randomX = Random.nextLong(minRandomNumber, maxRandomNumber).convertToRandomDouble()
        val randomY = Random.nextLong(minRandomNumber, maxRandomNumber).convertToRandomDouble()
        val textForCalculate = "$randomX${CalculatorKeyboard.MINES}$randomY"
        val expectedResult = randomX.minus(randomY)

        val actualResult = textCalculator!!.calculateResult(textForCalculate)

        println("expect: $expectedResult")
        println("actual: $actualResult")
        assertEquals(expectedResult.toString(), actualResult)
    }

    @RepeatedTest(value = 100)
    fun test_Complex_Operator_success() = runBlocking {
        val A = Random.nextLong(minRandomNumber, maxRandomNumber).convertToRandomDouble()
        val B = Random.nextLong(minRandomNumber, maxRandomNumber).convertToRandomDouble()
        val C = Random.nextLong(minRandomNumber, maxRandomNumber).convertToRandomDouble()
        val X = Random.nextLong(minRandomNumber, maxRandomNumber).convertToRandomDouble()
        val Y = Random.nextLong(minRandomNumber, maxRandomNumber).convertToRandomDouble()
        val RANDOMONE = Random.nextInt(10)
        //make random complex math
        var textForCalculate = "$A $PLUS $B $TIMES $C $DIVISION $X $MINES $Y"

        var expectedResult = A.plus(B.times(C).div(X)).minus(Y)

        when (RANDOMONE) {
            1 -> {
                textForCalculate = "$A $TIMES $B $PLUS $C $DIVISION $X $MINES $Y"
                expectedResult = A.times(B).plus(C.div(X)).minus(Y)
            }
            2 -> {
                textForCalculate = "$A $TIMES $B $DIVISION $C $PLUS $X"
                expectedResult = (A.times(B)).div(C).plus(X)
            }
            3 -> {
                textForCalculate = "$X $PLUS $Y $MINES $A $PLUS $B $PLUS $C"
                expectedResult = (X.plus(Y)).minus(A.plus(B).plus(C))
            }
            4 -> {
                textForCalculate = "$X $TIMES $Y $TIMES $A $TIMES $B $TIMES $C"
                expectedResult = X.times(Y).times(A).times(B).times(C)
            }
            5 -> {
                textForCalculate = "$X $DIVISION $Y"
                expectedResult = X.div(Y)
            }
            6 -> {
                textForCalculate = "$X $PLUS $Y $PLUS $A $PLUS $B $PLUS $C"
                expectedResult = X.plus(Y).plus(A).plus(B).plus(C)
            }
            6 -> {
                textForCalculate = "$X $MINES $Y $MINES $A $MINES $B $MINES $C"
                expectedResult = X.minus(Y).minus(A).minus(B).minus(C)
            }
        }

        textForCalculate = textForCalculate.replace(" ", "").replace(")", "").replace("(", "")

        val actualResult = textCalculator!!.calculateResult(textForCalculate)
        println("expect: $expectedResult")
        println("actual: $actualResult")
        assertEquals(expectedResult.toString(), actualResult)
    }

    @Test
    fun wiredOperatorPlacement_Plus() {
        val randomX = Random.nextLong(minRandomNumber, maxRandomNumber).convertToRandomDouble()
        val randomY = Random.nextLong(minRandomNumber, maxRandomNumber).convertToRandomDouble()
        var textForCalculate = "$PLUS $randomX${PLUS}$randomY $PLUS "
        textForCalculate = textForCalculate.replace(" ", "").replace(")", "").replace("(", "")

        val expectedResult = randomX.plus(randomY)

        val actualResult = textCalculator!!.calculateResult(textForCalculate)

        println("expect: $expectedResult")
        println("actual: $actualResult")
        assertEquals(expectedResult.toString(), actualResult)
    }

    @Test
    fun wiredOperatorPlacement_Minus() {
        val randomX = Random.nextLong(minRandomNumber, maxRandomNumber).convertToRandomDouble()
        val randomY = Random.nextLong(minRandomNumber, maxRandomNumber).convertToRandomDouble()
        var textForCalculate = "$MINES $randomX${MINES}$randomY $MINES "
        textForCalculate = textForCalculate.replace(" ", "").replace(")", "").replace("(", "")

        val expectedResult = randomX.minus(randomY)

        val actualResult = textCalculator!!.calculateResult(textForCalculate)

        println("expect: $expectedResult")
        println("actual: $actualResult")
        assertEquals(expectedResult.toString(), actualResult)
    }

    private fun Long.convertToRandomDouble(): Double {
        return this.minus(
            when (Random.nextInt(8)) {
                1 -> {
                    0.25
                }
                2 -> {
                    0.50
                }
                3 -> {
                    0.75
                }
                4 -> {
                    0.20
                }
                5 -> {
                    0.55
                }
                6 -> {
                    0.70
                }
                7 -> {
                    0.80
                }
                else -> {
                    1.0
                }
            }
        )
    }

    private fun separate3By3AndRoundIt(money: Double): String {
        Log.d("aa", "separate3By3AndRoundIt: start with $money")

        //seprate 3 by 3 part
        val formatter: DecimalFormat = NumberFormat.getInstance(Locale.US) as DecimalFormat
        formatter.applyPattern("#,###,###,###.###")
        val finalResult = formatter.format(money)
        Log.d(
            "aa",
            "separate3By3AndRoundIt: final Result = $finalResult + ${finalResult.indexOf('.')}"
        )
        if ((finalResult.indexOf('.')) == -1) {
            return finalResult
        }

        //round part
        if (finalResult.substring(finalResult.lastIndex.minus(1)) == ".0") {
            Log.d("aa", "separate3By3AndRoundIt: first shart")
            //convert 15.0 to 15
            return finalResult.substring(
                startIndex = 0,
                endIndex = finalResult.lastIndex.minus(1)
            )
        }

        val periodPosition = finalResult.indexOf('.')

        return if (periodPosition > -1 && periodPosition.plus(3) < finalResult.length) {
            Log.d("aa", "separate3By3AndRoundIt second shart")
            //convert 19.23423424 to 19.23
            finalResult.substring(0, periodPosition.plus(3))
        } else {
            Log.d("aa", "separate3By3AndRoundIt third shart shart")
            finalResult
        }

    }

    @Test
    fun testSeparator() {
        val testCases = listOf<Double>( 12.5, 12.546885, 13.98, -12.5, -12.546885, -13.98)
        val expectedValue = listOf<Double>( 12.5, 12.54, 13.98, -12.5, -12.54, -13.98)
        for (i in testCases.indices.minus(1)) {
            assertEquals(expectedValue[i].toString(), separate3By3AndRoundIt(testCases[i]))
        }
        //last one
        assertEquals("12", separate3By3AndRoundIt(12.0))
        assertEquals("0", separate3By3AndRoundIt(0.0))
        assertEquals("1", separate3By3AndRoundIt(1.0))
        assertEquals("-1", separate3By3AndRoundIt(-1.0))
        //grater then 999 number
        assertEquals("12,000", separate3By3AndRoundIt(12000.0))
        assertEquals("10,000.16", separate3By3AndRoundIt(10000.16426))
        assertEquals("100,000.5", separate3By3AndRoundIt(100000.5))
        assertEquals("100,000.73", separate3By3AndRoundIt(100000.73))
        //manfi ha
        assertEquals("-12,000", separate3By3AndRoundIt(-12000.0))
        assertEquals("-10,000.16", separate3By3AndRoundIt(-10000.16426))
        assertEquals("-100,000.5", separate3By3AndRoundIt(-100000.5))
        assertEquals("-100,000.73", separate3By3AndRoundIt(-100000.73))
        assertEquals("-354.4", separate3By3AndRoundIt(-354.3999999999942))

    }
}