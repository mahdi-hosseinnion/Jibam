package com.example.jibi.ui.main.transaction.addedittransaction.detailedittransaction

import android.content.SharedPreferences
import android.content.res.Resources
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat
import com.bumptech.glide.RequestManager
import com.example.jibi.R
import com.example.jibi.di.main.MainScope
import com.example.jibi.models.Category
import com.example.jibi.models.Transaction
import com.example.jibi.ui.main.transaction.addedittransaction.common.AddEditTransactionParentFragment
import com.example.jibi.ui.main.transaction.addedittransaction.detailedittransaction.state.DetailEditTransactionPresenterState
import com.example.jibi.ui.main.transaction.addedittransaction.detailedittransaction.state.DetailEditTransactionPresenterState.*
import com.example.jibi.util.*
import com.google.android.material.bottomsheet.BottomSheetBehavior
import kotlinx.android.synthetic.main.fragment_add_transaction.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.debounce
import java.util.*
import javax.inject.Inject

@FlowPreview
@ExperimentalCoroutinesApi
@MainScope
class DetailEditTransactionFragment
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
    _resources = _resources,
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
        setHasOptionsMenu(true)
        lifecycleScope.launchWhenStarted {
            viewModel.submitButtonState.isSubmitButtonEnable
                .collect {
                    Log.d("DetailEditTransactionVi", "setupUi: $it ")
                    fragment_add_toolbar_main.title = if (it) {
                        fab_submit.show()
                        _getString(R.string.edit_transaction)
                    } else {
                        fab_submit.hide()
                        _getString(R.string.details)
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
            viewModel.onMoneyChanged(it.toString())
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
                    _getString(R.string.transaction_successfully_deleted)
                ) {
                    uiCommunicationListener.hideSoftKeyboard()
                    navigateBack()
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

    private fun handlePresenterStateChange(newState: DetailEditTransactionPresenterState)
      =  when (newState) {

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
                _resources,
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
        if (hasFocus)
            viewModel.setPresenterState(EnteringAmountOfMoneyState)
//        else
//            viewModel.setPresenterState(NoneState)
    }

    override fun onClickedOnMoneyEditText() {
        viewModel.setPresenterState(EnteringAmountOfMoneyState)
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
        if (newState == BottomSheetBehavior.STATE_HIDDEN) {
            //bottomSheet slide animation stuff stuff
            if (edt_money.text.toString().isBlank()) {
                viewModel.setPresenterState(EnteringAmountOfMoneyState)
            } else {
                viewModel.setPresenterState(NoneState)
            }
        }
    }


    override fun setTextToAllViews() {
        txtField_memo.hint = _getString(R.string.write_note)
        txtField_date.hint = _getString(R.string.date)
        edt_money.hint = _getString(R.string._0)
        fragment_add_toolbar_main.title = _getString(R.string.details)
    }

    override fun onItemSelected(position: Int, item: Category) {
        //on category changed
        viewModel.setTransactionCategory(item)
        viewModel.setPresenterState(NoneState)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.delete_menu, menu)

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.delete_transaction) {
            //delete stuff
            checkForDelete(viewModel.getDefaultTransaction()?.id)
        }
        return super.onOptionsItemSelected(item)
    }

    private fun checkForDelete(transactionId: Int?) {
        if (transactionId == null) {
            //show toast error
            viewModel.addToMessageStack(
                _getString(R.string.unable_to_delete_no_transaction_found),
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
                message = _getString(R.string.are_you_sure_delete_transaction),
                uiComponentType = UIComponentType.AreYouSureDialog(
                    callback
                ),
                messageType = MessageType.Info
            )
        }
    }

}