package com.example.jibi.ui.main.transaction.categories.viewcategories.state

import com.example.jibi.models.Category
import com.example.jibi.util.StateEvent
import java.util.HashMap

sealed class ViewCategoriesStateEvent : StateEvent {

    data class DeleteCategory(
        val categoryId: Int
    ) : ViewCategoriesStateEvent() {
        override fun errorInfo(): String =
            "Unable to delete category"

        override fun getId(): String = "DeleteCategory $categoryId ${this.hashCode()}"
    }


    data class InsertCategory(
        val category: Category
    ) : ViewCategoriesStateEvent() {
        override fun errorInfo(): String =
            "Unable to insert this category"

        override fun getId(): String = "InsertCategory $category ${this.hashCode()}"
    }
    data class ChangeCategoryOrder(
        val newOrder: HashMap<Int, Int>,
        val type: Int
    ) : ViewCategoriesStateEvent() {
        override fun errorInfo(): String = "Unable to change order of selected category"


        override fun getId(): String = "ChangeCategoryOrder newOrder: $newOrder ${this.hashCode()}"

    }

    object GetAllOfCategories : ViewCategoriesStateEvent() {
        override fun errorInfo(): String = "Unable to get categories from database"

        override fun getId(): String = "Getting all of categories ${this.hashCode()}"

    }
}
