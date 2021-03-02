package com.example.jibi.ui.main.transaction

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.text.*
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.util.Log
import android.view.*
import android.view.View.OnFocusChangeListener
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputConnection
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.core.content.res.ResourcesCompat
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.RequestManager
import com.example.jibi.R
import com.example.jibi.di.main.MainScope
import com.example.jibi.models.Category
import com.example.jibi.models.Record
import com.example.jibi.ui.main.transaction.bottomSheet.CreateNewTransBottomSheet
import com.example.jibi.ui.main.transaction.state.TransactionStateEvent
import com.example.jibi.util.TextCalculator
import kotlinx.android.synthetic.main.fragment_add_transaction.*
import kotlinx.android.synthetic.main.fragment_add_transaction.view.*
import kotlinx.android.synthetic.main.keyboard_add_transaction.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import kotlin.random.Random


@FlowPreview
@ExperimentalCoroutinesApi
@MainScope
class AddTransactionFragment
@Inject
constructor(
    viewModelFactory: ViewModelProvider.Factory,
    private val requestManager: RequestManager,
    private val currentLocale: Locale

) : BaseTransactionFragment(
    R.layout.fragment_add_transaction,
    viewModelFactory,
    R.id.fragment_add_toolbar_main
) {

    private val args: AddTransactionFragmentArgs by navArgs()

    private var transactionCategory: Category? = null

    private val textCalculator = TextCalculator()

    private val combineCalender = GregorianCalendar(currentLocale)

    //if this var doesn't be null it mean we are in viewing transaction State
    private var submitButtonState: SubmitButtonState? = null

    //we use this int to save transaction id for updating
    private var viewTransactionId: Int? = null


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initUi()
        //check if current state is ViewingTransaction or CreateTransaction
        val detailTransFields = viewModel.viewState.value?.detailTransFields

        if (args.categoryId < 0 && //default value is -1
            detailTransFields != null//there is transaction in viewState
        ) {
            initUiForViewTransaction(detailTransFields)
        } else {
            initUiForNewTransaction()
        }

        edt_money.addTextChangedListener(onTextChangedListener)

        edt_memo.addTextChangedListener {
            submitButtonState?.onMemoChange(it.toString())
        }

        category_fab.setOnClickListener {
            showBottomSheet()
        }


    }

    private fun showBottomSheet() {
        val modalBottomSheet =
            CreateNewTransBottomSheet(
                viewModel.viewState.value!!.categoryList!!,
                requestManager,
                onDismissCalled,
                transactionCategory != null
            )
        modalBottomSheet.isCancelable = transactionCategory != null
        modalBottomSheet.show(parentFragmentManager, "CreateNewTransBottomSheet")
    }

    private val onDismissCalled =
        object : CreateNewTransBottomSheet.OnDismissCallback {
            override fun onDismissCalled(selectedCategory: Category?) {
                if (selectedCategory == null)
                    return
                //on category changed
                transactionCategory = selectedCategory
                submitButtonState?.onCategoryChange(selectedCategory.id)
                setTransProperties(category = selectedCategory)
            }

        }


    private fun initUi() {
        //Implementing an exposed dropdown menu for wallet editText
        //TODO MAKE wallet work with transacion
        addOptionsToWallet()

        // prevent system keyboard from appearing when EditText is tapped
        edt_money.setRawInputType(InputType.TYPE_CLASS_TEXT)
        edt_money.setTextIsSelectable(true)

        // pass the InputConnection from the EditText to the keyboard
        val ic: InputConnection = edt_money.onCreateInputConnection(EditorInfo())
        keyboard.inputConnection = ic
        //controll visibity

        // Make the custom keyboard appear
        edt_money.onFocusChangeListener = OnFocusChangeListener { v, hasFocus ->
            if (hasFocus)
                showCustomKeyboard(v)
            else
                hideCustomKeyboard()
        }
        edt_money.requestFocus()
        edt_money.setOnTouchListener { view, motionEvent ->
            val inType: Int = edt_money.getInputType() // Backup the input type
            edt_money.inputType = InputType.TYPE_NULL // Disable standard keyboard
            edt_money.onTouchEvent(motionEvent)               // Call native handler
            edt_money.inputType = inType // Restore input type

            return@setOnTouchListener true // Consume touch event
        }
        edt_money.setOnClickListener {
            showCustomKeyboard(it)
        }
        uiCommunicationListener.hideSoftKeyboard()
    }

    private fun initUiForNewTransaction() {
        //add date to dat
        setDateToEditText()
        //submit button should always be displayed
        setHasOptionsMenu(true)

        setTransProperties(categoryId = args.categoryId)

        showCustomKeyboard(edt_money)
    }

    private fun initUiForViewTransaction(transaction: Record) {

        //change id
        viewTransactionId = transaction.id

        //submit button state stuff
        submitButtonState = SubmitButtonState(transaction)
        lifecycleScope.launch {
            submitButtonState?.isSubmitButtonEnable?.collect {
                //submit button state
                setHasOptionsMenu(it)

            }
        }

        setTransProperties(categoryId = transaction.cat_id, memo = transaction.memo)
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

    private fun addOptionsToWallet() {
        //TODO GET LIST OF WALLET FROM DATABASE
        val items = listOf("Cash", "Bank Melli", "Bank Keshavarzi", "MasterCard")
        val adapter = ArrayAdapter(requireContext(), R.layout.list_item, items)
        txtField_wallet.edt_wallet?.setAdapter(adapter)
        //set default value for it
        edt_wallet.setText(items[0], false)
        //add listener to hide keyboard when clicked
        edt_wallet.setOnClickListener {
            uiCommunicationListener.hideSoftKeyboard()
        }
    }


    private fun setDateToEditText() {
        disableContentInteraction(edt_date)

        val date = dateWithPattern(DATE_PATTERN)
        val time = dateWithPattern(TIME_PATTERN)

        val ss = SpannableString("$date    $time")


        //set onClick to date and show DatePicker
        val dateOnClick = onClickedOnSpan { showDatePickerDialog() }
        val dateEndIndex = ss.indexOf(DATE_PATTERN[DATE_PATTERN.lastIndex]).plus(1)
        ss.setSpan(
            dateOnClick,
            0,
            dateEndIndex,
            SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        //set onClick to time and show timePicker
        val timeOnClick = onClickedOnSpan { showTimePickerDialog() }
        var timeStartIndex =
            dateEndIndex.plus(ss.count { it == ' ' }).minus(DATE_PATTERN.count { it == ' ' }).minus(
                TIME_PATTERN.count { it == ' ' })
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

    private fun onClickedOnSpan(onClicked: (v: View) -> Unit): ClickableSpan =
        object : ClickableSpan() {
            override fun onClick(p0: View) {
                onClicked(p0)
            }

            override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)
                //remove under line and background color
                ds.isUnderlineText = false
                ds.color = edt_date.currentTextColor
            }
        }

    private fun dateWithPattern(pattern: String): String {
        val df = Date(combineCalender.timeInMillis)
        return SimpleDateFormat(pattern, currentLocale).format(df)
    }

    private fun showDatePickerDialog() {
        //hide money keyboard
        hideCustomKeyboard()

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
        keyboard.visibility = View.VISIBLE
        val imm: InputMethodManager =
            activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    private fun hideCustomKeyboard() {
        keyboard.visibility = View.GONE
    }

    private fun setTransProperties(
        money: Int? = null,
        category: Category? = null,
        categoryId: Int? = null,
        memo: String? = null
//        specificDate: Int? = null,
//        wallet_id: Int? = null,
//        with: String? = null
    ) {
        money?.let { edt_money.setText(it) }

        memo?.let { edt_memo.setText(memo) }

        //set category by category or category id
        if (category != null) {
            setCategoryNameAndIcon(category)
        } else {
            categoryId?.let { id ->
                getCategoryById(id)?.let {
                    setCategoryNameAndIcon(it)
                }
            }
        }
    }

    private fun setCategoryNameAndIcon(category: Category) {
        category_fab.text = category.name
        category_fab.extend()

        val resourceId: Int = requireActivity().resources.getIdentifier(
            "ic_cat_${category.img_res}",
            "drawable",
            requireActivity().packageName
        )

        category_fab.icon = ResourcesCompat.getDrawable(resources, resourceId, null)
    }

    private fun getCategoryById(categoryId: Int): Category? {
        transactionCategory = findCategoryByIdFromViewState(cat_id = categoryId)

        return if (transactionCategory == null) {
            showBottomSheet()
            null
        } else {
            transactionCategory
        }
    }

    private fun findCategoryByIdFromViewState(cat_id: Int): Category? {
        //getting list of all category from
        viewModel.viewState.value?.categoryList?.let { categoryList ->
            for (category in categoryList) {
                if (category.id == cat_id) {
                    return category
                }
            }
        }
        //TODO WRITE getting FORM database
        return null
    }

    private fun insertNewTrans() {
        if (checkForInsertingErrors()) {
            var memo: String? = edt_memo.text.toString()
            //check if memo is blank then just save null
            if (memo.isNullOrBlank()) {
                memo = null
            }
            val calculatedMoney = textCalculator.calculateResult(edt_money.text.toString())
            var money: Double = (calculatedMoney.replace(",".toRegex(), "").toDouble())

            if (transactionCategory?.type == 1) {//if its expenses we save it with - marker
                money *= -1
            }
            val categoryId = transactionCategory?.id
            if (categoryId != null) {
                val transaction = Record(
                    id = viewTransactionId ?: 0,//we need detail id for replacing(updating)
                    money = money,
                    memo = memo,
                    cat_id = categoryId,
                    date = getTimeInSecond()
                )

                viewModel.launchNewJob(
                    TransactionStateEvent.OneShotOperationsTransactionStateEvent.InsertTransaction(
                        transaction
                    ), true
                )
                uiCommunicationListener.hideSoftKeyboard()
                findNavController().navigateUp()
            } else {
                //no category selected
                unknownCategoryError()
            }

        }
    }


    private fun checkForInsertingErrors(): Boolean {
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
        if (transactionCategory == null) {
            unknownCategoryError()
            return false
        }
        if (transactionCategory?.id!! < 1) {
            unknownCategoryError()
            return false
        }

        return true
    }

    private fun unknownCategoryError() {
        Log.e(TAG, "unknownCategoryError: UNKNOWN CATEGORY $transactionCategory")
        Toast.makeText(
            this.requireContext(),
            errorMsgMapper(ErrorMessages.PLEASE_SELECT_CATEGORY),
            Toast.LENGTH_SHORT
        )
            .show()
        showBottomSheet()
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
                    chars = listOfNumbers
                ) >= 0
            ) {
                val calculatedResult = textCalculator.calculateResult(p0.toString())
                finalNUmber.text = convertDoubleToString(calculatedResult)
                submitButtonState?.onMoneyChange(calculatedResult.toDoubleOrNull())
            } else {
                finalNUmber.text = ""
                submitButtonState?.onMoneyChange(0.0)
            }

            edt_money.addTextChangedListener(this)
        }

    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.add_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.save -> {
                insertNewTrans()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    fun convertDoubleToString(text: String): String {//convert 13.0 to 13
        if (text.length < 2) {
            return text
        }
        return if (text.substring(text.lastIndex.minus(1)) == ".0") //convert 13.0 to 13
            text.substring(0, text.lastIndex.minus(1))
        else
            text
    }


    inner class SubmitButtonState(private val defaultTransaction: Record) {

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
        private const val DATE_PATTERN = "MM/dd/yy (E)"
        private val listOfNumbers = charArrayOf(
            '1',
            '2',
            '3',
            '4',
            '5',
            '6',
            '7',
            '8',
            '9'
        )

        //errors
        private enum class ErrorMessages {
            PLEASE_SELECT_CATEGORY,
            NEGATIVE_MONEY,
            EMPTY_MONEY
        }


    }

    private fun errorMsgMapper(error: ErrorMessages): String = when (error) {
        ErrorMessages.PLEASE_SELECT_CATEGORY -> resources.getString(R.string.pls_select_category)

        ErrorMessages.NEGATIVE_MONEY -> resources.getString(R.string.money_shouldnt_be_negative)

        ErrorMessages.EMPTY_MONEY -> resources.getString(R.string.pls_insert_some_money)
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