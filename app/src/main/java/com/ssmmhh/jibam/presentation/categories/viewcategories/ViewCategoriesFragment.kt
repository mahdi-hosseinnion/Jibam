package com.ssmmhh.jibam.presentation.categories.viewcategories

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.RequestManager
import com.google.android.material.tabs.TabLayoutMediator
import com.ssmmhh.jibam.R
import com.ssmmhh.jibam.data.source.local.entity.CategoryEntity
import com.ssmmhh.jibam.databinding.FragmentViewCategoriesBinding
import com.ssmmhh.jibam.presentation.common.BaseFragment
import com.ssmmhh.jibam.presentation.util.ToolbarLayoutListener
import com.ssmmhh.jibam.util.EventObserver
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview

@FlowPreview
@ExperimentalCoroutinesApi
class ViewCategoriesFragment(
    viewModelFactory: ViewModelProvider.Factory,
    private val requestManager: RequestManager,
) : BaseFragment(), ToolbarLayoutListener {

    private val TAG = "ViewCategoriesFragment"

    private lateinit var binding: FragmentViewCategoriesBinding

    private val viewModel by viewModels<ViewCategoriesViewModel> { viewModelFactory }

    private lateinit var viewPagerAdapter: ViewCategoriesViewPagerAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentViewCategoriesBinding.inflate(inflater, container, false).apply {
            viewmodel = viewModel
            listener = this@ViewCategoriesFragment
        }
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViewPager()
        attachTabLayoutMeditor()
        subscribeObservers()
    }

    private fun setupViewPager() {
        viewPagerAdapter = ViewCategoriesViewPagerAdapter(
            viewModel = viewModel,
        )
        binding.viewPager.adapter = viewPagerAdapter
    }

    private fun attachTabLayoutMeditor() {
        //set tabs title
        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            if (position == 0) {
                tab.text = getString(R.string.expenses)
            } else if (position == 1) {
                tab.text = getString(R.string.income)
            }
        }.attach()
    }

    private fun subscribeObservers() {
        viewModel.expensesCategories.observe(viewLifecycleOwner) {
            if (viewModel.isChangeReorderRunning) return@observe
            viewPagerAdapter.submitExpensesCategoryList(it)
        }

        viewModel.incomeCategories.observe(viewLifecycleOwner) {
            if (viewModel.isChangeReorderRunning) return@observe
            viewPagerAdapter.submitIncomeCategoryList(it)
        }

        viewModel.openAddCategoryEvent.observe(viewLifecycleOwner, EventObserver {
            navigateToAddCategoryFragment()
        })
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
                handleNewStateMessage(stateMessage) { viewModel.clearStateMessage() }
            }
        }
    }

    private fun navigateToAddCategoryFragment() {
        val errorMessage = "navigateToAddCategoryFragment: Invalid viewPagerViewCategories position"
        val categoryType = when (binding.viewPager.currentItem) {
            0 -> CategoryEntity.EXPENSES_TYPE_MARKER
            1 -> CategoryEntity.INCOME_TYPE_MARKER
            else -> {
                Log.e(TAG, errorMessage)
                return
            }
        }

        val action = ViewCategoriesFragmentDirections
            .actionViewCategoriesFragmentToAddCategoryFragment(categoryType = categoryType)
        findNavController().navigate(action)
    }


    override fun onClickOnNavigation(view: View) {
        navigateBack()
    }

    override fun onClickOnMenuButton(view: View) {}
}