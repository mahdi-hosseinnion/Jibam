package com.example.jibi.ui.main.transaction.categories.state

data class CategoriesViewState(
    var insertedCategoryRow: Long? = null

)
data class ChangeOrderFields(
    val categoryId:Int,
    val categoryType:Int,
    val lastPosition:Int,
    val newPosition:Int
)
