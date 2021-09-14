package com.example.jibi.ui.main.transaction.addedittransaction.inserttransaction

import android.content.SharedPreferences
import android.content.res.Resources
import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat
import com.bumptech.glide.RequestManager
import com.example.jibi.R
import com.example.jibi.di.main.MainScope
import com.example.jibi.models.Category
import com.example.jibi.ui.main.transaction.addedittransaction.common.AddEditTransactionParentFragment
import com.example.jibi.ui.main.transaction.addedittransaction.inserttransaction.state.InsertTransactionPresenterState
import com.example.jibi.ui.main.transaction.addedittransaction.inserttransaction.state.InsertTransactionPresenterState.*
import com.example.jibi.util.StateMessageCallback
import com.google.android.material.bottomsheet.BottomSheetBehavior
import kotlinx.android.synthetic.main.fragment_add_transaction.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import java.util.*
import javax.inject.Inject

@FlowPreview
@ExperimentalCoroutinesApi
@MainScope
class InsertTransactionFragment
@Inject
constructor(
    viewModelFactory: ViewModelProvider.Factory,
    private val requestManager: RequestManager,
    private val currentLocale: Locale,
    private val sharedPreferences: SharedPreferences,
    private val sharedPrefsEditor: SharedPreferences.Editor,
    private val _resources: Resources
) : AddEditTransactionParentFragment(
    requestManager = requestManager,
    currentLocale = currentLocale,
    sharedPreferences = sharedPreferences,
    sharedPrefsEditor = sharedPrefsEditor,
    _resources = _resources
) {
    private val viewModel by viewModels<InsertTransactionViewModel> { viewModelFactory }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUi()
        subscribeObservers()
    }


    private fun setupUi() {

    }

    private fun subscribeObservers() {
        viewModel.viewState.observe(viewLifecycleOwner) { vs ->
            vs?.let { viewState ->
                viewState.category?.let { setCategoryFields(it) }
                viewState.moneyStr?.let { setMoneyStringFields(it) }
                viewState.finalMoney?.let { setFinalMoneyFields(it) }
                viewState.memo?.let { setMemoFields(it) }
                viewState.combineCalender?.let { setDateFields(it) }
                viewState.presenterState?.let { handlePresenterStateChange(it) }
            }
        }
    }

    private fun handlePresenterStateChange(newState: InsertTransactionPresenterState) =
        when (newState) {

            is SelectingCategoryState -> {
            }

            is EnteringAmountOfMoneyState -> {
            }

            is ChangingDateState -> {
            }

            is ChangingTimeState -> {
            }

            is AddingNoteState -> {
            }

            is NoneState -> {
            }
        }

    private fun setDateFields(calendar: GregorianCalendar) {
        setDateToEditTexts(calendar.timeInMillis)
    }

    private fun setMemoFields(memo: String) {
        if (edt_memo.text.toString() != memo) {
            edt_memo.setText(memo)
        }
    }

    private fun setFinalMoneyFields(money: Double) {
        if (finalNUmber.text.toString().toDoubleOrNull() != money) {
            finalNUmber.text = money.toString()
        }
    }

    private fun setMoneyStringFields(moneyStr: String) {
        if (edt_money.text.toString() != moneyStr) {
            edt_money.setText(moneyStr)
        }
    }

    private fun setCategoryFields(category: Category) {
        //set name and icon
        category_fab.text =
            category.getCategoryNameFromStringFile(_resources, requireActivity().packageName) {
                it.name
            }
        category_fab.extend()

        val resourceId: Int = requireActivity().resources.getIdentifier(
            "ic_cat_${category.img_res}",
            "drawable",
            requireActivity().packageName
        )
        category_fab.icon = VectorDrawableCompat.create(resources, resourceId, null)
    }

    override fun setTextToAllViews() {
        txtField_memo.hint = _getString(R.string.write_note)
        txtField_date.hint = _getString(R.string.date)
        edt_money.hint = _getString(R.string._0)
    }


    override fun getCombineCalender(): GregorianCalendar = viewModel.getCombineCalender()

    override fun setToCombineCalender(year: Int, month: Int, day: Int) {
        viewModel.setToCombineCalender(year, month, day)
    }

    override fun setToCombineCalender(field: Int, value: Int) {
        viewModel.setToCombineCalender(field, value)
    }

    override fun onMoneyEditTextFocusChanged(hasFocus: Boolean) {
        if (hasFocus)
            viewModel.setPresenterState(EnteringAmountOfMoneyState)
        else
            viewModel.setPresenterState(NoneState)
    }

    override fun onClickedOnMoneyEditText() {
        viewModel.setPresenterState(EnteringAmountOfMoneyState)
    }

    override fun onBottomSheetStateChanged(newState: Int) {
        if (newState == BottomSheetBehavior.STATE_HIDDEN) {
            //bottomSheet slide animation stuff stuff
            if (edt_money.text.toString().isBlank()) {
                viewModel.setPresenterState(EnteringAmountOfMoneyState)
            } else {
                viewModel.setPresenterState(NoneState)
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

    override fun handleStateMessages() {
        viewModel.stateMessage.observe(viewLifecycleOwner) { sm ->
            sm?.let { stateMessage ->
                uiCommunicationListener.onResponseReceived(
                    response = stateMessage.response,
                    stateMessageCallback = object : StateMessageCallback {
                        override fun removeMessageFromStack() {
                            viewModel.clearStateMessage()
                        }
                    })
            }
        }
    }

    override fun onItemSelected(position: Int, item: Category) {
        //on category changed
        viewModel.setTransactionCategory(item)
        viewModel.setPresenterState(EnteringAmountOfMoneyState)
    }

}