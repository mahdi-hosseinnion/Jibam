package com.ssmmhh.jibam.ui.main.transaction.categories.addcategoires.state

import com.ssmmhh.jibam.models.Category
import com.ssmmhh.jibam.util.StateEvent

sealed class AddCategoryStateEvent : StateEvent {

    data class InsertCategory(
        val category: Category
    ) : AddCategoryStateEvent() {
        override fun errorInfo(): String =
            "Unable to insert this category"

        override fun getId(): String = "InsertCategory $category ${this.hashCode()}"
    }
}