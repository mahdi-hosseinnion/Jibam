package com.example.jibi.ui.main.transaction.categories.addcategoires.state

import com.example.jibi.models.Category
import com.example.jibi.util.StateEvent

sealed class AddCategoryStateEvent : StateEvent {

    data class InsertCategory(
        val category: Category
    ) : AddCategoryStateEvent() {
        override fun errorInfo(): String =
            "Unable to insert this category"

        override fun getId(): String = "InsertCategory $category ${this.hashCode()}"
    }
}