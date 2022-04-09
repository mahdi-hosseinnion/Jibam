package com.ssmmhh.jibam.presentation.addedittransaction.common

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.text.*
import android.util.Log
import android.view.LayoutInflater
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
import com.ssmmhh.jibam.presentation.common.BaseFragment
import com.ssmmhh.jibam.util.*
import com.ssmmhh.jibam.util.PreferenceKeys.CALENDAR_SOLAR
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.snackbar.Snackbar
import com.ssmmhh.jibam.databinding.FragmentAddTransactionBinding
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
) : BaseFragment(), CalculatorKeyboard.CalculatorInteraction,
    CategoryBottomSheetListAdapter.Interaction {

    private var _binding: FragmentAddTransactionBinding? = null

    val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentAddTransactionBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    val textCalculator = TextCalculator()

    lateinit var btmsheetViewPagerAdapter: CategoryBottomSheetViewPagerAdapter

    lateinit var bottomSheetBehavior: BottomSheetBehavior<LinearLayout>

    private val bottomSheetCallback = object : BottomSheetBehavior.BottomSheetCallback() {

        override fun onStateChanged(bottomSheet: View, newState: Int) {
            onBottomSheetStateChanged(newState)
        }

        override fun onSlide(bottomSheet: View, slideOffset: Float) {
            //bottomSheet slide animation stuff stuff
            binding.transactionDetailContainer.alpha = 1f.minus(slideOffset)
        }
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initUi()
    }

    private fun initUi() {
        binding.fabSubmit.text = getString(fab_text)
        binding.fabSubmit.icon =
            ResourcesCompat.getDrawable(
                resources,
                R.drawable.ic_check_green_24dp,
                requireContext().theme
            )

        binding.edtMoney.addTextChangedListener(onTextChangedListener)

        setupBottomSheet()
        // prevent system keyboard from appearing when EditText is tapped
        binding.edtMoney.setRawInputType(InputType.TYPE_CLASS_TEXT)
        binding.edtMoney.setTextIsSelectable(true)

        // pass the InputConnection from the EditText to the keyboard
        val ic: InputConnection = binding.edtMoney.onCreateInputConnection(EditorInfo())
        binding.keyboard.inputConnection = ic
        binding.keyboard.calculatorInteraction = this
        binding.keyboard.setTextToAllViews()
        //controll visibity

        binding.edtMoney.setOnTouchListener { view, motionEvent ->
            val inType: Int = binding.edtMoney.getInputType() // Backup the input type
            binding.edtMoney.inputType = InputType.TYPE_NULL // Disable standard keyboard
            binding.edtMoney.onTouchEvent(motionEvent)               // Call native handler
            binding.edtMoney.inputType = inType // Restore input type
            view.performClick()
            return@setOnTouchListener true // Consume touch event
        }
        binding.edtMoney.setOnClickListener {
            onClickedOnMoneyEditText()

        }
        binding.transactionDetailContainer.setOnClickListener {
            onClickedOnEmptyOfDetailContainer()
        }
        binding.edtDateSp.setOnClickListener {
            onClickedOnDate()
        }
        binding.edtTime.setOnClickListener {
            onClickedOnTime()
        }
        activityCommunicationListener.hideSoftKeyboard()
        setupCategoryBottomSheet()
    }

    override fun onPause() {
        super.onPause()
        if (binding.edtMoney.hasFocus()) {
            binding.edtMoney.clearFocus()
        }
    }

    private fun setupBottomSheet() {

        bottomSheetBehavior = BottomSheetBehavior.from(binding.selectCategoryBottomSheet)
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
            categoryEntityList = null,
            interaction = this,
            requestManager = requestManager,
            isLeftToRight = isLeftToRight,
            selectedCategoryId = null
        )

        binding.bottomSheetViewpager.adapter = btmsheetViewPagerAdapter
        binding.categoryTabLayout.setupWithViewPager(binding.bottomSheetViewpager)

        if (!isLeftToRight) {
            binding.bottomSheetViewpager.currentItem =
                CategoryBottomSheetViewPagerAdapter.VIEW_PAGER_SIZE
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            binding.categoryTabLayout.layoutDirection = View.LAYOUT_DIRECTION_LTR
        } else {
            //TODO TEST THIS
            binding.categoryTabLayout.let {
                ViewCompat.setLayoutDirection(
                    it,
                    ViewCompat.LAYOUT_DIRECTION_LTR
                )
            }
        }

    }

    fun setDateToEditTexts(unixTimeInMillis: Long) {
        disableContentInteraction(binding.edtDateSp)
        disableContentInteraction(binding.edtTime)

        val date = dateWithPattern(unixTimeInMillis)
        val time = timeWithPattern(unixTimeInMillis)

        binding.edtDateSp.setText(date)
        binding.edtTime.setText(time)

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
                minShamsiYear,
                minShamsiMonth,
                minShamsiDay
            )
            .maxDate(
                maxShamsiYear,
                maxShamsiMonth,
                maxShamsiDay
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
        datePickerDialog.datePicker.minDate = minGregorianDate
        datePickerDialog.datePicker.maxDate = maxGregorianDate
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
        edt.setSelection(edt.text.length)
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
            val date = convertUnixTimeToSolarHijri(unixTimeInMillis)
            val formattedYear = date.year
            val formattedMonth = date.month
            val formattedDay = date.day
            val dayOfWeekName = date.getDayOfWeekName(resources)
            "$formattedYear/$formattedMonth/${formattedDay} (${dayOfWeekName})"
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
        binding.keyboard.visibility = View.VISIBLE

        //change bottom margin of fab
        val _16Dp = convertDpToPx(16)

        changeFabBottomMargin(binding.keyboard.height.plus(_16Dp))
        //b/c when fragment is created keyboard is not visible and keyboard height is 0
        //so we check 10 times with 100 wait each time for keyboard to show up if it does'nt
        //show up we don't care b/c if focus change this method will be call again
        lifecycleScope.launch {
            for (i in 1..10) {
                delay(100)
                if (binding.keyboard.height > 0) {
                    changeFabBottomMargin(binding.keyboard.height.plus(_16Dp))
                    break
                }
            }
        }


    }

    fun hideCustomKeyboard() {
        binding.keyboard.visibility = View.GONE
        val _16Dp = convertDpToPx(16)
        //change bottom margin of fab
        changeFabBottomMargin()
    }

    private fun changeFabBottomMargin(marginBottom: Int? = null) {
        val _16Dp = convertDpToPx(16)
        //TODO ("Add slide animation to the fabSubmit while changing margin
        if (binding.fabSubmit.isShown) {
            binding.fabSubmit.setMargins(_16Dp, _16Dp, _16Dp, marginBottom ?: _16Dp)
        } else {
            binding.fabSubmit.setMargins(_16Dp, _16Dp, _16Dp, marginBottom ?: _16Dp)
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
        binding.keyboard.preloadKeyboard(binding.finalNUmber.text.toString().removeSeparateSign())
    }

    private val onTextChangedListener = object : TextWatcher {
        override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

        override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

        override fun afterTextChanged(p0: Editable?) {
            binding.edtMoney.removeTextChangedListener(this)
            //calculate result of main edittext
            val text = p0.toString().remove3By3Separators()

            if (text.indexOfAny(chars = listOfNumbers()) >= 0) {
                val calculatedResult = textCalculator.calculateResult(text)
                val finalNumberText =
                    localizeDoubleNumber(calculatedResult.toDoubleOrNull(), currentLocale)

                if (finalNumberText == null) {
                    binding.finalNUmber.text = getString(R.string.invalid_number_error)
                } else {
                    binding.finalNUmber.text =
                        if (finalNumberText == binding.edtMoney.text.toString()
                                .removeOperationSigns()
                        ) ""
                        else finalNumberText.convertFarsiDigitsToEnglishDigits()
                            .toBigDecimalOrNull()
                            ?.let { separate3By3(it, currentLocale) }
                            ?: finalNumberText
                }
            } else {
                binding.finalNUmber.text = ""

            }
            //separate text in edtMoney 3by 3 and set it back
            val separated3By3Text = separateCalculatorText3By3(p0.toString(), currentLocale)
            val selectionPositionBeforeChangeText = binding.edtMoney.selectionStart
            binding.edtMoney.setText(separated3By3Text)
            val countOfSeparatorBeforeChange = (p0.toString()).count { NUMBER_SEPARATOR == it }
            val countOfSeparatorAfterChange = separated3By3Text.count { NUMBER_SEPARATOR == it }
            try {
                //we use this code to determine 'newSelectionPosition' according to the count of
                // 'NUMBER_SEPARATOR' added to text
                val newSelectionPosition = selectionPositionBeforeChangeText.plus(
                    countOfSeparatorAfterChange.minus(countOfSeparatorBeforeChange)
                )
                binding.edtMoney.setSelection(newSelectionPosition)
            } catch (e: Exception) {
                Log.e(TAG, "afterTextChanged: ${e.message}", e)
                binding.edtMoney.setSelection(binding.edtMoney.text.length)
            }


            binding.edtMoney.addTextChangedListener(this)
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
        val operationSigns = (CalculatorKeyboard.listOfSigns).toMutableList()
        operationSigns.remove(CalculatorKeyboard.PERIOD)
        for (values in operationSigns) {
            result = result.replace(values, "")
        }
        return result
    }


    fun showSnackBar(@StringRes resId: Int) {
        Snackbar.make(
            binding.bottomCoordinator,
            getString(resId),
            Snackbar.LENGTH_SHORT
        ).setAnchorView(binding.fabSubmit).show()

    }

    /**
     *     abstract functions
     */


    abstract fun setToCombineCalender(year: Int, month: Int, day: Int)

    abstract fun setToCombineCalender(field: Int, value: Int)

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

    }
}