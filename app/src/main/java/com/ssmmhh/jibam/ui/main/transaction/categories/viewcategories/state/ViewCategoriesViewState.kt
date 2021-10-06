package com.ssmmhh.jibam.ui.main.transaction.categories.viewcategories.state

import com.ssmmhh.jibam.models.Category

data class ViewCategoriesViewState(
    var categoryList: List<Category>? = null,
    var insertedCategoryRow: Long? = null

)

