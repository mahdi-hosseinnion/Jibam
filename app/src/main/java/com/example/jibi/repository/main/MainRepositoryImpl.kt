package com.example.jibi.repository.main

import com.example.jibi.persistence.CategoriesDao
import com.example.jibi.persistence.RecordsDao
import javax.inject.Inject

class MainRepositoryImpl
@Inject
constructor(
    val recordsDao: RecordsDao,
    val categoriesDao: CategoriesDao
) : MainRepository {


}