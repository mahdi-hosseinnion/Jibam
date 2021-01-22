package com.example.jibi.util

import android.util.Log
import com.example.jibi.ui.CalculatorKeyboard
import com.example.jibi.ui.CalculatorKeyboard.Companion.DIVISION
import com.example.jibi.ui.CalculatorKeyboard.Companion.MINES
import com.example.jibi.ui.CalculatorKeyboard.Companion.PLUS
import com.example.jibi.ui.CalculatorKeyboard.Companion.TIMES

//TODO divide by 0?
class TextCalculator {

    companion object {
        private const val TAG = "TextCalculator"
    }

    fun calculateResult(value: String): Double {
        Log.d(TAG, "calculateResult: method called with v: $value")
        var result = ignoreLastIndexOperator(value)


        val times = value.indexOf(CalculatorKeyboard.TIMES)
        val division = value.indexOf(CalculatorKeyboard.DIVISION)
        var plus = value.indexOf(CalculatorKeyboard.PLUS)
        var mines = value.indexOf(CalculatorKeyboard.MINES)
        if (mines == 0) {
            //addad manfi , negative number like -1334
            mines = value.indexOf(CalculatorKeyboard.MINES, 1)
        }
        if (plus == 0) {
            //addad mosbat , positive number like +3223
            plus = value.indexOf(CalculatorKeyboard.PLUS, 1)
        }

        val operatorList = listOf(
            times, division, plus, mines,
            //add this if have 2 like 15+454+569+1
            value.indexOf(CalculatorKeyboard.TIMES, times.plus(1)),
            value.indexOf(CalculatorKeyboard.DIVISION, division.plus(1)),
            value.indexOf(CalculatorKeyboard.PLUS, plus.plus(1)),
            value.indexOf(CalculatorKeyboard.MINES, mines.plus(1)),
        ).sorted()

        //times
        if (times.isPositive()) {
            if (times.plus(1) == value.length) {
                //if it was the last one remove it
                return calculateResult(result.removeRange(result.length.minus(1), result.length))
            } else {

                Log.d(TAG, "calculateResult: TIMES $result")

                //we should calculate the result and add it to text
                val timesIndexInOrder = operatorList.indexOf(times)
                val afterTimesIndexInSorted = timesIndexInOrder.plus(1)
                var beforeTimesIndexInSorted = timesIndexInOrder.minus(1)
                var beforeTimes = 0
                var afterTimes = 0

                if (afterTimesIndexInSorted == operatorList.size) {
                    //times is the last operator
                    afterTimes = value.length.minus(1)//last index
                } else {
                    afterTimes = operatorList[afterTimesIndexInSorted].minus(1)
                }
                if (beforeTimesIndexInSorted < 0) {
                    //times is the first operator
                    beforeTimes = 0//last index
                } else {
                    beforeTimes = operatorList[beforeTimesIndexInSorted].plus(1)
                    //if its -1 'not existed'
                    if (beforeTimes < 0)
                        beforeTimes = 0


                }

                Log.d(TAG, "calculateResult: before = $beforeTimes")
                Log.d(TAG, "calculateResult: MAIN = $times")
                Log.d(TAG, "calculateResult: after = $afterTimes")

                val x = result.substring(beforeTimes, times).toDouble()
                val y = result.substring(times.plus(1), afterTimes.plus(1)).toDouble()

                Log.d(TAG, "calculateResult: x = $x")
                Log.d(TAG, "calculateResult: y = $y")

                val timesResult = (x.times(y)).toString()
                Log.d(TAG, "calculateResult: timeResult = $timesResult")
                result = result.replaceRange(beforeTimes, afterTimes.plus(1), timesResult)
                Log.d(TAG, "calculateResult: times final result = $result")
                return calculateResult(result)

            }
        }
        //division
        if (division.isPositive()) {

            if (division.plus(1) == value.length) {
                //if it was the last one remove it
                return calculateResult(result.removeRange(result.length.minus(1), result.length))
            } else {
                Log.d(TAG, "calculateResult: DIVISION $result")

                //we should calculate the result and add it to text
                val divisionIndexInOrder = operatorList.indexOf(division)
                val afterDivisionIndexInSorted = divisionIndexInOrder.plus(1)
                var beforeDivisionIndexInSorted = divisionIndexInOrder.minus(1)
                var beforeDivision = 0
                var afterDivision = 0

                if (afterDivisionIndexInSorted == operatorList.size) {
                    //division is the last operator
                    afterDivision = value.length.minus(1)//last index
                } else {
                    afterDivision = operatorList[afterDivisionIndexInSorted].minus(1)
                }
                if (beforeDivisionIndexInSorted < 0) {
                    //division is the first operator
                    beforeDivision = 0//last index
                } else {
                    beforeDivision = operatorList[beforeDivisionIndexInSorted].plus(1)
                    //if its -1 'not existed'
                    if (beforeDivision < 0)
                        beforeDivision = 0


                }

                Log.d(TAG, "calculateResult: before = $beforeDivision")
                Log.d(TAG, "calculateResult: MAIN = $division")
                Log.d(TAG, "calculateResult: after = $afterDivision")

                val x = result.substring(beforeDivision, division).toDouble()
                val y = result.substring(division.plus(1), afterDivision.plus(1)).toDouble()

                Log.d(TAG, "calculateResult: x = $x")
                Log.d(TAG, "calculateResult: y = $y")

                val divisionResult = (x.div(y)).toString()
                Log.d(TAG, "calculateResult: timeResult = $divisionResult")
                result = result.replaceRange(beforeDivision, afterDivision.plus(1), divisionResult)
                Log.d(TAG, "calculateResult: division final result = $result")
                return calculateResult(result)

            }
        }

        //plus
        if (plus.isPositive()) {
            if (plus.plus(1) == value.length) {
                //if it was the last one remove it
                return calculateResult(result.removeRange(result.length.minus(1), result.length))
            } else {
                Log.d(TAG, "calculateResult: PLUS $result")
                //we should calculate the result and add it to text
                val plusIndexInOrder = operatorList.indexOf(plus)
                val afterPlusIndexInSorted = plusIndexInOrder.plus(1)
                var beforePlusIndexInSorted = plusIndexInOrder.minus(1)
                var beforeplus = 0
                var afterplus = 0

                if (afterPlusIndexInSorted == operatorList.size) {
                    //plus is the last operator
                    afterplus = value.length.minus(1)//last index
                } else {
                    afterplus = operatorList[afterPlusIndexInSorted].minus(1)
                }
                if (beforePlusIndexInSorted < 0) {
                    //plus is the first operator
                    beforeplus = 0//last index
                } else {
                    beforeplus = operatorList[beforePlusIndexInSorted].plus(1)
                    //if its -1 'not existed'
                    if (beforeplus < 0)
                        beforeplus = 0


                }

                Log.d(TAG, "calculateResult: before = $beforeplus")
                Log.d(TAG, "calculateResult: MAIN = $plus")
                Log.d(TAG, "calculateResult: after = $afterplus")

                val x = result.substring(beforeplus, plus).toDouble()
                val y = result.substring(plus, afterplus.plus(1)).toDouble()

                Log.d(TAG, "calculateResult: x = $x")
                Log.d(TAG, "calculateResult: y = $y")

                val plusResult = (x.plus(y)).toString()
                Log.d(TAG, "calculateResult: timeResult = $plusResult")
                result = result.replaceRange(beforeplus, afterplus.plus(1), plusResult)
                Log.d(TAG, "calculateResult: plus final result = $result")

                return calculateResult(result)

            }
        }

        //mines
        if (mines.isPositive()) {
            if (mines.plus(1) == value.length) {
                //if it was the last one remove it
                //TODO HOW ABOUT اعداد منفی ما اینجا بیخیال شدیم
                return calculateResult(result.removeRange(result.length.minus(1), result.length))
            } else if (mines == 0) {
                //TODO ADDAD MANFI like -599
                return calculateResult(result.removeRange(0, 1))
            } else {

                Log.d(TAG, "calculateResult: MINUS $result")
                //we should calculate the result and add it to text
                val minesIndexInOrder = operatorList.indexOf(mines)
                val afterMinesIndexInSorted = minesIndexInOrder.plus(1)
                var beforeMinesIndexInSorted = minesIndexInOrder.minus(1)
                var beforemines = 0
                var aftermines = 0

                if (afterMinesIndexInSorted == operatorList.size) {
                    //mines is the last operator
                    aftermines = value.length.minus(1)//last index
                } else {
                    aftermines = operatorList[afterMinesIndexInSorted].minus(1)
                }
                if (beforeMinesIndexInSorted < 0) {
                    //mines is the first operator
                    beforemines = 0//last index
                } else {
                    beforemines = operatorList[beforeMinesIndexInSorted].plus(1)
                    //if its -1 'not existed'
                    if (beforemines < 0)
                        beforemines = 0


                }

                Log.d(TAG, "calculateResult: before = $beforemines")
                Log.d(TAG, "calculateResult: MAIN = $mines")
                Log.d(TAG, "calculateResult: after = $aftermines")

                val x = result.substring(beforemines, mines).toDouble()
                val y = result.substring(mines.plus(1), aftermines.plus(1)).toDouble()

                Log.d(TAG, "calculateResult: x = $x")
                Log.d(TAG, "calculateResult: y = $y")

                val minesResult = (x.minus(y)).toString()
                Log.d(TAG, "calculateResult: timeResult = $minesResult")
                result = result.replaceRange(beforemines, aftermines.plus(1), minesResult)
                Log.d(TAG, "calculateResult: minus final result = $result")
                return calculateResult(result)

            }
        }

        Log.d(TAG, "calculateResult: end final final result $result")
        return result.toDouble()
    }

    private fun ignoreLastIndexOperator(value: String): String {
        //this method delete the last operator
        val lastChar = value[value.length.minus(1)].toString()
        if (
            lastChar == TIMES ||
            lastChar == DIVISION ||
            lastChar == PLUS ||
            lastChar == MINES
        ){
            //remove it from string
            return value.removeRange(value.length.minus(1),value.length)
        }
        return value
    }

    private fun Int.isPositive(): Boolean = this >= 0
}