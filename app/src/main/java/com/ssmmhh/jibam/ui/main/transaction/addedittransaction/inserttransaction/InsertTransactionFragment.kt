package com.ssmmhh.jibam.ui.main.transaction.addedittransaction.inserttransaction

import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat
import com.bumptech.glide.RequestManager
import com.ssmmhh.jibam.R
import com.ssmmhh.jibam.models.Category
import com.ssmmhh.jibam.models.TransactionEntity
import com.ssmmhh.jibam.ui.main.transaction.addedittransaction.common.AddEditTransactionParentFragment
import com.ssmmhh.jibam.ui.main.transaction.addedittransaction.inserttransaction.state.InsertTransactionPresenterState
import com.ssmmhh.jibam.ui.main.transaction.addedittransaction.inserttransaction.state.InsertTransactionPresenterState.*
import com.ssmmhh.jibam.util.Constants.EXPENSES_TYPE_MARKER
import com.ssmmhh.jibam.util.DiscardOrSaveCallback
import com.ssmmhh.jibam.util.MessageType
import com.ssmmhh.jibam.util.StateMessageCallback
import com.ssmmhh.jibam.util.UIComponentType
import com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_EXPANDED
import com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_HIDDEN
import kotlinx.android.synthetic.main.fragment_add_transaction.*
import kotlinx.android.synthetic.main.layout_toolbar_with_back_btn.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import java.util.*

@FlowPreview
@ExperimentalCoroutinesApi
class InsertTransactionFragment
constructor(
    viewModelFactory: ViewModelProvider.Factory,
    private val requestManager: RequestManager,
    private val currentLocale: Locale,
    private val sharedPreferences: SharedPreferences,
    private val sharedPrefsEditor: SharedPreferences.Editor
) : AddEditTransactionParentFragment(
    requestManager = requestManager,
    currentLocale = currentLocale,
    sharedPreferences = sharedPreferences,
    sharedPrefsEditor = sharedPrefsEditor,
    fab_text = R.string.save
) {

    private val viewModel by viewModels<InsertTransactionViewModel> { viewModelFactory }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUi()
        subscribeObservers()
    }


    private fun setupUi() {
        findNavController().currentDestination?.label = getString(R.string.add_transaction)

        category_fab.hide()
        //add backstack listener for discard dialog
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            backStackForDialog
        )
        showCustomKeyboard(edt_money)
        topAppBar_normal.title = getString(R.string.add_transaction)
        topAppBar_normal.setNavigationOnClickListener {
            backStackForDialog.handleOnBackPressed()
        }
        /**
         * on clicks
         */
        bottom_sheet_close_btn.setOnClickListener {
            hideCategoryBottomSheet()
        }
        category_fab.setOnClickListener {
            viewModel.setPresenterState(SelectingCategoryState)
        }


        edt_memo.setOnClickListener {
            viewModel.setPresenterState(AddingNoteState)

        }
        fab_submit.setOnClickListener {
            insertTransaction()
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
                if (stateMessage.response.message == getString(R.string.transaction_successfully_inserted)) {
                    //transaction successfully inserted
                    uiCommunicationListener.hideSoftKeyboard()
                    navigateBack()
                }
                if (stateMessage.response.messageType == MessageType.Error) {
                    fab_submit.isEnabled = true
                }
            }
        }
    }

    private fun subscribeObservers() {
        viewModel.viewState.observe(viewLifecycleOwner) { vs ->
            vs?.let { viewState ->
                handleBottomSheetDrag(viewState.category)
                viewState.category?.let { setCategoryFields(it) }
                viewState.moneyStr?.let { setMoneyStringFields(it) }
                viewState.finalMoney?.let { setFinalMoneyFields(it) }
                viewState.memo?.let { setMemoFields(it) }
                viewState.combineCalender?.let { setDateFields(it) }
                viewState.allOfCategories?.let { setAllOfCategoriesFields(it) }
                viewState.presenterState?.getContentIfNotHandled()
                    ?.let { handlePresenterStateChange(it) }
            }
        }
    }


    private fun handlePresenterStateChange(newState: InsertTransactionPresenterState) =
        //TODO add system to ensure presenter don't call twice
        when (newState) {

            is SelectingCategoryState -> {
                btmsheetViewPagerAdapter.submitSelectedItemId(viewModel.getTransactionCategory()?.id)
                bottomSheetBehavior.state = STATE_EXPANDED
                category_fab.hide()
                fab_submit.hide()
                uiCommunicationListener.hideSoftKeyboard()
                disableContentInteraction(edt_memo)
            }

            is EnteringAmountOfMoneyState -> {
                bottomSheetBehavior.state = STATE_HIDDEN
                category_fab.show()
                fab_submit.show()
                disableContentInteraction(edt_memo)
//                uiCommunicationListener.hideSoftKeyboard()
                showCustomKeyboard(edt_money)
            }
            is AddingNoteState -> {
                bottomSheetBehavior.state = STATE_HIDDEN
                category_fab.show()
                fab_submit.show()
                hideCustomKeyboard()
                enableContentInteraction(edt_memo)
                forceKeyBoardToOpenForEditText(edt_memo)
            }
            is ChangingDateState -> {
                bottomSheetBehavior.state = STATE_HIDDEN
                category_fab.show()
                fab_submit.show()
                uiCommunicationListener.hideSoftKeyboard()
                hideCustomKeyboard()
                disableContentInteraction(edt_memo)
                //apply
                showDatePickerDialog(viewModel.getCombineCalender())
            }

            is ChangingTimeState -> {
                bottomSheetBehavior.state = STATE_HIDDEN
                category_fab.show()
                fab_submit.show()
                uiCommunicationListener.hideSoftKeyboard()
                hideCustomKeyboard()
                disableContentInteraction(edt_memo)
                //apply
                showTimePickerDialog(viewModel.getCombineCalender())
            }


            is NoneState -> {
                bottomSheetBehavior.state = STATE_HIDDEN
                category_fab.show()
                fab_submit.show()
                uiCommunicationListener.hideSoftKeyboard()
                hideCustomKeyboard()
                disableContentInteraction(edt_memo)

            }
        }

    private fun handleBottomSheetDrag(category: Category?) {
        val didUserSelectCategory = category != null
        //user should not be able to drag down bottom sheet when no category has been selected
        bottomSheetBehavior.isDraggable = didUserSelectCategory
        edt_money.isEnabled = didUserSelectCategory
        finalNUmber.isEnabled = didUserSelectCategory
        bottom_sheet_close_btn.visibility = if (category == null)
            View.GONE
        else
            View.VISIBLE
    }

    private fun setAllOfCategoriesFields(list: List<Category>) {
        btmsheetViewPagerAdapter.submitData(list)
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
            category.getCategoryNameFromStringFile(resources, requireActivity().packageName) {
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


    override fun setToCombineCalender(year: Int, month: Int, day: Int) {
        viewModel.setToCombineCalender(year, month, day)
    }

    override fun setToCombineCalender(field: Int, value: Int) {
        viewModel.setToCombineCalender(field, value)
    }

    override fun onMoneyEditTextFocusChanged(hasFocus: Boolean) {
//        if (hasFocus)
//            viewModel.setPresenterState(EnteringAmountOfMoneyState)
    }

    override fun onClickedOnMoneyEditText() {
        viewModel.setPresenterState(EnteringAmountOfMoneyState)
    }

    override fun onClickedOnEmptyOfDetailContainer() {
        viewModel.setPresenterState(NoneState)
    }

    override fun onClickedOnDate() {
        viewModel.setPresenterState(ChangingDateState)
    }

    override fun onClickedOnTime() {
        viewModel.setPresenterState(ChangingTimeState)
    }

    override fun removeDatePickerFromScreen() {
        viewModel.setPresenterState(NoneState)
    }

    override fun removeTimePickerFromScreen() {
        viewModel.setPresenterState(NoneState)
    }

    override fun onBottomSheetStateChanged(newState: Int) {
        if (newState == STATE_HIDDEN && viewModel.getTransactionCategory() != null) {
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


    override fun onItemSelected(position: Int, item: Category) {
        //on category changed
        viewModel.setTransactionCategory(item)
        viewModel.setPresenterState(EnteringAmountOfMoneyState)
    }

    private fun insertTransaction() {
        fab_submit.isEnabled = false
        getTransactionEntityFiled()?.let {
            viewModel.insertTransaction(it)
        } ?: run {
            fab_submit.isEnabled = true
        }
    }

    private fun getTransactionEntityFiled(): TransactionEntity? {

        val category = viewModel.getTransactionCategory()

        if (category == null) {
            showSnackBar(R.string.pls_select_category)
            viewModel.setPresenterState(SelectingCategoryState)
            return null
        }
        val moneyEditTextStr = edt_money.text.toString()

        if (moneyEditTextStr.isBlank()) {
            showSnackBar(R.string.pls_insert_some_money)
            viewModel.setPresenterState(EnteringAmountOfMoneyState)
            return null
        }

        val calculatedMoney = textCalculator.calculateResult(moneyEditTextStr)
            .replace(",".toRegex(), "")

        if (calculatedMoney.isBlank() || calculatedMoney.toDouble() <= 0) {
            showSnackBar(R.string.pls_insert_valid_amount_of_money)
            viewModel.setPresenterState(EnteringAmountOfMoneyState)
            return null
        }

        val calender = viewModel.getCombineCalender()

        //add marker to money if its expenses
        var money: Double = calculatedMoney.toDouble()
        if (category.type == EXPENSES_TYPE_MARKER) {
            money = money.times(-1)
        }

        return TransactionEntity(
            id = 0,
            money = money,
            memo = edt_memo.text.toString(),
            cat_id = category.id,
            date = (calender.timeInMillis).div(1_000).toInt()
        )
    }

    private fun hideCategoryBottomSheet() {
        if (viewModel.getTransactionCategory() == null) {
            showSnackBar(R.string.pls_select_category)
        } else {
            if (edt_money.text.toString().isBlank()) {
                //if user didn't insert money
                viewModel.setPresenterState(EnteringAmountOfMoneyState)
            } else {
                viewModel.setPresenterState(NoneState)
            }
        }
    }

    private val backStackForDialog = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            //check for search view
            if (viewModel.getTransactionCategory() != null) {
                showDiscardOrSaveDialog()
            } else {
                navigateBack()
            }
        }

    }

    private fun showDiscardOrSaveDialog() {
        val callback = object : DiscardOrSaveCallback {
            override fun save() {
                insertTransaction()
            }

            override fun discard() {
                navigateBack()
            }

            override fun cancel() {}
        }
        viewModel.addToMessageStack(
            message = getString(R.string.you_changes_have_not_saved),
            uiComponentType = UIComponentType.DiscardOrSaveDialog(callback),
            messageType = MessageType.Info
        )
    }

    companion object {
        private const val TAG = "InsertTransactionFragme"
    }
}