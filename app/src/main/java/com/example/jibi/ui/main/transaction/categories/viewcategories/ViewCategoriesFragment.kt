package com.example.jibi.ui.main.transaction.categories.viewcategories

import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.RequestManager
import com.example.jibi.R
import com.example.jibi.models.Category
import com.example.jibi.ui.main.transaction.categories.addcategoires.AddCategoryFragment.Companion.EXPENSES
import com.example.jibi.ui.main.transaction.categories.addcategoires.AddCategoryFragment.Companion.INCOME
import com.example.jibi.ui.main.transaction.categories.viewcategories.state.ViewCategoriesStateEvent
import com.example.jibi.ui.main.transaction.common.BaseFragment
import com.example.jibi.util.*
import com.example.jibi.util.Constants.EXPENSES_TYPE_MARKER
import com.example.jibi.util.Constants.INCOME_TYPE_MARKER
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.android.synthetic.main.fragment_view_categories.*
import kotlinx.android.synthetic.main.layout_toolbar_with_back_btn.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import javax.inject.Inject

@FlowPreview
@ExperimentalCoroutinesApi
class ViewCategoriesFragment
@Inject
constructor(
    viewModelFactory: ViewModelProvider.Factory,
    private val requestManager: RequestManager,
    private val sharedPreferences: SharedPreferences,
    private val sharedPrefsEditor: SharedPreferences.Editor
) : BaseFragment(
    R.layout.fragment_view_categories
), ViewCategoriesRecyclerAdapter.CategoryInteraction {

    private val viewModel by viewModels<ViewCategoriesViewModel> { viewModelFactory }

    private val expensesItemTouchHelper by lazy {
        ItemTouchHelper(ViewCategoryItemTouchHelperCallback {
            viewModel.newReorder(it, EXPENSES_TYPE_MARKER)
        })
    }
    private val incomeItemTouchHelper by lazy {
        ItemTouchHelper(ViewCategoryItemTouchHelperCallback {
            viewModel.newReorder(it, INCOME_TYPE_MARKER)
        })
    }


    //vars
    private lateinit var viewPagerAdapter: ViewCategoriesViewPagerAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViewPager()
        setupUi()

        add_new_appbar.setOnClickListener {
            navigateToAddCategoryFragment()
        }
        txt_addNewCategory.setOnClickListener {
            navigateToAddCategoryFragment()
        }

        subscribeObservers()
    }

    private fun setupUi() {
        topAppBar.title = getString(R.string.category_setting)
        topAppBar.setNavigationOnClickListener {
            navigateBack()
        }
        //set titles
        val tabLayout = TabLayoutMediator(tab_layout, viewPager_viewCategories) { tab, position ->
            if (position == 0) {
                tab.text = getString(R.string.expenses)
            } else {
                tab.text = getString(R.string.income)
            }
        }.attach()
    }

    override fun onResume() {
        super.onResume()
        //if user add category and return from AddCategoryFragment data should be updated
        viewModel.refreshCategoryList()
    }

    private fun setupViewPager() {
        viewPagerAdapter = ViewCategoriesViewPagerAdapter(
            this.requireContext(),
            listOfCategories = null,
            expensesItemTouchHelper = expensesItemTouchHelper,
            incomeItemTouchHelper = incomeItemTouchHelper,
            categoryInteraction = this,
            requestManager = requestManager,
            packageName = this.requireActivity().packageName
        )

        viewPager_viewCategories.adapter = viewPagerAdapter
    }

    override fun handleLoading() {
        viewModel.countOfActiveJobs.observe(
            viewLifecycleOwner
        ) {
            showProgressBar(viewModel.areAnyJobsActive())
        }
    }

    override fun handleStateMessages() {
        viewModel.stateMessage.observe(viewLifecycleOwner) {
            it?.let { stateMessage ->
                //refresh category list b/c if there is stateMessage it means
                //something have been inserted or removed or order changed so we need to refresh to
                //reflect changes
                viewModel.refreshCategoryList()
                handleNewStateMessage(it) { viewModel.clearStateMessage() }
            }
        }
    }

    private fun subscribeObservers() {
        viewModel.viewState.observe(viewLifecycleOwner) { vs ->
            vs?.let { viewState ->
                viewState.categoryList?.let {
                    viewPagerAdapter.submitList(it)
                }
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


    override fun onDeleteClicked(position: Int, category: Category) {
        val callback = object : AreYouSureCallback {
            override fun proceed() {
                deleteCategory(category)
            }

            override fun cancel() {}
        }
        uiCommunicationListener.onResponseReceived(
            Response(
                getString(R.string.are_you_sure_delete_category),
                UIComponentType.AreYouSureDialog(
                    callback
                ), MessageType.Info
            ),
            object : StateMessageCallback {
                override fun removeMessageFromStack() {}
            }
        )
    }

    override fun onStartDrag(viewHolder: RecyclerView.ViewHolder, itemType: Int) {
        if (itemType == 1) {
            //expenses
            expensesItemTouchHelper.startDrag(viewHolder)
        }
        if (itemType == 2) {
            //income
            incomeItemTouchHelper.startDrag(viewHolder)
        }
    }


    fun deleteCategory(category: Category) {
        viewModel.launchNewJob(
            ViewCategoriesStateEvent.DeleteCategory(
                category.id
            )
        )
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