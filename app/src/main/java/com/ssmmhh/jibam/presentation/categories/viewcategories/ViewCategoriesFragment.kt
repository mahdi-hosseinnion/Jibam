package com.ssmmhh.jibam.presentation.categories.viewcategories

import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.RequestManager
import com.ssmmhh.jibam.R
import com.ssmmhh.jibam.data.model.Category
import com.ssmmhh.jibam.presentation.categories.addcategoires.AddCategoryFragment.Companion.EXPENSES
import com.ssmmhh.jibam.presentation.categories.addcategoires.AddCategoryFragment.Companion.INCOME
import com.ssmmhh.jibam.presentation.categories.viewcategories.state.ViewCategoriesStateEvent
import com.ssmmhh.jibam.presentation.common.BaseFragment
import com.ssmmhh.jibam.data.source.local.entity.CategoryEntity.Companion.EXPENSES_TYPE_MARKER
import com.ssmmhh.jibam.data.source.local.entity.CategoryEntity.Companion.INCOME_TYPE_MARKER
import com.google.android.material.tabs.TabLayoutMediator
import com.ssmmhh.jibam.data.util.*
import com.ssmmhh.jibam.databinding.FragmentViewCategoriesBinding
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview

@FlowPreview
@ExperimentalCoroutinesApi
class ViewCategoriesFragment(
    viewModelFactory: ViewModelProvider.Factory,
    private val requestManager: RequestManager,
    private val sharedPreferences: SharedPreferences,
    private val sharedPrefsEditor: SharedPreferences.Editor
) : BaseFragment(), ViewCategoriesRecyclerAdapter.CategoryInteraction {

    private val viewModel by viewModels<ViewCategoriesViewModel> { viewModelFactory }

    private var _binding: FragmentViewCategoriesBinding? = null

    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentViewCategoriesBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

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

        binding.addNewAppbar.setOnClickListener {
            navigateToAddCategoryFragment()
        }
        binding.txtAddNewCategory.setOnClickListener {
            navigateToAddCategoryFragment()
        }

        subscribeObservers()
    }

    private fun setupUi() {
        binding.toolbar.topAppBarNormal.title = getString(R.string.category_setting)
        binding.toolbar.topAppBarNormal.setNavigationOnClickListener {
            navigateBack()
        }
        //set titles
        val tabLayout =
            TabLayoutMediator(binding.tabLayout, binding.viewPagerViewCategories) { tab, position ->
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
            listOfCategoryEntities = null,
            expensesItemTouchHelper = expensesItemTouchHelper,
            incomeItemTouchHelper = incomeItemTouchHelper,
            categoryInteraction = this,
            requestManager = requestManager,
        )

        binding.viewPagerViewCategories.adapter = viewPagerAdapter
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
                viewState.categoryEntityList?.let {
                    viewPagerAdapter.submitList(it)
                }
            }
        }
    }

    private fun navigateToAddCategoryFragment() {
        val categoryType = when (binding.viewPagerViewCategories.currentItem) {
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


    override fun onDeleteClicked(position: Int, categoryEntity: Category) {
        val callback = object : AreYouSureCallback {
            override fun proceed() {
                deleteCategory(categoryEntity)
            }

            override fun cancel() {}
        }
        activityCommunicationListener.onResponseReceived(
            Response(
                intArrayOf(R.string.are_you_sure_delete_category),
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


    fun deleteCategory(categoryEntity: Category) {
        viewModel.launchNewJob(
            ViewCategoriesStateEvent.DeleteCategory(
                categoryEntity.id
            )
        )
    }


    private fun showUnableToRecognizeCategoryTypeError() {
        val stateCallback = object : StateMessageCallback {
            override fun removeMessageFromStack() {}
        }

        activityCommunicationListener.onResponseReceived(
            Response(
                intArrayOf(R.string.unable_to_recognize_category_type),
                //TODO SHOW OK DIALOG
                UIComponentType.Dialog,
                MessageType.Error
            ), stateCallback
        )
    }
}