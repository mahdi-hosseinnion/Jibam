package com.example.jibi.persistence

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.jibi.models.Category
import com.example.jibi.models.CategoryImages
import com.example.jibi.models.Record
import kotlinx.coroutines.flow.Flow
import java.nio.ByteOrder

@Dao
interface CategoriesDao {
    @Query("SELECT * FROM categories")
    fun getCategories(): Flow<List<Category>>

    @Query("SELECT * FROM category_images")
    fun getCategoriesImages(): LiveData<List<CategoryImages>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrReplace(category: Category): Long

    @Update
    suspend fun updateCategory(category: Category): Int

    @Delete
    suspend fun deleteCategory(category: Category): Int

    @Query("SELECT MIN(ordering) FROM categories")
    suspend fun getMinOfOrdering(): Int

    @Query("SELECT MAX(ordering) FROM categories")
    suspend fun getMaxOfOrdering(): Int

    @Query("SELECT * FROM categories")
    suspend fun getAllCategory(): List<Category>

    @Query("SELECT * FROM categories WHERE cId = :id")
    suspend fun getCategoryById(id: Int): Category?
}