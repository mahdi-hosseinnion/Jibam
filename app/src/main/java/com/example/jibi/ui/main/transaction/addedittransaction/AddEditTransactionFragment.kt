package com.example.jibi.ui.main.transaction.addedittransaction

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.content.SharedPreferences
import android.content.res.Resources
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.text.*
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.util.Log
import android.view.*
import android.view.View.OnFocusChangeListener
import android.view.ViewGroup.MarginLayoutParams
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputConnection
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.core.text.TextUtilsCompat
import androidx.core.view.ViewCompat
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat
import com.alirezaafkar.sundatepicker.DatePicker
import com.bumptech.glide.RequestManager
import com.example.jibi.R
import com.example.jibi.di.main.MainScope
import com.example.jibi.models.Category
import com.example.jibi.models.TransactionEntity
import com.example.jibi.models.mappers.toTransactionEntity
import com.example.jibi.ui.main.transaction.addedittransaction.categorybottomsheet.CategoryBottomSheetListAdapter
import com.example.jibi.ui.main.transaction.addedittransaction.categorybottomsheet.CategoryBottomSheetViewPagerAdapter
import com.example.jibi.ui.main.transaction.addedittransaction.categorybottomsheet.CategoryBottomSheetViewPagerAdapter.Companion.VIEW_PAGER_SIZE
import com.example.jibi.ui.main.transaction.addedittransaction.state.AddEditTransactionStateEvent
import com.example.jibi.ui.main.transaction.addedittransaction.state.PresenterState
import com.example.jibi.ui.main.transaction.common.BaseFragment
import com.example.jibi.util.*
import com.example.jibi.util.SolarCalendar.ShamsiPatterns.DETAIL_FRAGMENT
import com.google.android.material.bottomsheet.BottomSheetBehavior
import kotlinx.android.synthetic.main.fragment_add_transaction.*
import kotlinx.android.synthetic.main.fragment_add_transaction.view.*
import kotlinx.android.synthetic.main.fragment_transaction.*
import kotlinx.android.synthetic.main.keyboard_add_transaction.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import uk.co.samuelwall.materialtaptargetprompt.MaterialTapTargetPrompt
import uk.co.samuelwall.materialtaptargetprompt.extras.backgrounds.RectanglePromptBackground
import uk.co.samuelwall.materialtaptargetprompt.extras.focals.RectanglePromptFocal
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject


@FlowPreview
@ExperimentalCoroutinesApi
@MainScope
class AddEditTransactionFragment
@Inject
constructor(
    viewModelFactory: ViewModelProvider.Factory,
    private val requestManager: RequestManager,
    private val currentLocale: Locale,
    private val sharedPreferences: SharedPreferences,
    private val sharedPrefsEditor: SharedPreferences.Editor,
    private val _resources: Resources
) : BaseFragment(
    R.layout.fragment_add_transaction,
    R.id.fragment_add_toolbar_main, _resources
), CalculatorKeyboard.CalculatorInteraction, CategoryBottomSheetListAdapter.Interaction {

    private val viewModel by viewModels<AddEditTransactionViewModel> { viewModelFactory }

    private val args: AddEditTransactionFragmentArgs by navArgs()


    private val textCalculator = TextCalculator()

    private val combineCalender = GregorianCalendar(currentLocale)

    //if this var doesn't be null it mean we are in viewing transaction State
    private var submitButtonState: SubmitButtonState? = null

    //we use this int to save transaction id for updating
    private var viewTransactionId: Int? = null

    private lateinit var btmsheetViewPagerAdapter: CategoryBottomSheetViewPagerAdapter

    private lateinit var bottomSheetBehavior: BottomSheetBehavior<LinearLayout>

    private val bottomSheetCallback = object : BottomSheetBehavior.BottomSheetCallback() {

        override fun onStateChanged(bottomSheet: View, newState: Int) {
            if (newState == BottomSheetBehavior.STATE_HIDDEN) {

                if (edt_money.text.toString().isBlank()) {
                    viewModel.setPresenterState(PresenterState.EnteringAmountOfMoney)
                } else {
                    viewModel.setPresenterState(PresenterState.NormalState)
                }
            }
        }

        override fun onSlide(bottomSheet: View, slideOffset: Float) {
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initUi()
        //check if current state is ViewingTransaction or CreateTransaction
        val transactionId = args.transactionId

        if (transactionId > -1  //default value is -1
        ) {
            //getting transaction via id
            viewModel.launchNewJob(
                AddEditTransactionStateEvent.GetTransactionById(
                    transactionId = transactionId
                )
            )
        } else {
            initUiForNewTransaction()
        }

        subscribeObservers()

        edt_money.addTextChangedListener(onTextChangedListener)

        edt_memo.addTextChangedListener {
            submitButtonState?.onMemoChange(it.toString())
        }

        category_fab.setOnClickListener {
            viewModel.setPresenterState(PresenterState.SelectingCategory(viewModel.getSelectedCategoryId()))
        }
        fab_submit.setOnClickListener {
            insertNewTrans()
        }


    }

    override fun handleLoading() {
        viewModel.countOfActiveJobs.observe(
            viewLifecycleOwner
        ) {
            Log.d(TAG, "handleLoading: activeJob: ${viewModel.getAllActiveJobs()}")
            showProgressBar(viewModel.areAnyJobsActive())
        }
    }

    override fun handleStateMessages() {
        viewModel.stateMessage.observe(viewLifecycleOwner) {
            it?.let {
                handleNewStateMessage(it) { viewModel.clearStateMessage() }
            }
        }
    }

    private fun subscribeObservers() {
        viewModel.viewState.observe(viewLifecycleOwner) { vs ->
            vs?.let { viewState ->
                viewState.transaction?.let {
                    //TODO BUG this should not call init ui for viwe
                    initUiForViewTransaction(it.toTransactionEntity())
                }
                viewState.presenterState?.let { onPresenterChanged(it) }

                viewState.categoriesList?.let {
                    btmsheetViewPagerAdapter.submitData(it)
                    viewModel.checkForTransactionCategoryToNotBeNull()
                }

                viewState.transactionCategory?.let { setCategoryNameAndIcon(it) }

                viewState.insertedTransactionRawId?.let {
                    uiCommunicationListener.hideSoftKeyboard()
                    findNavController().navigateUp()
                }
                viewState.successfullyDeletedTransactionIndicator?.let {
                    uiCommunicationListener.hideSoftKeyboard()
                    findNavController().navigateUp()
                }
            }
        }
    }

    private fun onPresenterChanged(state: PresenterState) =
        when (state) {
            is PresenterState.SelectingCategory -> {
                setViewsToSelectingCategory(state.default_category_id)
            }
            is PresenterState.EnteringAmountOfMoney -> {
                setViewsToEnteringAmountOfMoney()
            }
            is PresenterState.NormalState -> {
                setViewsToSelectingNormalState()
            }
        }

    private fun setViewsToSelectingCategory(defaultCategoryId: Int) {
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
        setupCategoryBottomSheet(defaultCategoryId)
        fab_submit.hide()
        category_fab.hide()
        hideCustomKeyboard()
    }

    private fun setViewsToEnteringAmountOfMoney() {
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
        fab_submit.show()
        category_fab.show()
        edt_money.requestFocus()
        showCustomKeyboard(edt_money)
    }

    private fun setViewsToSelectingNormalState() {
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
        fab_submit.show()
        category_fab.show()
        hideCustomKeyboard()

    }


/*    private fun showBottomSheet() {
        val categoryList = viewModel.getCategoriesList()
        val modalBottomSheet =
            CreateNewTransBottomSheet(
                categoryList,
                requestManager,
                onDismissCalled,
                viewModel.getSelectedCategoryId(),
                sharedPreferences,
                sharedPrefsEditor,
                _resources
            )
        modalBottomSheet.show(parentFragmentManager, "CreateNewTransBottomSheet")
    }*/
/*
    private val onDismissCalled =
        object : CreateNewTransBottomSheet.OnDismissCallback {
            override fun onDismissCalled(selectedCategory: Category?) {
                checkForGuidePromote()
                if (selectedCategory == null)
                    return
                //on category changed
                transactionCategory = selectedCategory
                submitButtonState?.onCategoryChange(selectedCategory.id)
                setTransProperties(category = selectedCategory)
            }

        }*/


    private fun initUi() {
        setupBottomSheet()
        // prevent system keyboard from appearing when EditText is tapped
        edt_money.setRawInputType(InputType.TYPE_CLASS_TEXT)
        edt_money.setTextIsSelectable(true)

        // pass the InputConnection from the EditText to the keyboard
        val ic: InputConnection = edt_money.onCreateInputConnection(EditorInfo())
        keyboard.inputConnection = ic
        keyboard.calculatorInteraction = this
        keyboard._resources = _resources
        keyboard.setTextToAllViews()
        //controll visibity

        // Make the custom keyboard appear
        edt_money.onFocusChangeListener = OnFocusChangeListener { v, hasFocus ->
            if (hasFocus)
                viewModel.setPresenterState(PresenterState.EnteringAmountOfMoney)
            else
                viewModel.setPresenterState(PresenterState.NormalState)
        }

        edt_money.setOnTouchListener { view, motionEvent ->
            val inType: Int = edt_money.getInputType() // Backup the input type
            edt_money.inputType = InputType.TYPE_NULL // Disable standard keyboard
            edt_money.onTouchEvent(motionEvent)               // Call native handler
            edt_money.inputType = inType // Restore input type

            return@setOnTouchListener true // Consume touch event
        }
        edt_money.setOnClickListener {
            viewModel.setPresenterState(PresenterState.EnteringAmountOfMoney)
        }
        uiCommunicationListener.hideSoftKeyboard()
//        setupCategoryBottomSheet(viewModel.getSelectedCategoryId())
    }

    private fun setupBottomSheet() {

        bottomSheetBehavior = BottomSheetBehavior.from(select_category_bottom_sheet)
        bottomSheetBehavior.addBottomSheetCallback(bottomSheetCallback)
        bottomSheetBehavior.isHideable = true
        bottomSheetBehavior.skipCollapsed = true
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
    }

    private fun setupCategoryBottomSheet(selectedCategoryId: Int) {
        val isLeftToRight =
            (TextUtilsCompat.getLayoutDirectionFromLocale(Locale.getDefault()) == ViewCompat.LAYOUT_DIRECTION_LTR)
        //viewpager
        btmsheetViewPagerAdapter = CategoryBottomSheetViewPagerAdapter(
            context = this.requireContext(),
            categoryList = viewModel.getCategoriesList(),
            interaction = this,
            requestManager = requestManager,
            isLeftToRight = isLeftToRight,
            packageName = this.requireActivity().packageName,
            _resources = _resources,
            selectedCategoryId = viewModel.getSelectedCategoryId()
        )

        bottom_sheet_viewpager.adapter = btmsheetViewPagerAdapter
        category_tab_layout.setupWithViewPager(bottom_sheet_viewpager)

        if (!isLeftToRight) {
            bottom_sheet_viewpager.currentItem = VIEW_PAGER_SIZE
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

    private fun initUiForNewTransaction() {
        findNavController()
            .currentDestination?.label = _getString(R.string.add_transaction)
        /*lifecycleScope.launch {
            delay(300)
            showBottomSheet()
        }*/

        //add date to dat
        setDateToEditText()
        //submit button should always be displayed
        viewModel.setPresenterState(PresenterState.SelectingCategory(viewModel.getSelectedCategoryId()))
//        edt_money.requestFocus()
//        showCustomKeyboard(edt_money)
    }

    private fun initUiForViewTransaction(transaction: TransactionEntity) {
        findNavController()
            .currentDestination?.label = _getString(R.string.details)
        //change id
        viewTransactionId = transaction.id
        //add delete icon
        setHasOptionsMenu(true)

        //submit button state stuff
        submitButtonState = SubmitButtonState(transaction)
        lifecycleScope.launch {
            submitButtonState?.isSubmitButtonEnable?.collect {
                //submit button state
                if (it) {
                    fab_submit.show()
                } else {
                    fab_submit.hide()
                }
            }
        }

        setTransProperties(memo = transaction.memo)
        //change date to transaction date
        combineCalender.timeInMillis =
            ((transaction.date.toLong()) * 1000)//ALWAYS CALL BEFORE SET DATE
        //add date to dat
        setDateToEditText()
        //set money
        //convert -13 to 12
        val transactionMoney = if (transaction.money > 0)
            transaction.money.toString()
        else transaction.money.times(-1).toString()

        val money = convertDoubleToString(transactionMoney)

        keyboard.preloadKeyboard(money)

        hideCustomKeyboard()

    }

    private fun setDateToEditText() {
        disableContentInteraction(edt_date)

        val date = dateWithPattern()
        val time = timeWithPattern()
        val spaceBetweenDateAndTime = "   "
        val ss = SpannableString("$date$spaceBetweenDateAndTime$time")


        //set onClick to date and show DatePicker
        val dateOnClick = onClickedOnSpan(textColor = edt_date.currentTextColor) {
            showDatePickerDialog()
        }
        val dateEndIndex = date.length
        ss.setSpan(
            dateOnClick,
            0,
            dateEndIndex,
            SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        //set onClick to time and show timePicker
        val timeOnClick = onClickedOnSpan(textColor = edt_date.currentTextColor) {
            showTimePickerDialog()
        }
        var timeStartIndex = dateEndIndex.plus(spaceBetweenDateAndTime.length)

        if (timeStartIndex < dateEndIndex)
            timeStartIndex = dateEndIndex
        ss.setSpan(
            timeOnClick,
            timeStartIndex,
            ss.lastIndex.plus(1),
            SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        //some customization
        edt_date.setText(ss)
        edt_date.movementMethod = LinkMovementMethod.getInstance()
        edt_date.highlightColor = Color.TRANSPARENT
    }

    private fun onClickedOnSpan(textColor: Int, onClicked: (v: View) -> Unit): ClickableSpan =
        object : ClickableSpan() {
            override fun onClick(p0: View) {
                onClicked(p0)
            }

            override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)
                //remove under line and background color
                ds.isUnderlineText = false
                //this line cause fail during rotation so we get text color every time
                ds.color = textColor
            }
        }

    private fun dateWithPattern(): String {
        return if (currentLocale.isFarsi()) {
            SolarCalendar.calcSolarCalendar(
                combineCalender.timeInMillis, DETAIL_FRAGMENT, currentLocale
            )
        } else {
            val df = Date(combineCalender.timeInMillis)
            SimpleDateFormat(DATE_PATTERN, currentLocale).format(df)
        }
    }

    private fun timeWithPattern(): String {
        val df = Date(combineCalender.timeInMillis)
        return SimpleDateFormat(TIME_PATTERN, currentLocale).format(df)
    }

    private fun showDatePickerDialog() {
        //hide money keyboard
        hideCustomKeyboard()
        if (currentLocale.isFarsi()) {
            showShamsiDatePicker()
        } else {
            showGregorianDatePicker()
        }
    }

    private fun showShamsiDatePicker() {
        DatePicker.Builder()
            .date(combineCalender)
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
                if (calendar != null) {
                    combineCalender.set(
                        calendar[Calendar.YEAR],
                        calendar[Calendar.MONTH],
                        calendar[Calendar.DAY_OF_MONTH]
                    )
                    //update time
                    setDateToEditText()
                    submitButtonState?.onDateChange(getTimeInSecond())
                } else {
                    Log.e(TAG, "showShamsiDatePicker: NULL GREGORIAN CALENDER")
                    //TODO add backup plan and shamsi to gregorian converter
                    Toast.makeText(
                        this.requireContext(),
                        _getString(R.string.unable_to_get_date),
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
            .show(childFragmentManager, "ShamsiDatePicker")
    }

    private fun showGregorianDatePicker() {
        val datePickerDialog =
            DatePickerDialog(
                this.requireContext(),
                { _, year, monthOfYear, dayOfMonth ->
                    Log.d(
                        TAG,
                        "showDatePickerDialog 6859: year: $year month: $monthOfYear day: $dayOfMonth"
                    )
                    combineCalender.set(year, monthOfYear, dayOfMonth)
                    //update time
                    setDateToEditText()
                    submitButtonState?.onDateChange(getTimeInSecond())
                },
                combineCalender.get(Calendar.YEAR),
                combineCalender.get(Calendar.MONTH),
                combineCalender.get(Calendar.DAY_OF_MONTH)
            )
        datePickerDialog.datePicker.minDate = SolarCalendar.minGregorianDate
        datePickerDialog.datePicker.maxDate = SolarCalendar.maxGregorianDate
        datePickerDialog.show()
    }

    private fun showTimePickerDialog() {
        //hide money keyboard
        hideCustomKeyboard()
        //show picker
        val timePickerDialog =
            TimePickerDialog(
                this.requireContext(),
                { _, hourOfDay, minute ->
                    Log.d(TAG, "showTimePickerDialog: hour: $hourOfDay || minute: $minute ")
                    combineCalender.set(GregorianCalendar.HOUR_OF_DAY, hourOfDay)
                    combineCalender.set(GregorianCalendar.MINUTE, minute)
                    setDateToEditText()
                    submitButtonState?.onDateChange(getTimeInSecond())
                },
                combineCalender.get(GregorianCalendar.HOUR_OF_DAY),
                combineCalender.get(GregorianCalendar.MINUTE),
                false
            )
        //TODO MAKE farst time picker SUPPORT IN FARSI
//        timePickerDialog.setButton(TimePickerDialog.BUTTON_POSITIVE,_getString(R.string.capital_ok),timePickerDialog)
//        timePickerDialog.setButton(TimePickerDialog.BUTTON_NEGATIVE,_getString(R.string.capital_cancel),timePickerDialog)
        timePickerDialog.show()
    }

    private fun disableContentInteraction(edt: EditText) {
        edt.keyListener = null
        edt.isFocusable = false
        edt.isFocusableInTouchMode = false
        edt.isCursorVisible = false
        edt.clearFocus()
    }

    private fun showCustomKeyboard(view: View) {
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

    private fun hideCustomKeyboard() {
        keyboard.visibility = View.GONE
        val _16Dp = convertDpToPx(16)
        //change bottom margin of fab
        Log.d(TAG, "showCustomKeyboard: HIDDEE Height is ${keyboard.height}")
        changeFabBottomMargin()
    }


    fun View.setMargins(l: Int, t: Int, r: Int, b: Int) {
        if (this.layoutParams is MarginLayoutParams) {
            val p = this.layoutParams as MarginLayoutParams
            p.setMargins(l, t, r, b)
            this.requestLayout()
        }
    }

    private fun changeFabBottomMargin(marginBottom: Int? = null) {
        val _16Dp = convertDpToPx(16)
        if (fab_submit.isShown) {
            //TODO ADD SLIDE ANIMATION HERE
            fab_submit.hide()
            fab_submit.setMargins(_16Dp, _16Dp, _16Dp, marginBottom ?: _16Dp)
            fab_submit.show()
        } else {
            fab_submit.setMargins(_16Dp, _16Dp, _16Dp, marginBottom ?: _16Dp)
        }
    }

    private fun setTransProperties(
        money: Int? = null,
//        category: Category? = null,
//        categoryId: Int? = null,
        memo: String? = null
//        specificDate: Int? = null,
//        wallet_id: Int? = null,
//        with: String? = null
    ) {
        money?.let { edt_money.setText(it) }

        memo?.let { edt_memo.setText(memo) }

        //set category by category or category id
//        if (category != null) {
//            setCategoryNameAndIcon(category)
//            transactionCategory = category
//        } else {
//            categoryId?.let { id ->
//                getCategoryById(id)?.let {
//                    setCategoryNameAndIcon(it)
//                }
//            }
//        }
    }

    private fun setCategoryNameAndIcon(category: Category) {
        Log.d(
            TAG,
            "setCategoryNameAndIcon: CALLED with name: ${category.name} and icon: ${category.img_res} "
        )
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
//        ResourcesCompat.getDrawable(resources, resourceId, null)
        category_fab.icon =
            VectorDrawableCompat.create(resources, resourceId, null)
    }

/*
    private fun getCategoryById(categoryId: Int): Category? {
        transactionCategory = findCategoryByIdFromViewState(cat_id = categoryId)

        return if (transactionCategory == null) {
//            showBottomSheet()
            null
        } else {
            transactionCategory
        }
    }

    private fun findCategoryByIdFromViewState(cat_id: Int): Category? {
        //getting list of all category from
        viewModel.getCurrentViewStateOrNew().categoriesList?.let { categoryList ->
            for (category in categoryList) {
                if (category.id == cat_id) {
                    return category
                }
            }
        }
        //TODO WRITE getting FORM database
        return null
    }
*/

    private fun insertNewTrans() {
        if (checkForInsertingErrors()) {
            var memo: String? = edt_memo.text.toString()
            //check if memo is blank then just save null
            if (memo.isNullOrBlank()) {
                memo = null
            }
            val calculatedMoney = textCalculator.calculateResult(edt_money.text.toString())
            var money: Double = (calculatedMoney.replace(",".toRegex(), "").toDouble())

            val transactionCategory = viewModel.getTransactionCategory()
            if (transactionCategory?.type == 1) {//if its expenses we save it with - marker
                money *= -1
            }
            val categoryId = transactionCategory?.id
            if (categoryId != null) {
                val transaction = TransactionEntity(
                    id = viewTransactionId ?: 0,//we need detail id for replacing(updating)
                    money = money,
                    memo = memo,
                    cat_id = categoryId,
                    date = getTimeInSecond()
                )

                viewModel.insertTransaction(transaction)
            } else {
                //no category selected
                unknownCategoryError()
            }

        }
    }


    private fun checkForInsertingErrors(): Boolean {
        if (edt_money.text.toString().isBlank()) {
            Log.e(TAG, "MONEY IS NULL")
            edt_money.error = errorMsgMapper(ErrorMessages.EMPTY_MONEY)
            return false
        }
        val calculatedMoney = textCalculator.calculateResult(edt_money.text.toString())
            .replace(",".toRegex(), "")
        if (calculatedMoney.isBlank()) {
            Log.e(TAG, "MONEY IS NULL")
            edt_money.error = errorMsgMapper(ErrorMessages.EMPTY_MONEY)
            return false
        }
        if (calculatedMoney.toDouble() < 0) {
            Log.e(TAG, "MONEY IS INVALID cannot save negative money ")
            edt_money.error = errorMsgMapper(ErrorMessages.NEGATIVE_MONEY)
            return false
        }
        if (viewModel.getTransactionCategory() == null) {
            unknownCategoryError()
            return false
        }
        return true
    }

    private fun unknownCategoryError() {
        Log.e(TAG, "unknownCategoryError: UNKNOWN CATEGORY ${viewModel.getTransactionCategory()}")
        Toast.makeText(
            this.requireContext(),
            errorMsgMapper(ErrorMessages.PLEASE_SELECT_CATEGORY),
            Toast.LENGTH_SHORT
        )
            .show()
//        showBottomSheet()
    }

    private fun getTimeInSecond(): Int = ((combineCalender.timeInMillis) / 1000).toInt()


    private val onTextChangedListener = object : TextWatcher {
        override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

        override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

        override fun afterTextChanged(p0: Editable?) {
            edt_money.removeTextChangedListener(this)

            /* try {
                var originalString: String = p0.toString()
                val longval: Long
                if (originalString.contains(",")) {
                    originalString = originalString.replace(",".toRegex(), "")
                }
                longval = originalString.toLong()
                val formatter: DecimalFormat = NumberFormat.getInstance(Locale.US) as DecimalFormat
                formatter.applyPattern("#,###,###,###.###")
                val formattedString: String = formatter.format(longval)

                //setting text after format to EditText
                edt_money.setText(formattedString)
                //TODO FIX SELECTION ISSUE
                //IF YOU SELECT LENGH that will not work b/c if cursor be in middle of text it move to the end
                edt_money.setSelection(edt_money.text!!.length.minus(1))

            } catch (nfe: NumberFormatException) {
                nfe.printStackTrace()
            } catch (e: Exception) {
                Log.e(TAG, "afterTextChanged: ", e)
            }*/

            //calculate result of main edittext
            if (p0.toString().indexOfAny(
                    chars = listOfNumbers()
                ) >= 0
            ) {
                val calculatedResult = textCalculator.calculateResult(p0.toString())

                val finalNumberText =
                    localizeDoubleNumber(calculatedResult.toDouble(), currentLocale)
                finalNUmber.text =
                    if (finalNumberText == edt_money.text.toString().removeOperationSigns()) ""
                    else finalNumberText

                var newMoney = calculatedResult.toDoubleOrNull()

                if (viewModel.getTransactionCategory()?.type == 1 && newMoney != null) {
                    //if its expenses we save it with - marker
                    newMoney *= -1
                }
                submitButtonState?.onMoneyChange(newMoney)
            } else {
                finalNUmber.text = ""
                submitButtonState?.onMoneyChange(0.0)
            }

            edt_money.addTextChangedListener(this)
        }

    }

    fun String.removeOperationSigns(): String {
        var result = this
        for (values in CalculatorKeyboard.listOfSigns) {
            result = result.replace(values, "")
        }
        return result
    }

    private fun checkForGuidePromote() {
        if (sharedPreferences.getBoolean(PreferenceKeys.PROMOTE_ADD_TRANSACTION, true)) {
            trySafe { showCategoryFabPromote() }
        }
    }

    private fun showCategoryFabPromote() {
        val categoryFabPrompt = MaterialTapTargetPrompt.Builder(this)
            .setTarget(R.id.category_fab)
            .setPrimaryText(_getString(R.string.category_fab_tap_target_primary))
            .setSecondaryText(_getString(R.string.category_fab_tap_target_secondary))
            .setPromptBackground(RectanglePromptBackground())
            .setPromptFocal(RectanglePromptFocal())
            .setPromptStateChangeListener { _, state ->
                if (state == MaterialTapTargetPrompt.STATE_FOCAL_PRESSED || state == MaterialTapTargetPrompt.STATE_DISMISSING) {
                    trySafe { showMoneyPromote() }

                }
            }
            .create()
        categoryFabPrompt!!.show()
    }

    private fun showMoneyPromote() {
        val edtMoneyPrompt = MaterialTapTargetPrompt.Builder(this)
            .setTarget(R.id.edt_money)
            .setPrimaryText(_getString(R.string.edt_money_tap_target_primary))
            .setSecondaryText(_getString(R.string.edt_money_tap_target_secondary))
            .setPromptBackground(RectanglePromptBackground())
            .setPromptFocal(RectanglePromptFocal())

            .setPromptStateChangeListener { _, state ->
                if (state == MaterialTapTargetPrompt.STATE_FOCAL_PRESSED || state == MaterialTapTargetPrompt.STATE_DISMISSING) {
                    trySafe { showDatePickerPromote() }

                }
            }
            .create()
        edtMoneyPrompt!!.show()
    }


    private fun showDatePickerPromote() {
        val datePrompt = MaterialTapTargetPrompt.Builder(this)
            .setTarget(R.id.txtField_date)
            .setPrimaryText(_getString(R.string.date_tap_target_primary))
            .setSecondaryText(_getString(R.string.date_tap_target_secondary))
            .setPromptBackground(RectanglePromptBackground())
            .setPromptFocal(RectanglePromptFocal())

            .setPromptStateChangeListener { _, state ->
                if (state == MaterialTapTargetPrompt.STATE_FOCAL_PRESSED || state == MaterialTapTargetPrompt.STATE_DISMISSING) {
                    trySafe { showNotePromote() }

                }
            }
            .create()
        datePrompt!!.show()
    }

    private fun showNotePromote() {
        val datePrompt = MaterialTapTargetPrompt.Builder(this)
            .setTarget(R.id.txtField_memo)
            .setPrimaryText(_getString(R.string.note_tap_target_primary))
            .setPromptBackground(RectanglePromptBackground())
            .setPromptFocal(RectanglePromptFocal())

            .setPromptStateChangeListener { _, state ->
                if (state == MaterialTapTargetPrompt.STATE_FOCAL_PRESSED || state == MaterialTapTargetPrompt.STATE_DISMISSING) {
                    sharedPrefsEditor.putBoolean(
                        PreferenceKeys.PROMOTE_ADD_TRANSACTION,
                        false
                    ).apply()
                }
            }
            .create()
        datePrompt!!.show()
    }

    inner class SubmitButtonState(private val defaultTransaction: TransactionEntity) {

        private val _doesMoneyChange = MutableStateFlow(false)
        private val _doesMemoChange = MutableStateFlow(false)
        private val _doesCategoryChange = MutableStateFlow(false)
        private val _doesDateChange = MutableStateFlow(false)

        val isSubmitButtonEnable: Flow<Boolean> = combine(
            _doesMoneyChange,
            _doesMemoChange,
            _doesCategoryChange,
            _doesDateChange
        )
        { money, memo, category, date ->
            return@combine money || memo || category || date
        }

        fun onMoneyChange(newMoney: Double?) {
            _doesMoneyChange.value = defaultTransaction.money != newMoney
        }

        fun onMemoChange(newMemo: String?) {
            _doesMoneyChange.value = defaultTransaction.memo != newMemo
        }

        fun onCategoryChange(categoryId: Int) {
            _doesMoneyChange.value = defaultTransaction.cat_id != categoryId

        }

        fun onDateChange(newDate: Int) {
            _doesMoneyChange.value = defaultTransaction.date != newDate

        }
    }

    companion object {
        private const val TAG = "AddTransactionFragment"

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

    private fun listOfNumbers(): CharArray =
        charArrayOf(
            _getString(R.string._1)[0],
            _getString(R.string._2)[0],
            _getString(R.string._3)[0],
            _getString(R.string._4)[0],
            _getString(R.string._5)[0],
            _getString(R.string._6)[0],
            _getString(R.string._7)[0],
            _getString(R.string._8)[0],
            _getString(R.string._9)[0]
        )

    private fun errorMsgMapper(error: ErrorMessages): String = when (error) {
        ErrorMessages.PLEASE_SELECT_CATEGORY -> _getString(R.string.pls_select_category)

        ErrorMessages.NEGATIVE_MONEY -> _getString(R.string.money_shouldnt_be_negative)

        ErrorMessages.EMPTY_MONEY -> _getString(R.string.pls_insert_some_money)
    }

    override fun onEqualClicked() {
        keyboard.preloadKeyboard(finalNUmber.text.toString())
    }

    override fun setTextToAllViews() {
        txtField_memo.hint = _getString(R.string.write_note)
        txtField_date.hint = _getString(R.string.date)
        edt_money.hint = _getString(R.string._0)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.delete_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.delete_transaction -> {
                checkForDelete(viewTransactionId)
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun checkForDelete(id: Int?) {
        if (id == null) {
            //show toast error
            viewModel.addToMessageStack(
                _getString(R.string.unable_to_recognize_this_transaction),
                Throwable("$TAG : deleteTransaction: viewTransactionId is null!  viewTransactionId = $viewTransactionId"),
                UIComponentType.Toast,
                MessageType.Error
            )
        } else {
            val callback = object : AreYouSureCallback {
                override fun proceed() {
                    deleteTransaction(id)
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

    fun deleteTransaction(id: Int) {
        viewModel.launchNewJob(
            AddEditTransactionStateEvent.DeleteTransaction(
                id
            )
        )

    }

    override fun onItemSelected(position: Int, item: Category) {
        //on category changed
        viewModel.setTransactionCategory(item)
        submitButtonState?.onCategoryChange(item.id)

        viewModel.setPresenterState(PresenterState.EnteringAmountOfMoney)
    }
}
//TODO TRASH
/*
private fun forceKeyBoardToOpenForMoneyEditText(edt: EditText) {
    edt.requestFocus()
    val imm: InputMethodManager =
        activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.showSoftInput(edt, InputMethodManager.SHOW_IMPLICIT)
}

    private fun convertDpToPx(dp: Int): Int {
        val r: Resources = resources
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            dp.toFloat(),
            r.displayMetrics
        ).toInt()
    }
 */