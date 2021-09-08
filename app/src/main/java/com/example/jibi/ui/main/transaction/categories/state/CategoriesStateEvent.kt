package com.example.jibi.ui.main.transaction.categories.state

import com.example.jibi.models.Category
import com.example.jibi.util.StateEvent
import java.util.HashMap

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

    data class ChangeCategoryOrder(
        val changeOrderFields: ChangeOrderFields
    ) : CategoriesStateEvent() {
        override fun errorInfo(): String = ERROR


        override fun getId(): String = "$NAME req: $changeOrderFields ${this.hashCode()}"

        companion object {
            const val NAME = "ChangeCategoryOrder"
            const val ERROR = "Unable to change order of category"
        }
    }

    data class ChangeCategoryOrderNew(
        val newOrder: HashMap<Int, Int>,
        val type: Int
    ) : CategoriesStateEvent() {
        override fun errorInfo(): String = ERROR


        override fun getId(): String = "$NAME newOrder: $newOrder ${this.hashCode()}"

        companion object {
            const val NAME = "ChangeCategoryOrderNEW"
            const val ERROR = "Unable to change order of category"
        }
    }

    object GetAllOfCategories : CategoriesStateEvent() {
        override fun errorInfo(): String = "Unable to get categories from database"

        override fun getId(): String = "Getting all of categories ${this.hashCode()}"

    }
}
