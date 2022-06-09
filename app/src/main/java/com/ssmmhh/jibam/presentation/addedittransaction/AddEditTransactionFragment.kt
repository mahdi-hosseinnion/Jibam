package com.ssmmhh.jibam.presentation.addedittransaction

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.navArgs
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetBehavior.*
import com.ssmmhh.jibam.R
import com.ssmmhh.jibam.databinding.FragmentAddEditTransactionBinding
import com.ssmmhh.jibam.presentation.common.BaseFragment
import com.ssmmhh.jibam.presentation.util.ToolbarLayoutListener
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview

@FlowPreview
@ExperimentalCoroutinesApi
class AddEditTransactionFragment(
    viewModelFactory: ViewModelProvider.Factory,
) : BaseFragment(), ToolbarLayoutListener {

    private lateinit var binding: FragmentAddEditTransactionBinding

    private val viewModel by viewModels<AddEditTransactionViewModel> { viewModelFactory }

    private val args: AddEditTransactionFragmentArgs by navArgs()

    lateinit var selectCategoryBottomSheetBehavior: BottomSheetBehavior<LinearLayout>

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentAddEditTransactionBinding.inflate(inflater, container, false).apply {
            listener = this@AddEditTransactionFragment
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        start(args.transactionId)
        initializeSelectCategoryBottomSheet()
        subscribeObservers()
    }

    private fun start(transactionId: Int) {
        if (transactionId >= 0) {
            //Transaction Detail
            binding.toolbarTitle = getString(R.string.details)
            viewModel.startWithTransaction(transactionId)
        } else {
            //Add new transaction (transactionId will be -1)
            binding.toolbarTitle = getString(R.string.add_transaction)
            viewModel.showSelectCategoryBottomSheet()
        }
    }

    private fun initializeSelectCategoryBottomSheet() {
        selectCategoryBottomSheetBehavior = from(binding.selectCategoryBottomSheet).apply {
            addBottomSheetCallback(selectCategoryBottomSheetBehaviorCallback)
            isHideable = true
            skipCollapsed = true
            state = STATE_HIDDEN
        }
    }

    private fun subscribeObservers() {
        viewModel.showSelectCategoryBottomSheet.observe(viewLifecycleOwner) {
            handleSelectCategoryBottomSheetState(it)
        }
    }

    private fun handleSelectCategoryBottomSheetState(showBottomSheet: Boolean) {
        selectCategoryBottomSheetBehavior.state =
            if (showBottomSheet) STATE_EXPANDED else STATE_HIDDEN

        if (showBottomSheet) {
            binding.categoryFab.hide()
            binding.fabSubmit.hide()
            activityCommunicationListener.hideSoftKeyboard()
            binding.edtMoney.isFocusable = false
            binding.edtMemo.isFocusable = false
        } else {
            binding.categoryFab.show()
            binding.fabSubmit.show()
            binding.edtMoney.isFocusableInTouchMode = true
            binding.edtMemo.isFocusableInTouchMode = true
        }
    }


    override fun handleStateMessages() {
        viewModel.stateMessage.observe(viewLifecycleOwner) {
            it?.let {
                //Change undo snack bar parent view to fragment's root
                handleNewStateMessage(it) { viewModel.clearStateMessage() }
            }
        }
    }

    override fun handleLoading() {
        viewModel.countOfActiveJobs.observe(
            viewLifecycleOwner
        ) {
            showProgressBar(viewModel.areAnyJobsActive())
        }
    }

    override fun onClickOnNavigation(view: View) {
        navigateBack()
    }

    private val selectCategoryBottomSheetBehaviorCallback =
        object : BottomSheetBehavior.BottomSheetCallback() {

            override fun onStateChanged(bottomSheet: View, newState: Int) {
                if (newState == STATE_HIDDEN) {
                    //Update viewmodel state if user slide the bottom sheet down.
                    viewModel.hideSelectCategoryBottomSheet()
                }
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {}
        }

    override fun onClickOnMenuButton(view: View) {}
}
