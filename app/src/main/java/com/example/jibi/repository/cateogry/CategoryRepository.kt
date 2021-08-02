package com.example.jibi.repository.cateogry

import com.example.jibi.models.Category
import com.example.jibi.models.CategoryImages
import kotlinx.coroutines.flow.Flow

interface CategoryRepository {

    fun getCategoryList(
    ): Flow<List<Category>>

    fun getCategoryImages(
    ): Flow<List<CategoryImages>>

}