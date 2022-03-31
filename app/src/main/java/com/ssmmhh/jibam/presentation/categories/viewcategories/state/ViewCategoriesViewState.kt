package com.ssmmhh.jibam.presentation.categories.viewcategories.state

import com.ssmmhh.jibam.data.model.Category

data class ViewCategoriesViewState(
    var categoryEntityList: List<Category>? = null,
    var insertedCategoryRow: Long? = null

)

