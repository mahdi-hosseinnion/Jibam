package com.ssmmhh.jibam.feature_categories.viewcategories.state

import com.ssmmhh.jibam.util.StateEvent
import java.util.*

sealed class ViewCategoriesStateEvent : StateEvent {

    data class DeleteCategory(
        val categoryId: Int
    ) : ViewCategoriesStateEvent() {
        override val errorInfo: String =
            "Unable to delete category"

        override val getId: String = "DeleteCategory $categoryId ${this.hashCode()}"
    }

    data class ChangeCategoryOrder(
        val newOrder: HashMap<Int, Int>,
        val type: Int
    ) : ViewCategoriesStateEvent() {
        override val errorInfo: String = "Unable to change order of selected category"


        override val getId: String = "ChangeCategoryOrder newOrder: $newOrder ${this.hashCode()}"

    }

    object GetAllOfCategories : ViewCategoriesStateEvent() {
        override val errorInfo: String = "Unable to get categories from database"

        override val getId: String = "Getting all of categories ${this.hashCode()}"

    }
}
