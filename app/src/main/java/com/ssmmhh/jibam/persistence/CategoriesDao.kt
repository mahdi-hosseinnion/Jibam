package com.ssmmhh.jibam.persistence

import androidx.room.*
import com.ssmmhh.jibam.persistence.dtos.CategoryDto
import com.ssmmhh.jibam.persistence.entities.CategoryEntity
import com.ssmmhh.jibam.persistence.entities.CategoryImageEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoriesDao {
    @Query(
        """
        SELECT categories.*, 
        categoryImages.resName as imageResourceId, 
        categoryImages.backgroundColor as imageBackgroundColor 
        FROM categories 
        LEFT JOIN categoryImages ON categories.imageId = categoryImages.id 
        $CATEGORY_ORDER"""
    )
    fun getCategories(): Flow<List<CategoryDto>>

    @Query(
        """
        SELECT categories.*, 
        categoryImages.resName as imageResourceId, 
        categoryImages.backgroundColor as imageBackgroundColor 
        FROM categories 
        LEFT JOIN categoryImages ON categories.imageId = categoryImages.id 
        $CATEGORY_ORDER"""
    )
    suspend fun getAllOfCategories(): List<CategoryDto>

    @Query(
        """
        SELECT *, 
        categoryImages.resName as imageResourceId, 
        categoryImages.backgroundColor as imageBackgroundColor 
        FROM categories 
        LEFT JOIN categoryImages ON categories.imageId = categoryImages.id 
        WHERE type = :type $CATEGORY_ORDER"""
    )
    suspend fun getAllOfCategoriesWithType(type: Int): List<CategoryDto>

    @Query("SELECT * FROM categoryImages")
    fun getCategoriesImages(): Flow<List<CategoryImageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrReplace(categoryEntity: CategoryEntity): Long

    @Update
    suspend fun updateCategory(categoryEntity: CategoryEntity): Int

    @Delete
    suspend fun deleteCategory(categoryEntity: CategoryEntity): Int

    @Query("DELETE FROM categories WHERE id = :categoryId")
    suspend fun deleteCategory(categoryId: Int): Int

    @Query("SELECT MIN(ordering) FROM categories")
    suspend fun getMinOfOrdering(): Int

    @Query("SELECT MAX(ordering) FROM categories")
    suspend fun getMaxOfOrdering(): Int

    @Query(
        """
        SELECT categories.*, 
        categoryImages.resName as imageResourceId, 
        categoryImages.backgroundColor as imageBackgroundColor 
        FROM categories 
        LEFT JOIN categoryImages ON categories.imageId = categoryImages.id 
        WHERE categories.id = :id
        """
    )
    suspend fun getCategoryById(id: Int): CategoryDto?

    @Query(
        """
        UPDATE categories SET 
        ordering = :newOrder 
        WHERE id = :categoryId
        """
    )
    suspend fun updateOrder(categoryId: Int, newOrder: Int): Int

    @Query(
        """
        SELECT *, 
        categoryImages.resName as imageResourceId, 
        categoryImages.backgroundColor as imageBackgroundColor 
        FROM categories 
        LEFT JOIN categoryImages ON categories.imageId = categoryImages.id 
        WHERE type = :type 
        AND 
        ordering BETWEEN :fromOrder AND :toOrder 
        $CATEGORY_ORDER
        """
    )
    fun getAllCategoriesBetweenSpecificOrder(
        type: Int,
        fromOrder: Int,
        toOrder: Int
    ): List<CategoryDto>

    @Query("""UPDATE categories SET ordering = (ordering + 1) WHERE type = :type""")
    fun increaseAllOfOrdersByOne(type: Int)

    companion object {
        private const val CATEGORY_ORDER = "ORDER BY ordering ASC"
    }
}