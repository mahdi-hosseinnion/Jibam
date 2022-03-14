package com.ssmmhh.jibam.persistence

import androidx.room.*
import com.ssmmhh.jibam.persistence.entities.CategoryEntity
import com.ssmmhh.jibam.persistence.entities.CategoryImageEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoriesDao {
    @Query("SELECT * FROM categories $CATEGORY_ORDER")
    fun getCategories(): Flow<List<CategoryEntity>>

    @Query("SELECT * FROM categories $CATEGORY_ORDER")
    suspend fun getAllOfCategories(): List<CategoryEntity>

    @Query("SELECT * FROM categories WHERE type = :type $CATEGORY_ORDER")
    suspend  fun getAllOfCategoriesWithType(type: Int): List<CategoryEntity>

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

    @Query("SELECT * FROM categories WHERE cId = :id")
    suspend fun getCategoryById(id: Int): CategoryEntity?

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
        SELECT * FROM categories 
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
    ): List<CategoryEntity>

    @Query("""UPDATE categories SET ordering = (ordering + 1) WHERE type = :type""")
    fun increaseAllOfOrdersByOne(type: Int)

    companion object {
        private const val CATEGORY_ORDER = "ORDER BY ordering ASC"
    }
}