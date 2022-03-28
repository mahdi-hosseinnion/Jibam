package com.ssmmhh.jibam.ui.main.transaction.common.state

import com.ssmmhh.jibam.models.Category
import com.ssmmhh.jibam.util.Event

data class GetAllOfCategoriesViewState(
    val allOfCategories: Event<List<Category>?>?
)


