package com.ssmmhh.jibam.presentation.categories.addcategoires.state

import com.ssmmhh.jibam.data.source.local.entity.CategoryImageEntity

data class AddCategoryViewState(
    val categoryType: Int?=null,
    val categoryImage: CategoryImageEntity? =null
)