package com.ssmmhh.jibam.util

import android.view.View
import android.widget.TextView
import androidx.annotation.StringRes
import androidx.recyclerview.widget.RecyclerView

//TODO ("Databind this view holder")
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