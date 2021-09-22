package com.example.jibi.ui.main.transaction.categories.viewcategories.state

import com.example.jibi.models.Category

data class ViewCategoriesViewState(
    var categoryList: List<Category>? = null,
    var insertedCategoryRow: Long? = null

)

