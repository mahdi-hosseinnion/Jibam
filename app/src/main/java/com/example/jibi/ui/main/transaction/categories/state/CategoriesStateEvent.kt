package com.example.jibi.ui.main.transaction.categories.state

import com.example.jibi.models.Category
import com.example.jibi.util.StateEvent

sealed class CategoriesStateEvent : StateEvent {

    data class DeleteCategory(
        val categoryId: Int
    ) : CategoriesStateEvent() {
        override fun errorInfo(): String =
            "Unable to delete category"

        override fun getId(): String = "DeleteCategory $categoryId ${this.hashCode()}"
    }

//    data class PinOrUnpinCategory(
//        val category: Category
//    ) : CategoriesStateEvent() {
//        override fun errorInfo(): String =
//            "unable to pin this category"
//
//        override fun getId(): String = "pin or unpin Category $category ${this.hashCode()}"
//    }

    data class InsertCategory(
        val category: Category
    ) : CategoriesStateEvent() {
        override fun errorInfo(): String =
            "Unable to insert this category"

        override fun getId(): String = "InsertCategory $category ${this.hashCode()}"
    }
}
