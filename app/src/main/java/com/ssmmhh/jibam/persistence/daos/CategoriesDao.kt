package com.ssmmhh.jibam.persistence.daos

import androidx.room.*
import com.ssmmhh.jibam.persistence.dtos.CategoryDto
import com.ssmmhh.jibam.persistence.entities.CategoryEntity
import com.ssmmhh.jibam.persistence.entities.CategoryImageEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data access object for categories table & categoryImages.
 */
@Dao
interface CategoriesDao {

    /**
     * Insert new category or replace existing one.
     *
     * @param [categoryEntity], The category to be inserted.
     * @return The new rowId for the inserted category.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrReplace(categoryEntity: CategoryEntity): Long

    /**
     * Update existing category.
     *
     * @param [categoryEntity], The category to be inserted.
     * @return The number of row updated successfully
     */
    @Update
    suspend fun updateCategory(categoryEntity: CategoryEntity): Int

    /**
     * Delete a category.
     *
     * @param [categoryEntity], The category to be deleted.
     * @return The number of row deleted successfully.
     */
    @Delete
    suspend fun deleteCategory(categoryEntity: CategoryEntity): Int

    /**
     * Delete a category with id.
     *
     * @param [categoryId], The category id to be deleted.
     * @return The number of row deleted successfully.
     */
    @Query("DELETE FROM categories WHERE id = :categoryId")
    suspend fun deleteCategory(categoryId: Int): Int

    /**
     * Observe all of categories, order by category's order ascending.
     * Left join on categoryImages table with imageId.
     *
     * @return A list of all of the categories order by ordering.
     */
    @Query(
        value =
        """
        SELECT categories.*, 
        categoryImages.resName as imageResourceId, 
        categoryImages.backgroundColor as imageBackgroundColor 
        FROM categories 
        LEFT JOIN categoryImages ON categories.imageId = categoryImages.id 
        ORDER BY ordering ASC 
        """
    )
    fun observeCategories(): Flow<List<CategoryDto>>

    /**
     * Get all of categories, order by category's order ascending.
     * Left join on categoryImages table with imageId.
     *
     * @return A list of all of the categories order by ordering.
     */
    @Query(
        value =
        """
        SELECT categories.*, 
        categoryImages.resName as imageResourceId, 
        categoryImages.backgroundColor as imageBackgroundColor 
        FROM categories 
        LEFT JOIN categoryImages ON categories.imageId = categoryImages.id 
        ORDER BY ordering ASC 
        """
    )
    suspend fun getAllOfCategories(): List<CategoryDto>

    /**
     * Get all of categories with category type, order by category's order ascending.
     * Left join on categoryImages table with imageId.
     *
     * @return A list of all of the categories with [type], order by ordering.
     */
    @Query(
        value =
        """
        SELECT *, 
        categoryImages.resName as imageResourceId, 
        categoryImages.backgroundColor as imageBackgroundColor 
        FROM categories 
        LEFT JOIN categoryImages ON categories.imageId = categoryImages.id 
        WHERE type = :type 
        ORDER BY ordering ASC 
        """
    )
    suspend fun getAllOfCategoriesWithType(type: Int): List<CategoryDto>

    /**
     * Get category by id or null if does not exist.
     *
     * @param [id], Category id.
     * @return The category with id or null if does no exist.
     */
    @Query(
        value =
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

    /**
     * Update category order.
     *
     * @param [categoryId], The category to be updated.
     * @param [newOrder], The new order value.
     * @return The number of row updated successfully.
     */
    @Query(
        value =
        """
        UPDATE categories SET 
        ordering = :newOrder 
        WHERE id = :categoryId 
        """
    )
    suspend fun updateOrder(categoryId: Int, newOrder: Int): Int

    /**
     * Increase order of categories with [type] by one.
     *
     * @param [type], Categories type.
     */
    @Query("""UPDATE categories SET ordering = (ordering + 1) WHERE type = :type""")
    fun increaseAllOfOrdersByOne(type: Int)

    /**
     * Observe a list of all of categoryImages.
     *
     * @return A list of all of categoryImages.
     */
    @Query("SELECT * FROM categoryImages")
    fun observeCategoriesImages(): Flow<List<CategoryImageEntity>>

}