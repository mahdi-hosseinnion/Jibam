package com.ssmmhh.jibam.presentation.categories.addcategoires

import android.content.Context
import com.ssmmhh.jibam.data.source.local.entity.CategoryImageEntity
import com.ssmmhh.jibam.util.getResourcesStringValueByName

sealed class AddCategoryRecyclerViewItem(
    val itemType: Int
) {
    data class CategoryImage(
        val categoryImage: CategoryImageEntity
    ) : AddCategoryRecyclerViewItem(CATEGORY_VIEW_TYPE)

    data class Header(
        val name: String
    ) : AddCategoryRecyclerViewItem(HEADER_VIEW_TYPE) {

        /**
         * Retrieve group name by searching in string files to translate category group name to
         * other languages.
         * If there is not any matching string res then it will return [name].
         */
        fun getCategoryGroupNameFromStringFile(
            context: Context,
            defaultName: String = name
        ): String = getResourcesStringValueByName(context, this.name) ?: defaultName

    }

    companion object {
        const val CATEGORY_VIEW_TYPE = 1
        const val HEADER_VIEW_TYPE = 2
    }
}
