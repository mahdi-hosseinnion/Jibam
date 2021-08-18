package com.example.jibi.persistence

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.jibi.models.Category
import com.example.jibi.models.CategoryImages
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoriesDao {
    @Query("SELECT * FROM categories")
    fun getCategories(): Flow<List<Category>>

    @Query("SELECT * FROM categories")
    fun getAllOfCategories(): List<Category>

    @Query("SELECT * FROM category_images")
    fun getCategoriesImages(): Flow<List<CategoryImages>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrReplace(category: Category): Long

    @Update
    suspend fun updateCategory(category: Category): Int

    @Delete
    suspend fun deleteCategory(category: Category): Int

    @Query("DELETE FROM categories WHERE cId = :categoryId")
    suspend fun deleteCategory(categoryId: Int): Int

    @Query("SELECT MIN(ordering) FROM categories")
    suspend fun getMinOfOrdering(): Int

    @Query("SELECT MAX(ordering) FROM categories")
    suspend fun getMaxOfOrdering(): Int

    @Query("SELECT * FROM categories")
    suspend fun getAllCategory(): List<Category>

    @Query("SELECT * FROM categories WHERE cId = :id")
    suspend fun getCategoryById(id: Int): Category?
}