package com.example.jibi.ui

import android.util.Log
import com.example.jibi.ui.CalculatorKeyboard.Companion.DIVISION
import com.example.jibi.ui.CalculatorKeyboard.Companion.MINES
import com.example.jibi.ui.CalculatorKeyboard.Companion.PLUS
import com.example.jibi.ui.CalculatorKeyboard.Companion.TIMES
import com.example.jibi.util.TextCalculator
import io.mockk.every
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.*
import kotlin.random.Random

class TextCalculatorTest {

    //system under test
    var textCalculator: TextCalculator? = null

    val maxRandomNumber = 10.0
    val minRandomNumber = 1.0//should not be 0

    @BeforeEach
    fun before() {
        textCalculator = TextCalculator()
        //mock log.e for flow.catch in asDataState()
        mockkStatic(Log::class)
        every { Log.e(any(), any(), any()) } answers {
            println("Log.e from ${this.args[0]}, Message: ${this.args[1]}, Throwable: ${this.args[2]}")
            0
        }
        every { Log.d(any(), any()) } answers {
            println("Log.e from ${this.args[0]}, Message: ${this.args[1]}")
            0
        }
    }

    @AfterEach
    fun after() {
        textCalculator = null
        unmockkAll()
    }

    @Test
    fun test_Time_Operator_success() = runBlocking {
        val randomX = Random.nextDouble(minRandomNumber, maxRandomNumber)
        val randomY = Random.nextDouble(minRandomNumber, maxRandomNumber)
        val textForCalculate = "$randomX${CalculatorKeyboard.TIMES}$randomY"
        val expectedResult = randomX.times(randomY)

        val actualResult = textCalculator!!.calculateResult(textForCalculate)

        println("expect: $expectedResult")
        println("actual: $actualResult")
        assertEquals(expectedResult, actualResult)
    }

    @Test
    fun test_Division_Operator_success() = runBlocking {
        val randomX = Random.nextDouble(minRandomNumber, maxRandomNumber)
        val randomY = Random.nextDouble(minRandomNumber, maxRandomNumber)
        val textForCalculate = "$randomX${CalculatorKeyboard.DIVISION}$randomY"
        val expectedResult = randomX.div(randomY)

        val actualResult = textCalculator!!.calculateResult(textForCalculate)

        println("expect: $expectedResult")
        println("actual: $actualResult")
        assertEquals(expectedResult, actualResult)
    }

    @Test
    fun test_Plus_Operator_success() = runBlocking {
        val randomX = Random.nextDouble(minRandomNumber, maxRandomNumber)
        val randomY = Random.nextDouble(minRandomNumber, maxRandomNumber)

        val textForCalculate = "$randomX${CalculatorKeyboard.PLUS}$randomY"
        val expectedResult = randomX.plus(randomY)

        val actualResult = textCalculator!!.calculateResult(textForCalculate)

        println("expect: $expectedResult")
        println("actual: $actualResult")
        assertEquals(expectedResult, actualResult)
    }

    @Test
    fun test_Minus_Operator_success() = runBlocking {
        val randomX = Random.nextDouble(minRandomNumber, maxRandomNumber)
        val randomY = Random.nextDouble(minRandomNumber, maxRandomNumber)
        val textForCalculate = "$randomX${CalculatorKeyboard.MINES}$randomY"
        val expectedResult = randomX.minus(randomY)

        val actualResult = textCalculator!!.calculateResult(textForCalculate)

        println("expect: $expectedResult")
        println("actual: $actualResult")
        assertEquals(expectedResult, actualResult)
    }

    @RepeatedTest(value = 100)
    fun test_Complex_Operator_success() = runBlocking {
        val A = Random.nextDouble(minRandomNumber, maxRandomNumber)
        val B = Random.nextDouble(minRandomNumber, maxRandomNumber)
        val C = Random.nextDouble(minRandomNumber, maxRandomNumber)
        val X = Random.nextDouble(minRandomNumber, maxRandomNumber)
        val Y = Random.nextDouble(minRandomNumber, maxRandomNumber)
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
                textForCalculate ="$A $TIMES $B $DIVISION $C $PLUS $X"
                    expectedResult =(A.times(B)).div(C).plus(X)
            }
            3 -> {
                textForCalculate ="$X $PLUS $Y $MINES $A $PLUS $B $PLUS $C"
                    expectedResult = (X.plus(Y)).minus(A.plus(B).plus(C))
            }
            4 -> {
                textForCalculate ="$X $TIMES $Y $TIMES $A $TIMES $B $TIMES $C"
                    expectedResult =X.times(Y).times(A).times(B).times(C)
            }
            5 -> {
                textForCalculate ="$X $DIVISION $Y $DIVISION $A $DIVISION $B $DIVISION $C"
                    expectedResult =X.div(Y).div(A).div(B).div(C)
            }
            6 -> {
                textForCalculate ="$X $PLUS $Y $PLUS $A $PLUS $B $PLUS $C"
                    expectedResult =X.plus(Y).plus(A).plus(B).plus(C)
            }
            6 -> {
                textForCalculate ="$X $MINES $Y $MINES $A $MINES $B $MINES $C"
                    expectedResult =X.minus(Y).minus(A).minus(B).minus(C)
            }
        }

        textForCalculate = textForCalculate.replace(" ", "").replace(")", "").replace("(", "")

        val actualResult = textCalculator!!.calculateResult(textForCalculate)
        println("expect: $expectedResult")
        println("actual: $actualResult")
        assertEquals(expectedResult, actualResult)
    }

}