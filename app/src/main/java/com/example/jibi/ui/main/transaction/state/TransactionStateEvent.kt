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

        override fun getId(): String ="Get sum of all transaction hashCode: ${this.hashCode()}"
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

        override fun getId(): String = "getTransaction minDate: $minDate maxDate: $maxDate hashCode: ${this.hashCode()}"
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
        override fun getId(): String = "getSumOfMoney minDate: $minDate maxDate: $maxDate hashCode: ${this.hashCode()}"
    }

    object None : TransactionStateEvent() {
        override fun errorInfo(): String {
            return "ERROR NONE"
        }

        override fun toString(): String {
            return "None"
        }
        override fun getId(): String = "NONE  hashCode: ${this.hashCode()}"

    }
}