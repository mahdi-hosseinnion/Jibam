package com.example.jibi.ui.main.transaction.state

import com.example.jibi.util.StateEvent

sealed class TransactionStateEvent : StateEvent {
    //get the sum of all transaction, income and expenses
    class getSummaryMoney() : TransactionStateEvent() {

        override fun errorInfo(): String {
            return "Error get sum of all money ->balance, income and expenses"
        }

        override fun toString(): String {
            return "getSummeryTrans"
        }
    }

    class getTrasaction(
        val minDate: Int? = null,
        val maxDate: Int? = null
    ) : TransactionStateEvent() {

        override fun errorInfo(): String {
            return "Error get all Transaction from $minDate to $maxDate"
        }

        override fun toString(): String {
            return "getTrasaction"
        }
    }

    class getSumOfMoney(
        val minDate: Int? = null,
        val maxDate: Int? = null
    ) : TransactionStateEvent() {
        override fun errorInfo(): String {
            return "Error get sum of money from $minDate to $maxDate"
        }

        override fun toString(): String {
            return "getSumOfMoney"
        }
    }

    class None() : TransactionStateEvent() {
        override fun errorInfo(): String {
            return "ERROR NONE"
        }

        override fun toString(): String {
            return "None"
        }
    }
}