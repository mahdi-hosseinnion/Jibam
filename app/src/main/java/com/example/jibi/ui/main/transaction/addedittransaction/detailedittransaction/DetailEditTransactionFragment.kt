package com.example.jibi.ui.main.transaction.addedittransaction.detailedittransaction

import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat
import com.bumptech.glide.RequestManager
import com.example.jibi.R
import com.example.jibi.models.Category
import com.example.jibi.models.Transaction
import com.example.jibi.models.TransactionEntity
import com.example.jibi.ui.main.transaction.addedittransaction.common.AddEditTransactionParentFragment
import com.example.jibi.ui.main.transaction.addedittransaction.detailedittransaction.state.DetailEditTransactionPresenterState
import com.example.jibi.ui.main.transaction.addedittransaction.detailedittransaction.state.DetailEditTransactionPresenterState.*
import com.example.jibi.util.*
import com.google.android.material.bottomsheet.BottomSheetBehavior
import kotlinx.android.synthetic.main.fragment_add_transaction.*
import kotlinx.android.synthetic.main.layout_toolbar_with_back_btn.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import java.util.*

@FlowPreview
@ExperimentalCoroutinesApi
class DetailEditTransactionFragment
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
    fab_text = R.string.update
) {

    private val TAG = "DetailEditTransactionFr"

    private val viewModel by viewModels<DetailEditTransactionViewModel> { viewModelFactory }

    private val args: DetailEditTransactionFragmentArgs by navArgs()


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUi()
        viewModel.getTransactionById(args.transactionId)
        subscribeObservers()
    }

    private fun setupUi() {
        topAppBar.title = getString(R.string.details)

        //add backstack listener for discard dialog
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            backStackForDialog
        )
        topAppBar.setNavigationOnClickListener {
            backStackForDialog.handleOnBackPressed()
        }
        topAppBar_img_btn.visibility = View.VISIBLE

        topAppBar_img_btn.setOnClickListener {
            checkForDelete(viewModel.getDefaultTransaction()?.id)

        }

        lifecycleScope.launchWhenStarted {
            //we use delay here
            //b/c its fab flashed(appear and disappear quickly) when data did not set to editTexts
            delay(500)
            viewModel.submitButtonState.isSubmitButtonEnable
                .collect {
                    Log.d("DetailEditTransactionVi", "setupUi: $it ")
                    backStackForDialog.isEnabled = it
                    topAppBar.title = if (it) {
                        fab_submit.show()
                        getString(R.string.edit_transaction)
                    } else {
                        fab_submit.hide()
                        getString(R.string.details)
                    }
                }
        }

        /**
         * on clicks
         */
        category_fab.setOnClickListener {
            viewModel.setPresenterState(SelectingCategoryState)
        }
        bottom_sheet_close_btn.setOnClickListener {
            viewModel.setPresenterState(NoneState)
        }
        edt_memo.setOnClickListener {
            viewModel.setPresenterState(AddingNoteState)
        }
        edt_memo.addTextChangedListener {
            Log.d(TAG, "setupUi: edt_memo: ${it.toString()} ")
            viewModel.onMemoChanged(it.toString())
        }
        edt_money.addTextChangedListener {
            viewModel.onMoneyChanged(it.toString(), resources)
        }
        fab_submit.setOnClickListener {
            updateTransaction()
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
                if (stateMessage.response.message ==
                    getString(R.string.transaction_successfully_deleted)
                ) {
                    uiCommunicationListener.hideSoftKeyboard()
                    navigateBack()
                }
                if (stateMessage.response.message == getString(R.string.transaction_successfully_updated)) {
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

    override fun handleLoading() {
        viewModel.countOfActiveJobs.observe(
            viewLifecycleOwner
        ) {
            showProgressBar(viewModel.areAnyJobsActive())
        }
    }

    private fun subscribeObservers() {
        viewModel.viewState.observe(viewLifecycleOwner) { vs ->
            vs?.let { viewState ->
                viewState.transaction?.let { setTransactionFields(it) }
                viewState.moneyStr?.let { setMoneyFields(it) }
                viewState.combineCalender?.let { setCombineFields(it) }
                viewState.presenterState?.getContentIfNotHandled()
                    ?.let { handlePresenterStateChange(it) }
                viewState.allOfCategories?.let { setAllOfCategoriesFields(it) }

            }
        }
    }

    private fun handlePresenterStateChange(newState: DetailEditTransactionPresenterState) =
        when (newState) {

            is SelectingCategoryState -> {
                btmsheetViewPagerAdapter.submitSelectedItemId(viewModel.getTransactionCategoryId())
                bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
                category_fab.hide()
                viewModel.submitButtonState.forcedHidden(true)
                uiCommunicationListener.hideSoftKeyboard()
                disableContentInteraction(edt_memo)
            }

            is EnteringAmountOfMoneyState -> {
                bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
                category_fab.show()
                viewModel.submitButtonState.forcedHidden(false)
                disableContentInteraction(edt_memo)
//                uiCommunicationListener.hideSoftKeyboard()
                showCustomKeyboard(edt_money)
            }
            is AddingNoteState -> {
                bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
                category_fab.show()
                viewModel.submitButtonState.forcedHidden(false)
                hideCustomKeyboard()
                enableContentInteraction(edt_memo)
                forceKeyBoardToOpenForEditText(edt_memo)
            }
            is ChangingDateState -> {
                bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
                category_fab.show()
                viewModel.submitButtonState.forcedHidden(false)
                uiCommunicationListener.hideSoftKeyboard()
                hideCustomKeyboard()
                disableContentInteraction(edt_memo)
                //apply
                showDatePickerDialog(viewModel.getCombineCalender())
            }

            is ChangingTimeState -> {
                bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
                category_fab.show()
                viewModel.submitButtonState.forcedHidden(false)
                uiCommunicationListener.hideSoftKeyboard()
                hideCustomKeyboard()
                disableContentInteraction(edt_memo)
                //apply
                showTimePickerDialog(viewModel.getCombineCalender())
            }


            is NoneState -> {
                bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
                category_fab.show()
                viewModel.submitButtonState.forcedHidden(false)
                uiCommunicationListener.hideSoftKeyboard()
                hideCustomKeyboard()
                disableContentInteraction(edt_memo)

            }
        }


    private fun setAllOfCategoriesFields(list: List<Category>) {
        btmsheetViewPagerAdapter.submitData(list)
    }

    private fun setTransactionFields(transaction: Transaction) {
        //set category name and image to fab
        setCategoryFields(
            transaction.getCategoryNameFromStringFile(
                resources,
                requireActivity().packageName
            ) { it.categoryName },
            transaction.categoryImage
        )

        //set memo
        if (edt_memo.text.toString() != transaction.memo) {
            edt_memo.setText(transaction.memo)
        }

        //set date to edit text
        setDateToEditTexts(((transaction.date).toLong()).times(1_000))

    }

    private fun setMoneyFields(money: String) {
        if (edt_money.text.toString() != money) {
            keyboard.preloadKeyboard(money)
        }
    }

    private fun setCategoryFields(
        name: String,
        categoryImage: String
    ) {
        //set name and icon
        category_fab.text = name
        category_fab.extend()

        val resourceId: Int = requireActivity().resources.getIdentifier(
            "ic_cat_${categoryImage}",
            "drawable",
            requireActivity().packageName
        )
        category_fab.icon = VectorDrawableCompat.create(resources, resourceId, null)
    }

    private fun setCombineFields(calendar: GregorianCalendar) {
        setDateToEditTexts(calendar.timeInMillis)
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
        if (newState == BottomSheetBehavior.STATE_HIDDEN && viewModel.getTransactionCategoryId() != null) {
            //bottomSheet slide animation stuff stuff
            if (edt_money.text.toString().isBlank()) {
                viewModel.setPresenterState(EnteringAmountOfMoneyState)
            } else {
                viewModel.setPresenterState(NoneState)
            }
        }
    }


    override fun onItemSelected(position: Int, item: Category) {
        //on category changed
        viewModel.setTransactionCategory(item)
        viewModel.setPresenterState(NoneState)
    }

    private fun checkForDelete(transactionId: Int?) {
        if (transactionId == null) {
            //show toast error
            viewModel.addToMessageStack(
                getString(R.string.unable_to_delete_no_transaction_found),
                Throwable("$TAG : deleteTransaction: viewTransactionId is null!  viewTransactionId = $transactionId"),
                UIComponentType.Toast,
                MessageType.Error
            )
        } else {
            val callback = object : AreYouSureCallback {
                override fun proceed() {
                    viewModel.deleteTransaction(transactionId)
                }

                override fun cancel() {}
            }
            viewModel.addToMessageStack(
                message = getString(R.string.are_you_sure_delete_transaction),
                uiComponentType = UIComponentType.AreYouSureDialog(
                    callback
                ),
                messageType = MessageType.Info
            )
        }
    }

    private fun updateTransaction() {
        fab_submit.isEnabled = false
        getTransactionEntityFiled()?.let {
            viewModel.updateTransaction(it)
        } ?: run {
            fab_submit.isEnabled = true
        }
    }

    private fun getTransactionEntityFiled(): TransactionEntity? {

        val transaction = viewModel.getTransaction()

        if (transaction == null) {
            showSnackBar(R.string.no_transaction_found)
            navigateBack()
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

        val type = viewModel.getTransactionCategoryType()

        if (type == null) {
            showSnackBar(R.string.unknown_category_type)
            viewModel.setPresenterState(SelectingCategoryState)
            return null
        }

        if (type == Constants.EXPENSES_TYPE_MARKER) {
            money = money.times(-1)
        }

        return TransactionEntity(
            id = transaction.id,
            money = money,
            memo = edt_memo.text.toString(),
            cat_id = transaction.categoryId,
            date = (calender.timeInMillis).div(1_000).toInt()
        )
    }

    private val backStackForDialog = object : OnBackPressedCallback(false) {
        override fun handleOnBackPressed() {
            if (this.isEnabled) {
                showDiscardOrSaveDialog()
            } else {
                uiCommunicationListener.hideSoftKeyboard()
                navigateBack()
            }
        }

    }

    private fun showDiscardOrSaveDialog() {
        val callback = object : DiscardOrSaveCallback {
            override fun save() {
                updateTransaction()
            }

            override fun discard() {
                uiCommunicationListener.hideSoftKeyboard()
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


}