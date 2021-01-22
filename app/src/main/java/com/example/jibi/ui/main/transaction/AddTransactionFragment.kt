package com.example.jibi.ui.main.transaction

import android.content.Context
import android.content.res.ColorStateList
import android.content.res.Resources
import android.os.Bundle
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.util.Log
import android.util.TypedValue
import android.view.View
import android.view.View.OnFocusChangeListener
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputConnection
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.LinearLayout
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.marginBottom
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.RequestManager
import com.example.jibi.R
import com.example.jibi.di.main.MainScope
import com.example.jibi.models.Category
import com.example.jibi.models.Record
import com.example.jibi.ui.main.transaction.state.TransactionStateEvent
import com.example.jibi.util.TextCalculator
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.android.synthetic.main.fragment_add_transaction.*
import kotlinx.android.synthetic.main.fragment_add_transaction.view.*
import kotlinx.android.synthetic.main.fragment_transaction.*
import kotlinx.android.synthetic.main.keyboard_add_transaction.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import java.text.DecimalFormat
import java.text.NumberFormat
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

    ) : BaseTransactionFragment(
    R.layout.fragment_add_transaction,
    viewModelFactory
) {
    private val TAG = "AddTransactionFragment"

    private val textCalculator = TextCalculator()

    private val args: AddTransactionFragmentArgs by navArgs()
    private var category: Category? = null


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


//        setHasOptionsMenu(true)
        category = findCategory(cat_id = args.categoryId)
        setTransProperties(category = category)
        initUi(view)
//        edt_money.addTextChangedListener(onTextChangedListener)
        edt_money.addTextChangedListener(onTextChangedListener)
        fab_insertTransaction.setOnClickListener {
            insertNewTrans()
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
        //make edt category nonEditable
//        edt_category.keyListener = null
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

    fun showCustomKeyboard(view: View) {
        keyboard.visibility = View.VISIBLE
        val imm: InputMethodManager =
            activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
        finalNUmber.text = "${keyboard.height} & ${keyboard.measuredHeight}"
        //change fab height
        val viewParams = fab_insertTransaction.layoutParams as CoordinatorLayout.LayoutParams
        val e = convertDpToPx(16)
        viewParams.setMargins(e, e, e, e.plus(keyboard.measuredHeight))
    }

    fun hideCustomKeyboard() {
        keyboard.visibility = View.GONE
        finalNUmber.text = "${keyboard.height} & ${keyboard.measuredHeight}"

        //change fab height
        val viewParams = fab_insertTransaction.layoutParams as CoordinatorLayout.LayoutParams
        val e = convertDpToPx(16)
        viewParams.setMargins(e, e, e, e)

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
            category_fab.backgroundTintList = ColorStateList.valueOf(
                resources.getColor(
                    TransactionListAdapter.listOfColor[(category.id.minus(
                        1
                    ))]
                )
            )
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
            var money: Int = (edt_money.text.toString().replace(",".toRegex(), "").toInt())

            if (category?.type == 1) {
                money *= -1
            }
            val transaction = Record(
                id = 0,
                money = money,
                memo = memo,
                cat_id = category!!.id,
                date = getCurrentTimeInSecond()
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
        if (edt_money.text.toString().replace(",".toRegex(), "").isBlank()) {
            Log.e(TAG, "MONEY IS NULL")
            edt_money.error = "Please insert some money"
            return false
        }
        if (edt_money.text.toString().replace(",".toRegex(), "").toInt() < 0) {
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

    private fun getCurrentTimeInSecond(): Int = (System.currentTimeMillis() / 1000).toInt()

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
                formatter.applyPattern("#,###,###,###,###,###")
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
            val calculatedResult = textCalculator.calculateResult(p0.toString())
            finalNUmber.text = calculatedResult.toString()


            edt_money.addTextChangedListener(this)
        }

    }


    private val bottomSheetCallback = object : BottomSheetBehavior.BottomSheetCallback() {

        override fun onStateChanged(bottomSheet: View, newState: Int) {
        }

        override fun onSlide(bottomSheet: View, slideOffset: Float) {
        }
    }
}