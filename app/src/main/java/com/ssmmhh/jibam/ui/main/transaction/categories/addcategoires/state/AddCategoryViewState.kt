package com.ssmmhh.jibam.ui.main.transaction.categories.addcategoires.state

import com.ssmmhh.jibam.models.CategoryImages

data class AddCategoryViewState(
    val categoryType: Int?=null,
    val categoryImage: CategoryImages? =null
)