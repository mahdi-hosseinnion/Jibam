package com.ssmmhh.jibam.presentation.categories.addcategoires.state

import com.ssmmhh.jibam.data.source.local.entity.CategoryEntity
import com.ssmmhh.jibam.util.StateEvent

sealed class AddCategoryStateEvent : StateEvent {

    data class InsertCategory(
        val categoryEntity: CategoryEntity
    ) : AddCategoryStateEvent() {
        override val errorInfo: String =
            "Unable to insert this category"

        override val getId: String = "InsertCategory $categoryEntity ${this.hashCode()}"
    }
}