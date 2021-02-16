package com.example.jibi.ui.main.transaction

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.RequestManager
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.example.jibi.R
import com.example.jibi.models.Category
import com.example.jibi.ui.main.transaction.AddCategoryFragment.Companion.EXPENSES
import com.example.jibi.ui.main.transaction.AddCategoryFragment.Companion.INCOME
import com.example.jibi.ui.main.transaction.state.TransactionStateEvent
import com.example.jibi.util.*
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.android.synthetic.main.fragment_view_categories.*
import kotlinx.android.synthetic.main.layout_transaction_list_item.view.*
import kotlinx.android.synthetic.main.layout_view_categories_list_item.view.*
import kotlinx.android.synthetic.main.layout_view_categories_list_item.view.category_image
import kotlinx.android.synthetic.main.layout_viewpager_list_item.view.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import java.util.*
import javax.inject.Inject

@FlowPreview
@ExperimentalCoroutinesApi
class ViewCategoriesFragment
@Inject
constructor(
    viewModelFactory: ViewModelProvider.Factory,
    private val requestManager: RequestManager
) : BaseTransactionFragment(
    R.layout.fragment_view_categories, viewModelFactory, R.id.viewCategoriesToolbar
) {
    //vars
    private val viewPagerAdapter = ViewPagerAdapter()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewPager_viewCategories.adapter = viewPagerAdapter

        //set titles
        val tabLayout=TabLayoutMediator(tab_layout, viewPager_viewCategories) { tab, position ->
            if (position == 0) {
                tab.text = resources.getString(R.string.expenses)
            } else {
                tab.text = resources.getString(R.string.income)
            }
        }.attach()
//        val int:Int=viewPager_viewCategories.currentItem
        adfasdf.setOnClickListener {
            Log.d("TAG123456", "onViewCreated: clicked ${viewPager_viewCategories.currentItem}")
            val categoryType = when (viewPager_viewCategories.currentItem) {
                0 -> EXPENSES
                1 -> INCOME
                else -> {
                    showUnableToRecognizeCategoryTypeError()
                    return@setOnClickListener
                }
            }

            val action =
                ViewCategoriesFragmentDirections.actionViewCategoriesFragmentToAddCategoryFragment(
                    categoryType = categoryType
                )
            findNavController().navigate(action)
        }

        subscribeObservers()
    }

    private fun subscribeObservers() {

        viewModel.viewState.observe(viewLifecycleOwner) { viewState ->

            viewState.categoryList?.let {
                viewPagerAdapter.submitList(it)
            }
        }
    }

    companion object {
        const val VIEWPAGER_SIZE = 2

    }

    //adapter
    private inner class ViewPagerAdapter(

    ) : RecyclerView.Adapter<ViewPagerAdapter.ViewPagerViewHolder>() {

        private var listOfCategories: List<Category>? = null

        private var currentPage = 0

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewPagerViewHolder =
            ViewPagerViewHolder(
                LayoutInflater.from(parent.context).inflate(
                    R.layout.layout_viewpager_list_item,
                    parent,
                    false
                )
            )

        override fun onBindViewHolder(holder: ViewPagerViewHolder, position: Int) {
            if (position == 0) {
                //expenses
                holder.bind(listOfCategories?.filter { category -> category.type == 1 })

            } else {
                //income
                holder.bind(listOfCategories?.filter { category -> category.type > 1 })
            }

        }

        fun submitList(listOfCategories: List<Category>) {
            this.listOfCategories = listOfCategories
            notifyDataSetChanged()
        }

        override fun getItemCount(): Int = VIEWPAGER_SIZE

        inner class ViewPagerViewHolder(
            itemView: View
        ) : RecyclerView.ViewHolder(itemView) {
            fun bind(categoryList: List<Category>?) {
                itemView.recycler_viewCategories.apply {

                    layoutManager =
                        LinearLayoutManager(this@ViewCategoriesFragment.requireContext())
                    adapter = ViewPagerRecyclerViewAdapter(categoryList, categoryInteraction)
                }

            }
        }


//        override fun onPageSelected(position: Int) {
//            currentPage = position
//        }

        fun getCurrentPage(): Int {
            return this.currentPage
        }
    }

    private inner class ViewPagerRecyclerViewAdapter
        (
        private val listOfCategories: List<Category>?,
        private val interaction: CategoryInteraction
    ) : RecyclerView.Adapter<ViewPagerRecyclerViewAdapter.ViewPagerRecyclerViewHolder>() {

        private val packageName = this@ViewCategoriesFragment.requireActivity().packageName

        override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int
        ): ViewPagerRecyclerViewHolder = ViewPagerRecyclerViewHolder(
            LayoutInflater.from(
                parent.context

            ).inflate(
                R.layout.layout_view_categories_list_item,
                parent,
                false
            ), interaction
        )

        override fun onBindViewHolder(holder: ViewPagerRecyclerViewHolder, position: Int) {
            holder.bind(listOfCategories?.get(position))
        }

        override fun getItemCount(): Int = listOfCategories?.size ?: 0


        inner class ViewPagerRecyclerViewHolder(
            itemView: View,
            private val intercation: CategoryInteraction
        ) :
            RecyclerView.ViewHolder(itemView) {

            fun bind(item: Category?) {
                if (item != null) {
                    itemView.apply {
                        itemView.nameOfCategory.text = item.name

                        val categoryImageUrl = this.resources.getIdentifier(
                            "ic_cat_${item.img_res}",
                            "drawable",
                            packageName
                        )
                        requestManager
                            .load(categoryImageUrl)
                            .centerInside()
                            .transition(DrawableTransitionOptions.withCrossFade())
                            .error(R.drawable.ic_error)
                            .into(itemView.category_image)

                        delete_category.setOnClickListener {
                            intercation.onDeleteClicked(adapterPosition, item)
                        }
                    }
                } else {
                    itemView.nameOfCategory.text = "UNKNOWN CATEGORY"
                }
            }
        }


    }

    val categoryInteraction = object : CategoryInteraction {
        override fun onDeleteClicked(position: Int, category: Category) {
            val callback = object : AreYouSureCallback {
                override fun proceed() {
                    deleteCategory(category)
                }

                override fun cancel() {}
            }
            uiCommunicationListener.onResponseReceived(
                Response(
                    "Are You sure you want to delete this category?",
                    UIComponentType.AreYouSureDialog(
                        callback
                    ), MessageType.Info
                ),
                object : StateMessageCallback {
                    override fun removeMessageFromStack() {}
                }
            )
        }
    }

    fun deleteCategory(category: Category) {
        viewModel.launchNewJob(
            TransactionStateEvent.OneShotOperationsTransactionStateEvent.DeleteCategory(
                category
            )
        )
    }

    interface CategoryInteraction {
        fun onDeleteClicked(position: Int, category: Category)
    }

    private fun showUnableToRecognizeCategoryTypeError() {
        val stateCallback = object : StateMessageCallback {
            override fun removeMessageFromStack() {}
        }

        uiCommunicationListener.onResponseReceived(
            Response(
                getString(R.string.unable_to_recognize_category_type),
                //TODO SHOW OK DIALOG
                UIComponentType.Dialog,
                MessageType.Error
            ), stateCallback
        )
    }
}