package com.example.jibi.ui.main.transaction.categories.state

import com.example.jibi.models.Category

data class CategoriesViewState(
    var categoryList: List<Category>? = null,
    var insertedCategoryRow: Long? = null

)

data class ChangeOrderFields(
    val categoryId: Int,
    val categoryType: Int,
    val lastPosition: Int,
    val newPosition: Int
)
