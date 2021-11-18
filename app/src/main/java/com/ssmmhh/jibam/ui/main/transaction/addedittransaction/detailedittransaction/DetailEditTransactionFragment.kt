package com.ssmmhh.jibam.ui.main.transaction.addedittransaction.detailedittransaction

import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat
import com.bumptech.glide.RequestManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.ssmmhh.jibam.R
import com.ssmmhh.jibam.databinding.FragmentAddTransactionBinding
import com.ssmmhh.jibam.models.Category
import com.ssmmhh.jibam.models.Transaction
import com.ssmmhh.jibam.models.TransactionEntity
import com.ssmmhh.jibam.ui.main.transaction.addedittransaction.common.AddEditTransactionParentFragment
import com.ssmmhh.jibam.ui.main.transaction.addedittransaction.detailedittransaction.state.DetailEditTransactionPresenterState
import com.ssmmhh.jibam.ui.main.transaction.addedittransaction.detailedittransaction.state.DetailEditTransactionPresenterState.*
import com.ssmmhh.jibam.util.*
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
        binding.toolbar?.topAppBarNormal?.title = getString(R.string.details)

        //add backstack listener for discard dialog
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            backStackForDialog
        )
        binding.toolbar?.topAppBarNormal?.setNavigationOnClickListener {
            backStackForDialog.handleOnBackPressed()
        }
        binding.toolbar?.topAppBarImgBtn?.visibility = View.VISIBLE

        binding.toolbar?.topAppBarImgBtn?.setOnClickListener {
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
                    binding.toolbar?.topAppBarNormal?.title = if (it) {
                        binding.fabSubmit.show()
                        getString(R.string.edit_transaction)
                    } else {
                        binding.fabSubmit.hide()
                        getString(R.string.details)
                    }
                }
        }

        /**
         * on clicks
         */
        binding.categoryFab.setOnClickListener {
            viewModel.setPresenterState(SelectingCategoryState)
        }
        binding.bottomSheetCloseBtn.setOnClickListener {
            viewModel.setPresenterState(NoneState)
        }
        binding.edtMemo.setOnClickListener {
            viewModel.setPresenterState(AddingNoteState)
        }
        binding.edtMemo.addTextChangedListener {
            Log.d(TAG, "setupUi: edt_memo: ${it.toString()} ")
            viewModel.onMemoChanged(it.toString())
        }
        binding.edtMoney.addTextChangedListener {
            viewModel.onMoneyChanged(it.toString(), resources)
        }
        binding.fabSubmit.setOnClickListener {
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
                    binding.fabSubmit.isEnabled = true
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
                binding.categoryFab.hide()
                viewModel.submitButtonState.forcedHidden(true)
                uiCommunicationListener.hideSoftKeyboard()
                disableContentInteraction(binding.edtMemo)
            }

            is EnteringAmountOfMoneyState -> {
                bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
                binding.categoryFab.show()
                viewModel.submitButtonState.forcedHidden(false)
                disableContentInteraction(binding.edtMemo)
//                uiCommunicationListener.hideSoftKeyboard()
                showCustomKeyboard(binding.edtMoney)
            }
            is AddingNoteState -> {
                bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
                binding.categoryFab.show()
                viewModel.submitButtonState.forcedHidden(false)
                hideCustomKeyboard()
                enableContentInteraction(binding.edtMemo)
                forceKeyBoardToOpenForEditText(binding.edtMemo)
            }
            is ChangingDateState -> {
                bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
                binding.categoryFab.show()
                viewModel.submitButtonState.forcedHidden(false)
                uiCommunicationListener.hideSoftKeyboard()
                hideCustomKeyboard()
                disableContentInteraction(binding.edtMemo)
                //apply
                showDatePickerDialog(viewModel.getCombineCalender())
            }

            is ChangingTimeState -> {
                bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
                binding.categoryFab.show()
                viewModel.submitButtonState.forcedHidden(false)
                uiCommunicationListener.hideSoftKeyboard()
                hideCustomKeyboard()
                disableContentInteraction(binding.edtMemo)
                //apply
                showTimePickerDialog(viewModel.getCombineCalender())
            }


            is NoneState -> {
                bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
                binding.categoryFab.show()
                viewModel.submitButtonState.forcedHidden(false)
                uiCommunicationListener.hideSoftKeyboard()
                hideCustomKeyboard()
                disableContentInteraction(binding.edtMemo)

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
        if (binding.edtMemo.text.toString() != transaction.memo) {
            binding.edtMemo.setText(transaction.memo)
        }

        //set date to edit text
        setDateToEditTexts(((transaction.date).toLong()).times(1_000))

    }

    private fun setMoneyFields(money: String) {
        if (binding.edtMoney.text.toString() != money) {
            binding.keyboard.preloadKeyboard(money)
        }
    }

    private fun setCategoryFields(
        name: String,
        categoryImage: String
    ) {
        //set name and icon
        binding.categoryFab.text = name
        binding.categoryFab.extend()

        val resourceId: Int = requireActivity().resources.getIdentifier(
            "ic_cat_${categoryImage}",
            "drawable",
            requireActivity().packageName
        )
        binding.categoryFab.icon = VectorDrawableCompat.create(resources, resourceId, null)
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
            if (binding.edtMoney.text.toString().isBlank()) {
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
        binding.fabSubmit.isEnabled = false
        getTransactionEntityFiled()?.let {
            viewModel.updateTransaction(it)
        } ?: run {
            binding.fabSubmit.isEnabled = true
        }
    }

    private fun getTransactionEntityFiled(): TransactionEntity? {

        val transaction = viewModel.getTransaction()

        if (transaction == null) {
            showSnackBar(R.string.no_transaction_found)
            navigateBack()
            return null
        }
        val moneyEditTextStr = binding.edtMoney.text.toString()

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
            memo = binding.edtMemo.text.toString(),
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