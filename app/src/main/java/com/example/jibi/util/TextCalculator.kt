package com.example.jibi.util

import android.util.Log
import com.example.jibi.ui.CalculatorKeyboard.Companion.DIVISION
import com.example.jibi.ui.CalculatorKeyboard.Companion.MINES
import com.example.jibi.ui.CalculatorKeyboard.Companion.PLUS
import com.example.jibi.ui.CalculatorKeyboard.Companion.TIMES

//TODO divide by 0?
class TextCalculator {

    companion object {
        private const val TAG = "TextCalculator"
        private const val GENERIC_ERROR = "Unknown error :/"
        private const val OUT_OF_RANGE_ERROR = "this number is too big"
        private const val ERROR_EMPTY_STRING = "EmptyString"
        val operatorSymbols = listOf(TIMES, DIVISION, PLUS, MINES)

    }

    fun calculateResult(value1: String): String {
        var value = value1
        value = value.convertFarsiDigitsToEnglishDigits()//convert farsi digits to english if needed
        try {


            Log.d(TAG, "calculateResult: method called with v: $value")
            var result = ignoreLastIndexOperator(value)

            //Multiplication
            var indexOfOperator = result.indexOf(TIMES)
            while (indexOfOperator > 0) { //while there is time operator symbol in our text

                result = mathReplacer(
                    value = result,
                    operatorIndex = indexOfOperator,
                    mathResult = { x, y -> x.times(y) })
                //reset condition for loop
                indexOfOperator = result.indexOf(TIMES)
            }
            //Division
            indexOfOperator = result.indexOf(DIVISION)
            while (indexOfOperator > 0) { //while there is division operator symbol in our text

                result = mathReplacer(
                    value = result,
                    operatorIndex = indexOfOperator,
                    mathResult = { x, y -> x.div(y) })
                //reset condition for loop
                indexOfOperator = result.indexOf(DIVISION)
            }
            //Addition
            indexOfOperator = result.indexOf(PLUS)
            while (indexOfOperator > 0) { //while there is plus operator symbol in our text

                result = mathReplacer(
                    value = result,
                    operatorIndex = indexOfOperator,
                    mathResult = { x, y -> x.plus(y) })
                //reset condition for loop
                indexOfOperator = result.indexOf(PLUS)
            }
            //Subtraction
            indexOfOperator = result.indexOf(MINES)
            while (indexOfOperator > 0) { //while there is minus operator symbol in our text

                result = mathReplacer(
                    value = result,
                    operatorIndex = indexOfOperator,
                    mathResult = { x, y -> x.minus(y) })
                //reset condition for loop
                indexOfOperator = result.indexOf(MINES)
            }
            if (result.contains(GENERIC_ERROR)) {
                return GENERIC_ERROR
            }
            if (result.contains(OUT_OF_RANGE_ERROR) || result.contains(ERROR_EMPTY_STRING)) {
                return result
            }
            return result
        } catch (e: Exception) {
            Log.e(TAG, "calculateResult: ${e.message}", e)
            return GENERIC_ERROR
        }

    }

    private fun ignoreLastIndexOperator(value: String): String {
        //this method delete the last operator
        val lastChar = value[value.lastIndex].toString()
        val firstChar = value[0].toString()
        var result = value
        if (
            lastChar == TIMES ||
            lastChar == DIVISION ||
            lastChar == PLUS ||
            lastChar == MINES
        ) {
            //remove it from string
            result = result.removeRange(value.length.minus(1), value.length)
        }
        if (
            firstChar == TIMES ||
            firstChar == DIVISION ||
            firstChar == PLUS ||
            firstChar == MINES
        ) {
            //remove it from string
            result = result.removeRange(0, 1)
        }
        return result
    }

    private fun mathReplacer(
        value: String,
        operatorIndex: Int,
        mathResult: (Double, Double) -> Double
    ): String {
        try {


            Log.d(
                TAG,
                "mathReplacer: value: $value and operatorIndex: $operatorIndex and math: ${
                    mathResult(
                        4.0,
                        2.0
                    )
                }"
            )

            val previousOperatorIndex = value.lastIndexOfAny(
                strings = operatorSymbols,
                startIndex = operatorIndex.minus(1)
            )
            val nextOperatorIndex = value.indexOfAny(
                strings = operatorSymbols,
                startIndex = operatorIndex.plus(1)
            )
            //check for -1 (does not exist in )
            val firstNumber = if (previousOperatorIndex < 0) {
                value.substring(0, operatorIndex)
            } else {
                value.substring(previousOperatorIndex.plus(1), operatorIndex)
            }.toDouble()
            //check for -1
            val secondNumber = if (nextOperatorIndex < 0) {
                value.substring(operatorIndex.plus(1))
            } else {
                value.substring(operatorIndex.plus(1), nextOperatorIndex)
            }.toDouble()

            return if (nextOperatorIndex < 0) {
                value.replaceRange(
                    previousOperatorIndex.plus(1),
                    value.length,
                    mathResult(firstNumber, secondNumber).toString()
                )
            } else {
                value.replaceRange(
                    previousOperatorIndex.plus(1),
                    nextOperatorIndex,
                    mathResult(firstNumber, secondNumber).toString()
                )
            }
        } catch (outOfRangeException: NumberFormatException) {
            if (outOfRangeException.message?.contains("empty String") == true) {
                return ERROR_EMPTY_STRING
            }
            return OUT_OF_RANGE_ERROR + ": ${outOfRangeException.message}"
        } catch (e: Exception) {
            return GENERIC_ERROR
        }
    }

    private fun Int.isPositive(): Boolean = this >= 0
}