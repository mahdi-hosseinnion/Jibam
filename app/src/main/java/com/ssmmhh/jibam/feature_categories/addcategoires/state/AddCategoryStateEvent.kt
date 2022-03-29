package com.ssmmhh.jibam.feature_categories.addcategoires.state

import com.ssmmhh.jibam.data.source.local.entity.CategoryEntity
import com.ssmmhh.jibam.util.StateEvent

sealed class AddCategoryStateEvent : StateEvent {

    data class InsertCategory(
        val categoryEntity: CategoryEntity
    ) : AddCategoryStateEvent() {
        override fun errorInfo(): String =
            "Unable to insert this category"

        override fun getId(): String = "InsertCategory $categoryEntity ${this.hashCode()}"
    }
}