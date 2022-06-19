package com.ssmmhh.jibam.presentation.addedittransaction

import android.app.TimePickerDialog
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.LinearLayout
import android.widget.TimePicker
import androidx.core.text.TextUtilsCompat
import androidx.core.view.ViewCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.RequestManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetBehavior.*
import com.ssmmhh.jibam.R
import com.ssmmhh.jibam.data.model.Category
import com.ssmmhh.jibam.data.model.DateHolderWithWeekDay
import com.ssmmhh.jibam.databinding.FragmentAddEditTransactionBinding
import com.ssmmhh.jibam.presentation.addedittransaction.common.CategoryBottomSheetListAdapter
import com.ssmmhh.jibam.presentation.addedittransaction.common.CategoryBottomSheetViewPagerAdapter
import com.ssmmhh.jibam.presentation.common.BaseFragment
import com.ssmmhh.jibam.presentation.util.ToolbarLayoutListener
import com.ssmmhh.jibam.util.*
import com.ssmmhh.jibam.util.DateUtils.toSeconds
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import java.text.SimpleDateFormat
import java.util.*

@FlowPreview
@ExperimentalCoroutinesApi
class AddEditTransactionFragment(
    viewModelFactory: ViewModelProvider.Factory,
    private val requestManager: RequestManager,
    private val sharedPreferences: SharedPreferences,
) : BaseFragment(), ToolbarLayoutListener, CategoryBottomSheetListAdapter.Interaction {

    private lateinit var binding: FragmentAddEditTransactionBinding

    private val viewModel by viewModels<AddEditTransactionViewModel> { viewModelFactory }

    private val navigationArgs: AddEditTransactionFragmentArgs by navArgs()

    lateinit var selectCategoryBottomSheetBehavior: BottomSheetBehavior<LinearLayout>

    lateinit var categoryBottomSheetViewPagerAdapter: CategoryBottomSheetViewPagerAdapter


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentAddEditTransactionBinding.inflate(inflater, container, false).apply {
            this.listener = this@AddEditTransactionFragment
            this.lifecycleOwner = this@AddEditTransactionFragment.viewLifecycleOwner
            this.viewmodel = viewModel
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        start(navigationArgs.transactionId)
        initializeSelectCategoryBottomSheet()
        setupBottomSheetViewPager()
        setupCalculatorKeyboard()
        subscribeObservers()
    }

    private fun start(transactionId: Int) {
        if (transactionId >= 0) {
            //Transaction Detail
            binding.toolbarTitle = getString(R.string.details)
            binding.fabSubmit.text = getString(R.string.update)
            viewModel.startWithTransaction(transactionId)
        } else {
            //Add new transaction (transactionId will be -1)
            binding.toolbarTitle = getString(R.string.add_transaction)
            binding.fabSubmit.text = getString(R.string.save)
            viewModel.startNewTransaction()
        }
        binding.fabSubmit.extend()
    }

    private fun initializeSelectCategoryBottomSheet() {
        selectCategoryBottomSheetBehavior = from(binding.selectCategoryBottomSheet).apply {
            addBottomSheetCallback(selectCategoryBottomSheetBehaviorCallback)
            isHideable = true
            skipCollapsed = true
            state = STATE_HIDDEN
        }
        binding.bottomSheetCloseBtn.setOnClickListener {
            viewModel.hideSelectCategoryBottomSheet()
        }

    }

    private fun setupBottomSheetViewPager() {
        val isLayoutDirectionLeftToRight =
            (TextUtilsCompat.getLayoutDirectionFromLocale(Locale.getDefault()) == ViewCompat.LAYOUT_DIRECTION_LTR)
        //viewpager
        categoryBottomSheetViewPagerAdapter = CategoryBottomSheetViewPagerAdapter(
            context = this.requireContext(),
            categoryEntityList = null,
            interaction = this,
            requestManager = requestManager,
            isLeftToRight = isLayoutDirectionLeftToRight,
            selectedCategoryId = viewModel.transactionCategory.value?.id
        )


        binding.bottomSheetViewpager.adapter = categoryBottomSheetViewPagerAdapter
        binding.categoryTabLayout.layoutDirection = View.LAYOUT_DIRECTION_LTR
        binding.categoryTabLayout.setupWithViewPager(binding.bottomSheetViewpager)
        if (!isLayoutDirectionLeftToRight) {
            binding.bottomSheetViewpager.currentItem =
                CategoryBottomSheetViewPagerAdapter.VIEW_PAGER_SIZE
        }
    }

    private fun setupCalculatorKeyboard() {
        // prevent system keyboard from appearing when EditText is tapped
        binding.edtMoney.apply {
            setRawInputType(InputType.TYPE_CLASS_TEXT)
            setTextIsSelectable(true)
            //Prevents the device keyboard from popping up and keeps the cursor visible.
            setOnTouchListener { view, motionEvent ->
                val inType: Int = inputType // Backup the input type
                binding.edtMoney.inputType = InputType.TYPE_NULL // Disable standard keyboard
                binding.edtMoney.onTouchEvent(motionEvent)               // Call native handler
                binding.edtMoney.inputType = inType // Restore input type
                view.performClick()
                return@setOnTouchListener true // Consume touch event
            }
            setOnClickListener {
                openCalculatorKeyboard()
            }
            addTextChangedListener(separateNumber3By3TextChangeListener)

        }
        binding.calculatorKeyboard.attachEditText(binding.edtMoney)
        binding.calculatorKeyboard.setOnEqualButtonClicked {

        }

    }

    private fun openCalculatorKeyboard() {
        //Hide device's soft keyboard
        (requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager).apply {
            hideSoftInputFromWindow(view?.windowToken, 0)
        }
        //Open calculator keyboard
        binding.calculatorKeyboard.visibility = View.VISIBLE
    }

    private fun subscribeObservers() {
        viewModel.showSelectCategoryBottomSheet.observe(viewLifecycleOwner) {
            handleSelectCategoryBottomSheetState(it)
        }
        viewModel.categories.observe(viewLifecycleOwner) {
            it?.let { categoryBottomSheetViewPagerAdapter.submitData(it) }
        }
        viewModel.transactionCategory.observe(viewLifecycleOwner) {
            selectCategoryBottomSheetBehavior.isDraggable = it != null
            binding.bottomSheetCloseBtn.visibility = if (it != null) View.VISIBLE else View.GONE
        }
        viewModel.transactionDate.observe(viewLifecycleOwner) {
            it?.let { calendar ->
                setTimeToEditText(calendar)
                setDateToEditText(calendar)
            }
        }
        viewModel.showTimePickerDialog.observe(viewLifecycleOwner) {
            if (it)
                showTimePickerDialog()
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

    private fun setTimeToEditText(calendar: GregorianCalendar) {
        val time = SimpleDateFormat(TIME_PATTERN, locale).format(calendar.time)
        binding.edtTime.setText(time)
    }


    private fun setDateToEditText(calendar: GregorianCalendar) {

        val isCalendarSolarHijri = sharedPreferences.isCalendarSolar(locale)

        val date: DateHolderWithWeekDay = DateUtils.convertUnixTimeToDate(
            calendar.timeInMillis.toSeconds(),
            isCalendarSolarHijri
        )

        val dayOfWeekStr = date.getDayOfWeekName(resources)
        val dayStr = date.day.toLocaleStringWithTwoDigits()
        val monthStr = date.month.toLocaleStringWithTwoDigits()
        val yearStr = date.year.toLocaleString()

        val result = if (isCalendarSolarHijri) {
            //Solar hijri calendar date format
            "$yearStr/$monthStr/$dayStr ($dayOfWeekStr)"
        } else {
            //Gregorian calendar date format
            "$monthStr/$dayStr/$yearStr ($dayOfWeekStr)"
        }
        binding.edtDateSp.setText(result)
    }

    private fun showTimePickerDialog() {
        val currentTime = viewModel.transactionDate.value ?: return
        val onTimeSet: (TimePicker, Int, Int) -> Unit = { _, hourOfDay, minute ->
            viewModel.updateTransactionDateTime(hourOfDay, minute)
        }
        val dialog = TimePickerDialog(
            this.requireContext(),
            onTimeSet,
            currentTime.get(GregorianCalendar.HOUR_OF_DAY),
            currentTime.get(GregorianCalendar.MINUTE),
            false
        )
        dialog.setOnDismissListener {
            viewModel.hideTimePickerDialog()
        }
        dialog.show()


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

    private val separateNumber3By3TextChangeListener = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

        override fun afterTextChanged(s: Editable?) {
            binding.edtMoney.removeTextChangedListener(this)
            //separate text in edtMoney 3by 3 and set it back
            val separated3By3Text = separateCalculatorText3By3(s.toString(), locale)
            val selectionPositionBeforeChangeText = binding.edtMoney.selectionStart
            binding.edtMoney.setText(separated3By3Text)

            val countOfSeparatorBeforeChange = (s.toString()).count { it == NUMBER_SEPARATOR }
            val countOfSeparatorAfterChange = separated3By3Text.count { it == NUMBER_SEPARATOR }
            try {
                //we use this code to determine 'newSelectionPosition' according to the count of
                // 'NUMBER_SEPARATOR' added to text
                val newSelectionPosition = selectionPositionBeforeChangeText.plus(
                    countOfSeparatorAfterChange.minus(countOfSeparatorBeforeChange)
                )
                binding.edtMoney.setSelection(newSelectionPosition)
            } catch (e: Exception) {
                binding.edtMoney.setSelection(binding.edtMoney.text.length)
            }
            binding.edtMoney.addTextChangedListener(this)

        }

    }

    override fun onClickOnMenuButton(view: View) {}

    override fun onItemSelected(position: Int, item: Category) {
        viewModel.setTransactionCategory(item)
        viewModel.hideSelectCategoryBottomSheet()
    }

    companion object {
        const val TIME_PATTERN = "KK:mm aa"
    }
}
