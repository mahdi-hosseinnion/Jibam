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
        SELECT *, 
        category_images.image_res as imageResourceId, 
        category_images.image_background_color as imageBackgroundColor 
        FROM categories 
        LEFT JOIN category_images ON categories.imageId = category_images.id 
        $CATEGORY_ORDER"""
    )
    fun getCategories(): Flow<List<CategoryDto>>

    @Query(
        """
        SELECT *, 
        category_images.image_res as imageResourceId, 
        category_images.image_background_color as imageBackgroundColor 
        FROM categories 
        LEFT JOIN category_images ON categories.imageId = category_images.id 
        $CATEGORY_ORDER"""
    )
    suspend fun getAllOfCategories(): List<CategoryDto>

    @Query(
        """
        SELECT *, 
        category_images.image_res as imageResourceId, 
        category_images.image_background_color as imageBackgroundColor 
        FROM categories 
        LEFT JOIN category_images ON categories.imageId = category_images.id 
        WHERE type = :type $CATEGORY_ORDER"""
    )
    suspend fun getAllOfCategoriesWithType(type: Int): List<CategoryDto>

    @Query("SELECT * FROM category_images")
    fun getCategoriesImages(): Flow<List<CategoryImageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrReplace(categoryEntity: CategoryEntity): Long

    @Update
    suspend fun updateCategory(categoryEntity: CategoryEntity): Int

    @Delete
    suspend fun deleteCategory(categoryEntity: CategoryEntity): Int

    @Query("DELETE FROM categories WHERE cId = :categoryId")
    suspend fun deleteCategory(categoryId: Int): Int

    @Query("SELECT MIN(ordering) FROM categories")
    suspend fun getMinOfOrdering(): Int

    @Query("SELECT MAX(ordering) FROM categories")
    suspend fun getMaxOfOrdering(): Int

    @Query(
        """
        SELECT *, 
        category_images.image_res as imageResourceId, 
        category_images.image_background_color as imageBackgroundColor 
        FROM categories 
        LEFT JOIN category_images ON categories.imageId = category_images.id 
        WHERE cId = :id
        """
    )
    suspend fun getCategoryById(id: Int): CategoryDto?

    @Query(
        """
        UPDATE categories SET 
        ordering = :newOrder 
        WHERE cId = :categoryId
        """
    )
    suspend fun updateOrder(categoryId: Int, newOrder: Int): Int

    @Query(
        """
        SELECT *, 
        category_images.image_res as imageResourceId, 
        category_images.image_background_color as imageBackgroundColor 
        FROM categories 
        LEFT JOIN category_images ON categories.imageId = category_images.id 
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