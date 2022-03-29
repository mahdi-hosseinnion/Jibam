package com.ssmmhh.jibam.feature_categories.addcategoires.state

import com.ssmmhh.jibam.data.source.local.entity.CategoryImageEntity

data class AddCategoryViewState(
    val categoryType: Int?=null,
    val categoryImage: CategoryImageEntity? =null
)