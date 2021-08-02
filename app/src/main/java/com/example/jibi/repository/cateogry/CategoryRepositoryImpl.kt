package com.example.jibi.repository.cateogry

import com.example.jibi.di.main.MainScope
import com.example.jibi.models.Category
import com.example.jibi.models.CategoryImages
import com.example.jibi.persistence.CategoriesDao
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class CategoryRepositoryImpl
@Inject
constructor(
    private val categoriesDao: CategoriesDao
) : CategoryRepository {

    override fun getCategoryList(): Flow<List<Category>> =
        categoriesDao.getCategories()

    override fun getCategoryImages(): Flow<List<CategoryImages>> =
        categoriesDao.getCategoriesImages()
}