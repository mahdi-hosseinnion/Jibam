package com.ssmmhh.jibam.ui.main.transaction.addedittransaction.common

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.text.*
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputConnection
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.core.content.res.ResourcesCompat
import androidx.core.text.TextUtilsCompat
import androidx.core.view.ViewCompat
import androidx.lifecycle.lifecycleScope
import com.alirezaafkar.sundatepicker.DatePicker
import com.bumptech.glide.RequestManager
import com.ssmmhh.jibam.R
import com.ssmmhh.jibam.ui.main.transaction.addedittransaction.categorybottomsheet.CategoryBottomSheetListAdapter
import com.ssmmhh.jibam.ui.main.transaction.addedittransaction.categorybottomsheet.CategoryBottomSheetViewPagerAdapter
import com.ssmmhh.jibam.ui.main.transaction.common.BaseFragment
import com.ssmmhh.jibam.util.*
import com.ssmmhh.jibam.util.PreferenceKeys.CALENDAR_SOLAR
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.fragment_add_transaction.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@ExperimentalCoroutinesApi
@FlowPreview
abstract class AddEditTransactionParentFragment
constructor(
    private val requestManager: RequestManager,
    private val currentLocale: Locale,
    private val sharedPreferences: SharedPreferences,
    private val sharedPrefsEditor: SharedPreferences.Editor,
    @StringRes private val fab_text: Int
) : BaseFragment(
    R.layout.fragment_add_transaction
), CalculatorKeyboard.CalculatorInteraction, CategoryBottomSheetListAdapter.Interaction {

    val textCalculator = TextCalculator()

    lateinit var btmsheetViewPagerAdapter: CategoryBottomSheetViewPagerAdapter

    lateinit var bottomSheetBehavior: BottomSheetBehavior<LinearLayout>

    private val bottomSheetCallback = object : BottomSheetBehavior.BottomSheetCallback() {

        override fun onStateChanged(bottomSheet: View, newState: Int) {
            onBottomSheetStateChanged(newState)
        }

        override fun onSlide(bottomSheet: View, slideOffset: Float) {
            //bottomSheet slide animation stuff stuff
            transaction_detail_container.alpha = 1f.minus(slideOffset)
        }
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initUi()
    }

    private fun initUi() {
        fab_submit.text = getString(fab_text)
        fab_submit.icon =
            ResourcesCompat.getDrawable(resources, R.drawable.ic_check_green_24dp, requireContext().theme)

        edt_money.addTextChangedListener(onTextChangedListener)

        setupBottomSheet()
        // prevent system keyboard from appearing when EditText is tapped
        edt_money.setRawInputType(InputType.TYPE_CLASS_TEXT)
        edt_money.setTextIsSelectable(true)

        // pass the InputConnection from the EditText to the keyboard
        val ic: InputConnection = edt_money.onCreateInputConnection(EditorInfo())
        keyboard.inputConnection = ic
        keyboard.calculatorInteraction = this
        keyboard._resources = resources
        keyboard.setTextToAllViews()
        //controll visibity

        // Make the custom keyboard appear
        edt_money.onFocusChangeListener = View.OnFocusChangeListener { v, hasFocus ->
            onMoneyEditTextFocusChanged(hasFocus)
        }

        edt_money.setOnTouchListener { view, motionEvent ->
            val inType: Int = edt_money.getInputType() // Backup the input type
            edt_money.inputType = InputType.TYPE_NULL // Disable standard keyboard
            edt_money.onTouchEvent(motionEvent)               // Call native handler
            edt_money.inputType = inType // Restore input type
            view.performClick()
            return@setOnTouchListener true // Consume touch event
        }
        edt_money.setOnClickListener {
            onClickedOnMoneyEditText()

        }
        transaction_detail_container.setOnClickListener {
            onClickedOnEmptyOfDetailContainer()
        }
        edt_date_sp.setOnClickListener {
            onClickedOnDate()
        }
        edt_time.setOnClickListener {
            onClickedOnTime()
        }
        uiCommunicationListener.hideSoftKeyboard()
        setupCategoryBottomSheet()
    }

    override fun onPause() {
        super.onPause()
        if (edt_money.hasFocus()){
            edt_money.clearFocus()
        }
    }
    private fun setupBottomSheet() {

        bottomSheetBehavior = BottomSheetBehavior.from(select_category_bottom_sheet)
        bottomSheetBehavior.addBottomSheetCallback(bottomSheetCallback)
        bottomSheetBehavior.isHideable = true
        bottomSheetBehavior.skipCollapsed = true
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
    }

    private fun setupCategoryBottomSheet() {
        val isLeftToRight =
            (TextUtilsCompat.getLayoutDirectionFromLocale(Locale.getDefault()) == ViewCompat.LAYOUT_DIRECTION_LTR)
        //viewpager
        btmsheetViewPagerAdapter = CategoryBottomSheetViewPagerAdapter(
            context = this.requireContext(),
            categoryList = null,
            interaction = this,
            requestManager = requestManager,
            isLeftToRight = isLeftToRight,
            packageName = this.requireActivity().packageName,
            selectedCategoryId = null
        )

        bottom_sheet_viewpager.adapter = btmsheetViewPagerAdapter
        category_tab_layout.setupWithViewPager(bottom_sheet_viewpager)

        if (!isLeftToRight) {
            bottom_sheet_viewpager.currentItem = CategoryBottomSheetViewPagerAdapter.VIEW_PAGER_SIZE
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            category_tab_layout?.layoutDirection = View.LAYOUT_DIRECTION_LTR
        } else {
            //TODO TEST THIS
            category_tab_layout?.let {
                ViewCompat.setLayoutDirection(
                    it,
                    ViewCompat.LAYOUT_DIRECTION_LTR
                )
            }
        }

    }

    fun setDateToEditTexts(unixTimeInMillis: Long) {
        disableContentInteraction(edt_date_sp)
        disableContentInteraction(edt_time)

        val date = dateWithPattern(unixTimeInMillis)
        val time = timeWithPattern(unixTimeInMillis)

        edt_date_sp.setText(date)
        edt_time.setText(time)

    }

    fun showDatePickerDialog(calender: GregorianCalendar) {
        val calendarType = sharedPreferences.getString(
            PreferenceKeys.APP_CALENDAR_PREFERENCE,
            PreferenceKeys.calendarDefault(currentLocale)
        )

        if (calendarType == CALENDAR_SOLAR) {
            showShamsiDatePicker(calender)
        } else {
            showGregorianDatePicker(calender)
        }
    }

    private fun showShamsiDatePicker(calender: GregorianCalendar) {
        DatePicker.Builder()
            .date(calender)
            .minDate(
                SolarCalendar.minShamsiYear,
                SolarCalendar.minShamsiMonth,
                SolarCalendar.minShamsiDay
            )
            .maxDate(
                SolarCalendar.maxShamsiYear,
                SolarCalendar.maxShamsiMonth,
                SolarCalendar.maxShamsiDay
            )
            .build { id, calendar, day, month, year ->
                removeDatePickerFromScreen()
                if (calendar != null) {
                    setToCombineCalender(
                        year = calendar[Calendar.YEAR],
                        month = calendar[Calendar.MONTH],
                        day = calendar[Calendar.DAY_OF_MONTH]
                    )
                } else {
                    //TODO add backup plan and shamsi to gregorian converter
                    Toast.makeText(
                        this.requireContext(),
                        getString(R.string.unable_to_get_date),
                        Toast.LENGTH_LONG
                    ).show()
                }
            }.show(childFragmentManager, "ShamsiDatePicker")
    }


    private fun showGregorianDatePicker(calender: GregorianCalendar) {

        val datePickerDialog =
            DatePickerDialog(
                this.requireContext(),
                { _, year, monthOfYear, dayOfMonth ->
                    removeDatePickerFromScreen()
                    setToCombineCalender(
                        year = year,
                        month = monthOfYear,
                        day = dayOfMonth
                    )
                },
                calender.get(Calendar.YEAR),
                calender.get(Calendar.MONTH),
                calender.get(Calendar.DAY_OF_MONTH)
            )
        datePickerDialog.datePicker.minDate = SolarCalendar.minGregorianDate
        datePickerDialog.datePicker.maxDate = SolarCalendar.maxGregorianDate
        datePickerDialog.show()
    }

    fun showTimePickerDialog(calender: GregorianCalendar) {

        //show picker
        val timePickerDialog =
            TimePickerDialog(
                this.requireContext(),
                { _, hourOfDay, minute ->
                    removeTimePickerFromScreen()
                    setToCombineCalender(GregorianCalendar.HOUR_OF_DAY, hourOfDay)
                    setToCombineCalender(GregorianCalendar.MINUTE, minute)
                },
                calender.get(GregorianCalendar.HOUR_OF_DAY),
                calender.get(GregorianCalendar.MINUTE),
                false
            )
        //TODO MAKE farst time picker SUPPORT IN FARSI
//        timePickerDialog.setButton(TimePickerDialog.BUTTON_POSITIVE,_getString(R.string.capital_ok),timePickerDialog)
//        timePickerDialog.setButton(TimePickerDialog.BUTTON_NEGATIVE,_getString(R.string.capital_cancel),timePickerDialog)
        timePickerDialog.show()
    }

    fun forceKeyBoardToOpenForEditText(editText: EditText) {
        editText.requestFocus()
        val imm: InputMethodManager =
            activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT)
    }

    fun enableContentInteraction(edt: EditText) {
        edt.keyListener = EditText(this.requireContext()).keyListener
        edt.isFocusable = true
        edt.isFocusableInTouchMode = true
        edt.isCursorVisible = true
        edt.requestFocus()
    }

    fun disableContentInteraction(edt: EditText) {
        edt.keyListener = null
        edt.isFocusable = false
        edt.isFocusableInTouchMode = false
        edt.isCursorVisible = false
        edt.clearFocus()
    }


    private fun dateWithPattern(unixTimeInMillis: Long): String {
        val calendarType = sharedPreferences.getString(
            PreferenceKeys.APP_CALENDAR_PREFERENCE,
            PreferenceKeys.calendarDefault(currentLocale)
        )

        return if (calendarType == CALENDAR_SOLAR) {
            SolarCalendar.calcSolarCalendar(
                unixTimeInMillis,
                SolarCalendar.ShamsiPatterns.DETAIL_FRAGMENT,
                resources,
                currentLocale
            )
        } else {
            val df = Date(unixTimeInMillis)
            SimpleDateFormat(DATE_PATTERN, currentLocale).format(df)
        }
    }

    private fun timeWithPattern(unixTimeInMillis: Long): String {
        val df = Date(unixTimeInMillis)
        return SimpleDateFormat(TIME_PATTERN, currentLocale).format(df)
    }

    fun showCustomKeyboard(view: View) {
        val imm: InputMethodManager =
            activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
        keyboard.visibility = View.VISIBLE

        //change bottom margin of fab
        val _16Dp = convertDpToPx(16)

        changeFabBottomMargin(keyboard.height.plus(_16Dp))
        //b/c when fragment is created keyboard is not visible and keyboard height is 0
        //so we check 10 times with 100 wait each time for keyboard to show up if it does'nt
        //show up we don't care b/c if focus change this method will be call again
        lifecycleScope.launch {
            for (i in 1..10) {
                delay(100)
                if (keyboard.height > 0) {
                    changeFabBottomMargin(keyboard.height.plus(_16Dp))
                    break
                }
            }
        }


    }

    fun hideCustomKeyboard() {
        keyboard.visibility = View.GONE
        val _16Dp = convertDpToPx(16)
        //change bottom margin of fab
        changeFabBottomMargin()
    }

    private fun changeFabBottomMargin(marginBottom: Int? = null) {
        val _16Dp = convertDpToPx(16)
        if (fab_submit.isShown) {
            //TODO ADD SLIDE ANIMATION HERE
//            fab_submit.hide()
            fab_submit.setMargins(_16Dp, _16Dp, _16Dp, marginBottom ?: _16Dp)
//            fab_submit.show()
        } else {
            fab_submit.setMargins(_16Dp, _16Dp, _16Dp, marginBottom ?: _16Dp)
        }
    }

    fun View.setMargins(l: Int, t: Int, r: Int, b: Int) {
        if (this.layoutParams is ViewGroup.MarginLayoutParams) {
            val p = this.layoutParams as ViewGroup.MarginLayoutParams
            p.setMargins(l, t, r, b)
            this.requestLayout()
        }
    }

    override fun onEqualClicked() {
        keyboard.preloadKeyboard(finalNUmber.text.toString())
    }

    private val onTextChangedListener = object : TextWatcher {
        override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

        override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

        override fun afterTextChanged(p0: Editable?) {
            edt_money.removeTextChangedListener(this)
            //calculate result of main edittext
            val text = p0.toString()

            if (text.indexOfAny(chars = listOfNumbers()) >= 0) {
                val calculatedResult = textCalculator.calculateResult(text)

                val finalNumberText =
                    localizeDoubleNumber(calculatedResult.toDouble(), currentLocale)

                finalNUmber.text =
                    if (finalNumberText == edt_money.text.toString().removeOperationSigns()) ""
                    else finalNumberText

            } else {
                finalNUmber.text = ""

            }

            edt_money.addTextChangedListener(this)
        }

    }

    private fun listOfNumbers(): CharArray =
        charArrayOf(
            getString(R.string._1)[0],
            getString(R.string._2)[0],
            getString(R.string._3)[0],
            getString(R.string._4)[0],
            getString(R.string._5)[0],
            getString(R.string._6)[0],
            getString(R.string._7)[0],
            getString(R.string._8)[0],
            getString(R.string._9)[0]
        )

    fun String.removeOperationSigns(): String {
        var result = this
        for (values in CalculatorKeyboard.listOfSigns) {
            result = result.replace(values, "")
        }
        return result
    }


    fun showSnackBar(@StringRes resId: Int) {
        Snackbar.make(
            bottomCoordinator,
            getString(resId),
            Snackbar.LENGTH_SHORT
        ).setAnchorView(fab_submit).show()

    }

    /**
     *     abstract functions
     */


    abstract fun setToCombineCalender(year: Int, month: Int, day: Int)

    abstract fun setToCombineCalender(field: Int, value: Int)

    abstract fun onMoneyEditTextFocusChanged(hasFocus: Boolean)

    abstract fun onClickedOnMoneyEditText()

    abstract fun onClickedOnEmptyOfDetailContainer()

    abstract fun onClickedOnDate()

    abstract fun onClickedOnTime()

    abstract fun removeDatePickerFromScreen()

    abstract fun removeTimePickerFromScreen()

    abstract fun onBottomSheetStateChanged(newState: Int)


    companion object {
        private const val TAG = "AddEditTransactionParen"

        private const val TIME_PATTERN = "KK:mm aa"

        //for shamsi date it's hard coded in SolarCalendar class
        private const val DATE_PATTERN = "MM/dd/yy (E)"


        //errors
        private enum class ErrorMessages {
            PLEASE_SELECT_CATEGORY,
            NEGATIVE_MONEY,
            EMPTY_MONEY
        }


    }
}