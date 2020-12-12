package com.example.jibi.ui.main.transaction.bottomSheet

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.example.jibi.R
import com.example.jibi.models.Category
import kotlinx.android.synthetic.main.bottom_sheet_create_new_trans.*
import kotlinx.android.synthetic.main.fragment_screen_slide_page.*

class ScreenSlidePageFragment
constructor(
    private val categoryList: List<Category>,
    private val interaction: BottomSheetListAdapter.Interaction
) : Fragment() {

    private lateinit var recyclerAdapter: BottomSheetListAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initRecyclerView()
    }


    private fun initRecyclerView() {
        category_recyclerView.apply {
            layoutManager = GridLayoutManager(this@ScreenSlidePageFragment.context, 4)
            recyclerAdapter = BottomSheetListAdapter(
                null,
                interaction
            )
            adapter = recyclerAdapter
        }
        recyclerAdapter.submitList(categoryList)
        category_recyclerView.isNestedScrollingEnabled = false
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_screen_slide_page, container, false)
}