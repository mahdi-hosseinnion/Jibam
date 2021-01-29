package com.example.jibi.ui.main.transaction

import android.app.DatePickerDialog
import android.app.DatePickerDialog.OnDateSetListener
import android.app.TimePickerDialog
import android.content.Context
import android.content.res.Resources
import android.graphics.Color
import android.os.Bundle
import android.text.*
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.util.Log
import android.util.TypedValue
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.View.OnFocusChangeListener
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputConnection
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.lifecycle.ViewModelProvider
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
import com.google.android.material.bottomsheet.BottomSheetBehavior
import kotlinx.android.synthetic.main.fragment_add_transaction.*
import kotlinx.android.synthetic.main.fragment_add_transaction.view.*
import kotlinx.android.synthetic.main.fragment_transaction.*
import kotlinx.android.synthetic.main.keyboard_add_transaction.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject


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
    viewModelFactory
) {
    private val TAG = "AddTransactionFragment"

    private val textCalculator = TextCalculator()

    private val args: AddTransactionFragmentArgs by navArgs()
    private var category: Category? = null
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

    val combineCalender = GregorianCalendar(currentLocale)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setHasOptionsMenu(true)
        category = findCategory(cat_id = args.categoryId)
        setTransProperties(category = category)
        initUi(view)
//        edt_money.addTextChangedListener(onTextChangedListener)
        edt_money.addTextChangedListener(onTextChangedListener)

        category_fab.setOnClickListener {
            showBottomSheet()
        }


    }

    private fun showBottomSheet() {
        val modalBottomSheet =
            CreateNewTransBottomSheet(
                viewModel.viewState.value!!.categoryList!!,
                requestManager,
                onCategorySelectedCallback
            )
        modalBottomSheet.show(parentFragmentManager, "CreateNewTransBottomSheet")
    }

    private val onCategorySelectedCallback =
        object : CreateNewTransBottomSheet.OnCategorySelectedCallback {
            override fun onCategorySelected(item: Category) {
                //on category changed
                category = item
                setTransProperties(category = item)
            }

        }

    override fun onResume() {
        uiCommunicationListener.showToolbar()
        super.onResume()
    }

    private fun addOptionsToWallet() {
        val items = listOf("Cash", "Bank Melli", "Bank Keshavarzi", "MasterCard")
        val adapter = ArrayAdapter(requireContext(), R.layout.list_item, items)
        (txtField_wallet.edt_wallet as? AutoCompleteTextView)?.setAdapter(adapter)
        //set default value for it
        edt_wallet.setText(items[0], false)
        //add listener to hide keyboard when clicked
        edt_wallet.setOnClickListener {
            uiCommunicationListener.hideSoftKeyboard()
        }
    }

    private fun forceKeyBoardToOpenForMoneyEditText() {
        edt_money.requestFocus()
        val imm: InputMethodManager =
            activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(edt_money, InputMethodManager.SHOW_IMPLICIT)
    }

    private fun initUi(view: View) {
        //add date to date
        setDateToEditText()
        //Implementing an exposed dropdown menu for wallet editText
        addOptionsToWallet()
        //force keyboard to open up when addFragment launches
//        forceKeyBoardToOpenForMoneyEditText()
        // init keyboard

        // prevent system keyboard from appearing when EditText is tapped
        edt_money.setRawInputType(InputType.TYPE_CLASS_TEXT)
        edt_money.setTextIsSelectable(true)

        // pass the InputConnection from the EditText to the keyboard
        val ic: InputConnection = edt_money.onCreateInputConnection(EditorInfo())
        keyboard.inputConnection = ic
        //controll visibity

        // Make the custom keyboard appear
        edt_money.setOnFocusChangeListener(OnFocusChangeListener { v, hasFocus ->
            if (hasFocus)
                showCustomKeyboard(v)
            else
                hideCustomKeyboard()
        })
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

        showCustomKeyboard(edt_money)

    }

    private fun setDateToEditText(time: Int? = null) {
        disableContentInteraction(edt_date)
        val ss =
            SpannableString("${dateWithPattern(DATE_PATTERN)}    ${dateWithPattern(TIME_PATTERN)}")
        val dateOnClick = object : ClickableSpan() {
            override fun onClick(p0: View) {
                showDatePickerDialog()

            }

            override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)
                ds.isUnderlineText = false
                ds.color = edt_date.currentTextColor
            }
        }
        val timeOnClick = object : ClickableSpan() {
            override fun onClick(p0: View) {
                showTimePickerDialog()
            }

            override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)
                ds.isUnderlineText = false
                ds.color = edt_date.currentTextColor
            }
        }
        val dateEndIndex = ss.indexOf(DATE_PATTERN[DATE_PATTERN.lastIndex]).plus(1)
        ss.setSpan(
            dateOnClick,
            0,
            dateEndIndex,
            SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE
        )
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

    private fun dateWithPattern(pattern: String): String {
//        val df: Date = Date(System.currentTimeMillis())
        Log.d(TAG, "dateWithPattern 6859: --->>> ${combineCalender.timeInMillis}")
        val df = Date(combineCalender.timeInMillis)
        return SimpleDateFormat(pattern, currentLocale).format(df)
    }

    private fun showDatePickerDialog() {
        //hide money keyboard
        hideCustomKeyboard()

        val datePickerDialog =
            DatePickerDialog(
                this.requireContext(),
                { datePicker, year, monthOfYear, dayOfMonth ->
                    Log.d(
                        TAG,
                        "showDatePickerDialog 6859: year: $year month: $monthOfYear day: $dayOfMonth"
                    )
                    combineCalender.set(year, monthOfYear, dayOfMonth)
                    //update time
                    setDateToEditText()
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
                TimePickerDialog.OnTimeSetListener { datePicker, hourOfDay, minute ->
                    Log.d(TAG, "showDatePickerDialog6859: hour: $hourOfDay || minute: $minute ")
                    combineCalender.set(Calendar.HOUR_OF_DAY, hourOfDay)
                    combineCalender.set(Calendar.MINUTE, minute)
                    setDateToEditText()
                },
                combineCalender.get(Calendar.HOUR_OF_DAY),
                combineCalender.get(Calendar.MINUTE),
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

    fun showCustomKeyboard(view: View) {
        keyboard.visibility = View.VISIBLE
        val imm: InputMethodManager =
            activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    fun hideCustomKeyboard() {
        keyboard.visibility = View.GONE


    }

    private fun convertDpToPx(dp: Int): Int {
        val r: Resources = resources
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            dp.toFloat(),
            r.displayMetrics
        ).toInt()
    }

    private fun findCategory(cat_id: Int?): Category? {
        if (cat_id != null) {
            viewModel.viewState.value?.categoryList?.let { categoryList ->
                for (category in categoryList) {
                    if (category.id == cat_id) {
                        return category
                    }
                }
            }
        }
        return null
    }

    private fun setTransProperties(record: Record) {

    }

    private fun setTransProperties(
        money: Int? = null,
        category: Category? = null,
        memo: String? = null,
        specificDate: Int? = null,
        wallet_id: Int? = null,
        with: String? = null,
    ) {
        money?.let { edt_money.setText(it) }
        memo?.let { edt_memo.setText(memo) }
        category?.let {
            category_fab.setText(it.name)
            category_fab.extend()
            Log.d(TAG, "setTransProperties: amedemah:${it.name}")

            val resourceId: Int = requireActivity().resources.getIdentifier(
                "ic_cat_${category.name}",
                "drawable",
                requireActivity().packageName
            )

            category_fab.icon = resources.getDrawable(resourceId)
            /*         category_fab.backgroundTintList = ColorStateList.valueOf(
                         resources.getColor(
                             TransactionListAdapter.listOfColor[(category.id.minus(
                                 1
                             ))]
                         )
                     )*/
//            val categoryImageUrl = this.resources.getIdentifier(
//                "ic_cat_${category.name}",
//                "drawable",
//                requireActivity().packageName
//            )
//            requestManager
//                .load(categoryImageUrl)
//                .centerInside()
//                .transition(DrawableTransitionOptions.withCrossFade())
//                .error(R.drawable.ic_error)
//                .into(category_fab.icon)
        }
/*      TODO HANDLE THIS vars
        specificDate?.let {}
        wallet_id?.let {}
        with?.let { }
        */
    }

    private fun insertNewTrans() {
        if (handleInsertingErrors()) {
            var memo: String? = edt_memo.text.toString()
            //check if memo is blank then just save null
            if (memo.isNullOrBlank()) {
                memo = null
            }
            var money: Double = (finalNUmber.text.toString().replace(",".toRegex(), "").toDouble())

            if (category?.type == 1) {
                money *= -1
            }
            val transaction = Record(
                id = 0,
                money = money,
                memo = memo,
                cat_id = category!!.id,
                date = getTimeInSecond()
            )

            viewModel.launchNewJob(
                TransactionStateEvent.OneShotOperationsTransactionStateEvent.InsertTransaction(
                    transaction
                ), true
            )
            uiCommunicationListener.hideSoftKeyboard()
            findNavController().navigateUp()
        }
    }


    private fun handleInsertingErrors(): Boolean {
        if (finalNUmber.text.toString().replace(",".toRegex(), "").isBlank()) {
            Log.e(TAG, "MONEY IS NULL")
            edt_money.error = "Please insert some money"
            return false
        }
        if (finalNUmber.text.toString().replace(",".toRegex(), "").toDouble() < 0) {
            Log.e(TAG, "MONEY IS INVALID MOENY")
            edt_money.error = "money should be grater then 0"
            return false
        }
        if (category == null) {
            Log.e(TAG, "CATEGORY == NULL")
//            edt_category.error = "Please select category"
        }
        if (category?.id == null) {
            Log.e(TAG, "CATEGORY ID == NULL")
//            edt_category.error = "Please select category"
            return false
        }
        if (category?.id!! < 1) {
            Log.e(TAG, "CATEGORY ID == -1")
//            edt_category.error = "Please select category"
            return false
        }
        if (category?.type == null) {
            Log.e(TAG, "CATEGORY type == NULL")
//            edt_category.error = "Please select category"
            return false
        }
        if (category?.type!! < 1) {
            Log.e(TAG, "CATEGORY type == -1")
//            edt_category.error = "Please select category"
            return false
        }

        return true
    }

    private fun getTimeInSecond(): Int = ((combineCalender.timeInMillis) / 1000).toInt()

//    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
//        inflater.inflate(R.menu.add_menu, menu)
//    }
//
//    override fun onOptionsItemSelected(item: MenuItem): Boolean {
//        when (item.itemId) {
//            R.id.save -> {
//                insertNewTrans()
//                return true
//            }
//        }
//        return super.onOptionsItemSelected(item)
//    }

    override fun onPause() {
        super.onPause()
    }

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
                finalNUmber.text = calculatedResult
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

    private val bottomSheetCallback = object : BottomSheetBehavior.BottomSheetCallback() {

        override fun onStateChanged(bottomSheet: View, newState: Int) {
        }

        override fun onSlide(bottomSheet: View, slideOffset: Float) {
        }
    }

    companion object {
        const val TIME_PATTERN = "KK:mm aa"
        const val DATE_PATTERN = "MM/dd/yy (E)"
    }
}