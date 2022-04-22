package com.ssmmhh.jibam.presentation.categories.viewcategories.state

import com.ssmmhh.jibam.R
import com.ssmmhh.jibam.util.StateEvent
import java.util.*

sealed class ViewCategoriesStateEvent : StateEvent {

    data class DeleteCategory(
        val categoryId: Int
    ) : ViewCategoriesStateEvent() {
        override val errorInfo: Int =
            R.string.unable_to_delete_category

        override val getId: String = "DeleteCategory $categoryId ${this.hashCode()}"
    }

    data class ChangeCategoryOrder(
        val newOrder: HashMap<Int, Int>,
        val type: Int
    ) : ViewCategoriesStateEvent() {
        override val errorInfo: Int = R.string.unable_to_change_order_of_selected_category


        override val getId: String = "ChangeCategoryOrder newOrder: $newOrder ${this.hashCode()}"

    }

}
