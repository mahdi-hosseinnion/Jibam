package com.example.jibi.ui.main.transaction.categories

import android.util.Log
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.ItemTouchHelper.*
import androidx.recyclerview.widget.RecyclerView
import com.example.jibi.ui.main.transaction.categories.state.ChangeOrderFields
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview

@ExperimentalCoroutinesApi
@FlowPreview
class ViewCategoryItemTouchHelperCallback(
    private val moveItem: (changeOrderFields: ChangeOrderFields) -> Unit
) : ItemTouchHelper.SimpleCallback(UP or DOWN, 0) {
    /**
     * this callback called when user drag an item
     * we place drag and drop login here
     */
    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean {
        val adapter = recyclerView.adapter as ViewCategoriesRecyclerAdapter
        val from = viewHolder.adapterPosition
        val to = target.adapterPosition
        if (from == to) {
            return true
        }
        adapter.getItemAtPosition(from)?.let { fromObj ->
            Log.d("DEBUG REORDER", "onMove: from: ${fromObj.name} to position: $to ")

            val changeOrderRequirement = ChangeOrderFields(
                categoryId = fromObj.id,
                categoryType = fromObj.type,
                lastPosition= from,
                newPosition = to
            )
            moveItem(changeOrderRequirement)
            adapter.onItemMoved(from, to)
            adapter.notifyItemMoved(from, to)
        }
        return true
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {}

    /**
     * this callback called when a viewHolder(item) is selected
     * we highlight selected item here
     */
    override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {
        super.onSelectedChanged(viewHolder, actionState)
        if (actionState == ACTION_STATE_DRAG) {
            viewHolder?.itemView?.alpha = 0.5f
        }
    }

    /**
     *  this callback called when a viewHolder(item) is unselected(dropped)
     *  we unhighlight item here
     */
    override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
        super.clearView(recyclerView, viewHolder)
        viewHolder.itemView.alpha = 1f
    }


}