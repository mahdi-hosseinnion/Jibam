package com.example.jibi.ui.main.transaction

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.jibi.R
import com.example.jibi.di.main.MainScope
import com.example.jibi.models.Category
import com.example.jibi.models.Record
import com.example.jibi.ui.main.transaction.state.TransactionStateEvent
import kotlinx.android.synthetic.main.fragment_add_transaction.*
import kotlinx.android.synthetic.main.fragment_add_transaction.view.*
import kotlinx.android.synthetic.main.layout_category_list_item.*
import kotlinx.android.synthetic.main.layout_transaction_list_item.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import java.text.DecimalFormat
import java.text.NumberFormat
import java.util.*
import javax.inject.Inject


@ExperimentalCoroutinesApi
@MainScope
class AddTransactionFragment
@Inject
constructor(
    viewModelFactory: ViewModelProvider.Factory
) : BaseTransactionFragment(
    R.layout.fragment_add_transaction,
    viewModelFactory
) {
    private val TAG = "AddTransactionFragment"

    private val args: AddTransactionFragmentArgs by navArgs()
    private var category: Category? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setHasOptionsMenu(true)
        category = findCategory(cat_id = args.categoryId)
        setTransProperties(category = category)
        initUi()
        edt_money.addTextChangedListener(onTextChangedListener)
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

    private fun initUi() {
        //make edt category nonEditable
        edt_category.keyListener = null
        //Implementing an exposed dropdown menu for wallet editText
        addOptionsToWallet()
        //force keyboard to open up when addFragment launches
        forceKeyBoardToOpenForMoneyEditText()

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
        category?.name?.let { edt_category.setText(it) }
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
            var money: Int = (edt_money.text.toString().toInt())
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
        if (edt_money.text.toString().isBlank()) {
            Log.e(TAG, "MONEY IS NULL")
            edt_money.error = "Please insert some money"
            return false
        }
        if (edt_money.text.toString().toInt() < 0) {
            Log.e(TAG, "MONEY IS INVALID MOENY")
            edt_money.error = "money should be grater then 0"
            return false
        }
        if (category == null) {
            Log.e(TAG, "CATEGORY == NULL")
            edt_category.error = "Please select category"
        }
        if (category?.id == null) {
            Log.e(TAG, "CATEGORY ID == NULL")
            edt_category.error = "Please select category"
            return false
        }
        if (category?.id!! < 1) {
            Log.e(TAG, "CATEGORY ID == -1")
            edt_category.error = "Please select category"
            return false
        }
        if (category?.type == null) {
            Log.e(TAG, "CATEGORY type == NULL")
            edt_category.error = "Please select category"
            return false
        }
        if (category?.type!! < 1) {
            Log.e(TAG, "CATEGORY type == -1")
            edt_category.error = "Please select category"
            return false
        }

        return true
    }

    private fun getCurrentTimeInSecond(): Int = (System.currentTimeMillis() / 1000).toInt()

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

    override fun onPause() {
        super.onPause()
    }

    private val onTextChangedListener = object : TextWatcher {
        override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

        override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

        override fun afterTextChanged(p0: Editable?) {
            edt_money.removeTextChangedListener(this)

            try {
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
                edt_money.setSelection(edt_money.text!!.length)
            } catch (nfe: NumberFormatException) {
                nfe.printStackTrace()
            } catch (e: Exception) {
                Log.e(TAG, "afterTextChanged: ", e)
            }

            edt_money.addTextChangedListener(this)
        }

    }

}