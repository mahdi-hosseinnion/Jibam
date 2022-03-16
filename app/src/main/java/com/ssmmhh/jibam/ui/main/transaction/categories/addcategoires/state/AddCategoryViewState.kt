package com.ssmmhh.jibam.ui.main.transaction.categories.addcategoires.state

import com.ssmmhh.jibam.persistence.entities.CategoryImageEntity

data class AddCategoryViewState(
    val categoryType: Int?=null,
    val categoryImage: CategoryImageEntity? =null
)