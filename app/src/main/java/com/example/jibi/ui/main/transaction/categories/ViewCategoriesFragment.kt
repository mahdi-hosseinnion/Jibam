package com.example.jibi.ui.main.transaction.categories

import android.content.SharedPreferences
import android.content.res.Resources
import android.os.Bundle
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
import com.example.jibi.ui.main.transaction.BaseTransactionFragment
import com.example.jibi.ui.main.transaction.categories.AddCategoryFragment.Companion.EXPENSES
import com.example.jibi.ui.main.transaction.categories.AddCategoryFragment.Companion.INCOME
import com.example.jibi.ui.main.transaction.transactions.TransactionsListAdapter
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
import uk.co.samuelwall.materialtaptargetprompt.MaterialTapTargetPrompt
import uk.co.samuelwall.materialtaptargetprompt.extras.backgrounds.RectanglePromptBackground
import uk.co.samuelwall.materialtaptargetprompt.extras.focals.RectanglePromptFocal
import javax.inject.Inject
import kotlin.random.Random

@FlowPreview
@ExperimentalCoroutinesApi
class ViewCategoriesFragment
@Inject
constructor(
    viewModelFactory: ViewModelProvider.Factory,
    private val requestManager: RequestManager,
    private val sharedPreferences: SharedPreferences,
    private val sharedPrefsEditor: SharedPreferences.Editor,
    private val _resources: Resources
) : BaseTransactionFragment(
    R.layout.fragment_view_categories, viewModelFactory, R.id.viewCategoriesToolbar, _resources
) {
    override fun setTextToAllViews() {
        txt_addNewCategory.text = _getString(R.string.add_new_category)
    }

    //vars
    private val viewPagerAdapter = ViewPagerAdapter()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewPager_viewCategories.adapter = viewPagerAdapter
        findNavController()
            .currentDestination?.label = _getString(R.string.category_setting)
        //set titles
        val tabLayout = TabLayoutMediator(tab_layout, viewPager_viewCategories) { tab, position ->
            if (position == 0) {
                tab.text = _getString(R.string.expenses)
            } else {
                tab.text = _getString(R.string.income)
            }
        }.attach()
//        val int:Int=viewPager_viewCategories.currentItem
        add_new_appbar.setOnClickListener {
            navigateToAddCategoryFragment()
        }
        txt_addNewCategory.setOnClickListener {
            navigateToAddCategoryFragment()
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

    private fun navigateToAddCategoryFragment() {
        val categoryType = when (viewPager_viewCategories.currentItem) {
            0 -> EXPENSES
            1 -> INCOME
            else -> {
                showUnableToRecognizeCategoryTypeError()
                return
            }
        }

        val action =
            ViewCategoriesFragmentDirections.actionViewCategoriesFragmentToAddCategoryFragment(
                categoryType = categoryType
            )
        findNavController().navigate(action)
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
                        sortCategoriesWithPinned(categoryList),
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
                    // Check that the view exists for the item
                    if (adapterPosition == 0 &&
                        sharedPreferences.getBoolean(
                            PreferenceKeys.PROMOTE_VIEW_CATEGORY_LIST,
                            true
                        )
                    ) {
                        showPromote()
                    }
                    itemView.apply {
                        val categoryName = item.getCategoryNameFromStringFile(
                            _resources,
                            requireActivity().packageName
                        ) {
                            it.name
                        }
                        itemView.nameOfCategory.text = "$categoryName"

                        changePinState(item.ordering < 0)

                        if (item.id > 0) {
                            try {
                                cardView_view_category.setCardBackgroundColor(
                                    resources.getColor(
                                        TransactionsListAdapter.listOfColor[(item.id.minus(
                                            1
                                        ))]
                                    )
                                )
                            } catch (e: Exception) {
                                //apply random color
                                cardView_view_category.setCardBackgroundColor(
                                    resources.getColor(
                                        TransactionsListAdapter.listOfColor[Random.nextInt(
                                            TransactionsListAdapter.listOfColor.size
                                        )]
                                    )
                                )
                            }
                        }

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
                    itemView.nameOfCategory.text = _getString(R.string.UNKNOWN_CATEGORY)
                }
            }

            fun showLoadingForPiningCategory() {
                itemView.pin_category.visibility = View.GONE
                itemView.pin_category_progressBar.visibility = View.VISIBLE
            }

            fun changePinState(isPinned: Boolean) {
                if (isPinned)
                    itemView.pin_category.setImageResource(R.drawable.ic_pin)
                else
                    itemView.pin_category.setImageResource(R.drawable.ic_unpin)
            }

            private fun showPromote() {
                MaterialTapTargetPrompt.Builder(this@ViewCategoriesFragment)
                    .setTarget(itemView.findViewById(R.id.root_transaction_item))
                    .setPrimaryText(_getString(R.string.view_category_tap_target_primary))
                    .setSecondaryText(_getString(R.string.view_category_tap_target_secondary))
                    .setPromptBackground(RectanglePromptBackground())
                    .setPromptFocal(RectanglePromptFocal())
                    .setPromptStateChangeListener { _, state ->
                        if (state == MaterialTapTargetPrompt.STATE_FOCAL_PRESSED || state == MaterialTapTargetPrompt.STATE_DISMISSING) {
                            sharedPrefsEditor.putBoolean(
                                PreferenceKeys.PROMOTE_VIEW_CATEGORY_LIST,
                                false
                            ).apply()
                        }
                    }
                    .show()
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
                    _getString(R.string.are_you_sure_delete_category),
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
                _getString(R.string.unable_to_recognize_category_type),
                //TODO SHOW OK DIALOG
                UIComponentType.Dialog,
                MessageType.Error
            ), stateCallback
        )
    }
}