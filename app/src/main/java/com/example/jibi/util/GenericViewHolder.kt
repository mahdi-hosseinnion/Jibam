package com.example.jibi.util

import android.content.res.Resources
import android.view.View
import android.widget.TextView
import androidx.annotation.StringRes
import androidx.recyclerview.widget.RecyclerView

class GenericViewHolder
constructor(
    itemView: View,
    viewId: Int,
    @StringRes stringId: Int
) : RecyclerView.ViewHolder(itemView) {
    init {
        itemView.findViewById<TextView>(viewId).text = itemView.resources.getString(stringId)
    }
}