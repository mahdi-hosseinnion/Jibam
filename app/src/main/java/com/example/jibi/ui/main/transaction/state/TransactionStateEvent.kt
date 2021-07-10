package com.example.jibi.ui.main.transaction.state

import com.example.jibi.models.Category
import com.example.jibi.models.Record
import com.example.jibi.util.StateEvent

sealed class TransactionStateEvent : StateEvent {
    //get the sum of all transaction, income and expenses
    class getSummaryMoney() : TransactionStateEvent() {
        //this will show to user
        override fun errorInfo(): String {
            return "Error get sum of all money ->balance, income and expenses"
        }

        override fun toString(): String {
            return "getSummeryTrans"
        }

        override fun getId(): String = "Get sum of all transaction hashCode: ${this.hashCode()}"
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

        override fun getId(): String =
            "getTransaction minDate: $minDate maxDate: $maxDate hashCode: ${this.hashCode()}"
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

        override fun getId(): String =
            "getSumOfMoney minDate: $minDate maxDate: $maxDate hashCode: ${this.hashCode()}"

    }

    object GetCategoryImages : TransactionStateEvent() {
        override fun errorInfo(): String {
            return "Error get category images"
        }

        override fun toString(): String {
            return "GetCategoryImages"
        }

        override fun getId(): String =
            "getSumOfMoney  hashCode: ${this.hashCode()}"
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

    sealed class OneShotOperationsTransactionStateEvent : TransactionStateEvent() {
        data class InsertTransaction(
            val record: Record
        ) : OneShotOperationsTransactionStateEvent() {
            override fun errorInfo(): String =
                "ERROR: inserting record! recordMoney = ${record.money}"

            override fun getId(): String = "InsertTransaction $record ${this.hashCode()}"
        }

        data class GetSpecificTransaction(
            val transactionId: Int
        ) : OneShotOperationsTransactionStateEvent() {
            override fun errorInfo(): String = "ERROR: getting record! recordId = ${transactionId}"

            override fun getId(): String =
                "GetSpecificTransaction id: $transactionId ${this.hashCode()}"
        }

        data class UpdateTransaction(
            val record: Record
        ) : OneShotOperationsTransactionStateEvent() {
            override fun errorInfo(): String =
                "ERROR: updating record! recordMoney = ${record.money}"

            override fun getId(): String = "UpdateTransaction $record ${this.hashCode()}"
        }

        data class DeleteTransaction(
            val record: Record
        ) : OneShotOperationsTransactionStateEvent() {
            override fun errorInfo(): String =
                "ERROR: deleting record! recordMoney = ${record.money}"

            override fun getId(): String = "DeleteTransaction $record ${this.hashCode()}"
        }
        data class DeleteTransactionById(
            val transactionId:Int
        ) : OneShotOperationsTransactionStateEvent() {
            override fun errorInfo(): String =
                "ERROR: deleting transaction! id = ${transactionId}"

            override fun getId(): String = "DeleteTransaction id: $transactionId ${this.hashCode()}"
        }

        data class DeleteCategory(
            val category: Category
        ) : OneShotOperationsTransactionStateEvent() {
            override fun errorInfo(): String =
                "ERROR: deleting category! categoryName = ${category.name}"

            override fun getId(): String = "DeleteCategory $category ${this.hashCode()}"
        }

        data class InsertCategory(
            val category: Category
        ) : OneShotOperationsTransactionStateEvent() {
            override fun errorInfo(): String =
                "ERROR: inserting category! categoryName = ${category.name}"

            override fun getId(): String = "InsertCategory $category ${this.hashCode()}"
        }

        data class PinOrUnpinCategory(
            val category: Category
        ) : OneShotOperationsTransactionStateEvent() {
            override fun errorInfo(): String =
                "ERROR: pin or unpin category! categoryName = ${category.name}"

            override fun getId(): String = "pin or unpin Category $category ${this.hashCode()}"
        }

        data class UpdateCategory(
            val category: Category
        ) : OneShotOperationsTransactionStateEvent() {
            override fun errorInfo(): String =
                "ERROR: updating category! categoryName = ${category.name}"

            override fun getId(): String = "updating Category $category ${this.hashCode()}"
        }

        object GetPieChartData : OneShotOperationsTransactionStateEvent() {
            override fun errorInfo(): String =
                "ERROR: getting pie chart data!"

            override fun getId(): String = "getting pie chart data ${this.hashCode()}"
        }

        data class GetCategoryById(
            val categoryId: Int
        ) : OneShotOperationsTransactionStateEvent() {
            override fun errorInfo(): String = "ERROR: getting category! categoryID = ${categoryId}"

            override fun getId(): String =
                "GetCategoryById id: $categoryId ${this.hashCode()}"
        }

        data class GetAllTransactionByCategoryId(
            val categoryId: Int
        ) : OneShotOperationsTransactionStateEvent() {
            override fun errorInfo(): String =
                "ERROR: getting all transaction with category  id = ${categoryId}"

            override fun getId(): String = "getting all transaction with category id $categoryId ${this.hashCode()}"
        }
    }
}