package com.ssmmhh.jibam.ui.main.transaction.categories.viewcategories.state

import com.ssmmhh.jibam.persistence.entities.CategoryEntity

data class ViewCategoriesViewState(
    var categoryEntityList: List<CategoryEntity>? = null,
    var insertedCategoryRow: Long? = null

)

