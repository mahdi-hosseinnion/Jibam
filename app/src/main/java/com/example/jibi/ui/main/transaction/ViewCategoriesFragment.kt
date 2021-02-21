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
import kotlinx.android.synthetic.main.layout_view_categories_list_item.*
import kotlinx.android.synthetic.main.layout_view_categories_list_item.view.*
import kotlinx.android.synthetic.main.layout_view_categories_list_item.view.category_image
import kotlinx.android.synthetic.main.layout_viewpager_list_item.view.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
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
        val tabLayout = TabLayoutMediator(tab_layout, viewPager_viewCategories) { tab, position ->
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
        const val CATEGORY_PIN_MARKER = -1

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

        fun sorteCateogories(categoryList: List<Category>?): List<Category>? {
            if (categoryList == null) {
                return null
            }
            val tempList = ArrayList(categoryList.sortedByDescending { it.ordering })
            val pinedList = categoryList.filter { it.ordering < 0 }.sortedByDescending { it.ordering }
            tempList.removeAll(pinedList)
            tempList.addAll(0, pinedList)
            return tempList
        }

        inner class ViewPagerViewHolder(
            itemView: View
        ) : RecyclerView.ViewHolder(itemView) {
            fun bind(categoryList: List<Category>?) {
                itemView.recycler_viewCategories.apply {

                    layoutManager =
                        LinearLayoutManager(this@ViewCategoriesFragment.requireContext())
                    var maxUnpinOrder = 0
                    if (categoryList != null) {
                        for (item in categoryList) {
                            if (maxUnpinOrder < item.ordering) {
                                maxUnpinOrder = item.ordering
                            }
                        }
                    }
                    adapter = ViewPagerRecyclerViewAdapter(
                        sorteCateogories(categoryList),
//                        categoryList?.sortedByDescending { category ->
//                            if (category.ordering < 0) {
//                                //manfi
//                                category.ordering.times(-1).plus(maxUnpinOrder)
//                            } else
//                                category.ordering
//                        },
                        categoryInteraction
                    )
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
                        itemView.nameOfCategory.text = "${item.ordering}| ${item.name}"

                        changePinState(item.ordering < 0)

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
                        pin_category.setOnClickListener {
                            intercation.onPinThisCategoryClicked(item)
                            showLoadingForPiningCategory()
                        }
                    }
                } else {
                    itemView.nameOfCategory.text = "UNKNOWN CATEGORY"
                }
            }

            fun showLoadingForPiningCategory() {
                itemView.pin_category.visibility = View.GONE
                itemView.pin_category_progressBar.visibility = View.VISIBLE
            }

            fun changePinState(isPinned: Boolean) {
                if (isPinned)
                    itemView.pin_category.setImageResource(android.R.drawable.btn_star_big_on)
                else
                    itemView.pin_category.setImageResource(android.R.drawable.btn_star_big_off)
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

        override fun onPinThisCategoryClicked(category: Category) {
            pinOrUnpinCategory(category)
        }
    }

    fun deleteCategory(category: Category) {
        viewModel.launchNewJob(
            TransactionStateEvent.OneShotOperationsTransactionStateEvent.DeleteCategory(
                category
            )
        )
    }

    fun pinOrUnpinCategory(category: Category) {
        viewModel.launchNewJob(
            TransactionStateEvent.OneShotOperationsTransactionStateEvent.PinOrUnpinCategory(
                category
            )
        )
    }

    interface CategoryInteraction {
        fun onDeleteClicked(position: Int, category: Category)
        fun onPinThisCategoryClicked(category: Category)
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