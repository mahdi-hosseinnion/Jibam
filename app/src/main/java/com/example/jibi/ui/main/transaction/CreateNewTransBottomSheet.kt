package com.example.jibi.ui.main.transaction

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.jibi.R
import com.example.jibi.models.Category
import com.example.jibi.models.Record
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.android.synthetic.main.bottom_sheet_create_new_trans.*

class CreateNewTransBottomSheet
constructor(
    private val categoryList: List<Category>
) : BottomSheetDialogFragment(), BottomSheetListAdapter.Interaction {

    private lateinit var recyclerAdapter: BottomSheetListAdapter


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
//
        initRecyclerView()
    }

    private fun initRecyclerView() {
        category_recyclerView.apply {
            layoutManager = GridLayoutManager(this@CreateNewTransBottomSheet.context, 4)
            recyclerAdapter = BottomSheetListAdapter(
                null,
                this@CreateNewTransBottomSheet
            )
            adapter = recyclerAdapter
        }
        recyclerAdapter.submitList(categoryList)
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.bottom_sheet_create_new_trans, container, false)

    override fun onItemSelected(position: Int, item: Category) {
        this.dismiss()
        findNavController().navigate(R.id.action_transactionFragment_to_createTransactionFragment)
    }

    override fun restoreListPosition() {
    }
}