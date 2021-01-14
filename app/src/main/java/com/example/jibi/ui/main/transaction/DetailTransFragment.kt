package com.example.jibi.ui.main.transaction

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.RequestManager
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.example.jibi.R
import com.example.jibi.models.Category
import com.example.jibi.models.Record
import com.example.jibi.repository.buildResponse
import com.example.jibi.ui.main.transaction.state.TransactionStateEvent
import com.example.jibi.util.AreYouSureCallback
import com.example.jibi.util.MessageType
import com.example.jibi.util.StateMessageCallback
import com.example.jibi.util.UIComponentType
import kotlinx.android.synthetic.main.fragment_detail_trans.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import java.text.DecimalFormat
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject


@FlowPreview
@ExperimentalCoroutinesApi
class DetailTransFragment
@Inject
constructor(
    viewModelFactory: ViewModelProvider.Factory,
    private val requestManager: RequestManager,
    private val currentLocale: Locale
) : BaseTransactionFragment(
    R.layout.fragment_detail_trans,
    viewModelFactory
), View.OnClickListener {

    private val TAG = "DetailTransFragment"
    var transaction_catId: Int = -1

    var transaction: Record? = null
    var category: Category? = null

    private var changeList = mutableSetOf<String>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setHasOptionsMenu(true)
        subscribeObservers()
        edt_money_detail.addTextChangedListener(onTextChangedListener)
        prepareEditTexts()
        fab_submitChanges.hide()
        fab_submitChanges.setOnClickListener {
            saveChanges()
        }
    }

    private fun prepareEditTexts() {
        //add on click
        edt_money_detail.setOnClickListener(this)
        edt_memo_detail.setOnClickListener(this)
        txt_date_detail.setOnClickListener(this)
        txt_time_detail.setOnClickListener(this)

        //clear focus from edit texts
        disableContentInteraction(edt_money_detail)
        disableContentInteraction(edt_memo_detail)
        disableContentInteraction(txt_date_detail)
        disableContentInteraction(txt_time_detail)
        //add on text change listener for memo
        edt_memo_detail.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun afterTextChanged(p0: Editable?) {
                val currentText = p0.toString()
                if (currentText != transaction?.memo) {
                    setToEditMode(MEMO)
                } else {
                    setToViewMode(MEMO)
                }
            }
        })
    }

    private fun subscribeObservers() {
        viewModel.viewState.observe(viewLifecycleOwner, Observer { viewState ->
            viewState?.detailTransFields?.let {
                //TODO NOT SET IT TILL NOW
                transaction = it
                setTranProperties(it)
                transaction_catId = it.cat_id
            }
            viewState?.categoryList?.let {
                Log.d(TAG, "subscribeObservers: called with $transaction_catId * 00$it")
                if (transaction_catId >= 0) {
                    for (item in it) {
                        if (item.id == transaction_catId) {
                            category = item
                            setCategoryProperties(item)
                            break
                        }
                    }
                }
            }
        })
    }

    private fun saveChanges() {
        if (handleInsertingErrors()) {
            uiCommunicationListener.hideSoftKeyboard()
            var memo: String? = edt_memo_detail.text.toString()
            //check if memo is blank then just save null
            if (memo.isNullOrBlank()) {
                memo = null
            }
            var money: Int = (edt_money_detail.text.toString().replace(",".toRegex(), "").toInt())

            if (category?.type == 1) {
                money *= -1
            }
            val tempTansaction = Record(
                id = transaction?.id ?: 0,
                money = money,
                memo = memo,
                cat_id = category!!.id,
                date = getUpdatedTime()
            )

            viewModel.launchNewJob(
                TransactionStateEvent.OneShotOperationsTransactionStateEvent.InsertTransaction(
                    tempTansaction
                ), true
            )
            findNavController().navigateUp()
        }
    }

    private fun handleInsertingErrors(): Boolean {
        var errorMsg = ""
        if (edt_money_detail.text.toString().replace(",".toRegex(), "").isBlank()) {
            Log.e(TAG, "MONEY IS NULL")
            edt_money_detail.error = "Please insert some money"
            return false
        }
        if (edt_money_detail.text.toString().replace(",".toRegex(), "").toInt() < 0) {
            Log.e(TAG, "MONEY IS INVALID MOENY")
            edt_money_detail.error = "money should be grater then 0"
            return false
        }
        if (category == null) {
            Log.e(TAG, "CATEGORY == NULL")
            errorMsg = "Please select category"
        }
        if (category?.id == null) {
            Log.e(TAG, "CATEGORY ID == NULL")
            errorMsg = "Please select category"
        }
        if (category?.id!! < 1) {
            Log.e(TAG, "CATEGORY ID == -1")
            errorMsg = "Please select category"

        }
        if (category?.type == null) {
            Log.e(TAG, "CATEGORY type == NULL")
            errorMsg = "Please select category"
        }
        if (category?.type!! < 1) {
            Log.e(TAG, "CATEGORY type == -1")
            errorMsg = "Please select category"
        }
        if (errorMsg.isNotBlank()) {
            Toast.makeText(requireContext(), errorMsg, Toast.LENGTH_LONG).show()
            return false
        }
        return true
    }

    private fun getUpdatedTime(): Int {
        //TODO ADD CHANGE TIME FUTARE
        return transaction?.date ?: 0
    }

    private fun setTranProperties(trans: Record) {
        if (trans.money > 0)
            edt_money_detail.setText(trans.money.toString())
        else
            edt_money_detail.setText((trans.money.times(-1)).toString())

        trans.memo?.let { edt_memo_detail.setText(it) }
        setDateProperties(trans.date)
    }

    private fun setCategoryProperties(category: Category) {
        category_name_detail.setText(category.name)
        val categoryImageUrl = this.resources.getIdentifier(
            "ic_cat_${category.name}",
            "drawable",
            requireActivity().packageName
        )

        image_cardView.setCardBackgroundColor(
            resources.getColor(
                TransactionListAdapter.listOfColor[(category.id.minus(
                    1
                ))]
            )
        )

        requestManager
            .load(categoryImageUrl)
            .centerInside()
            .transition(DrawableTransitionOptions.withCrossFade())
            .error(R.drawable.ic_error)
            .into(category_image_detail)

    }


    private fun setDateProperties(time: Int) {
        val dv: Long = ((time.toLong()) * 1000) // its need to be in milisecond
        val df: Date = Date(dv)
        txt_date_detail.setText(SimpleDateFormat("MM/dd/yy (E)", currentLocale).format(df))
        txt_time_detail.setText(SimpleDateFormat("K:m a", currentLocale).format(df))
    }

    override fun onResume() {
        uiCommunicationListener.showToolbar()
        super.onResume()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.delete_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.delete -> {
                confirmDeleteRequest()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun confirmDeleteRequest() {
        val callback = object : AreYouSureCallback {
            override fun proceed() {
                deleteNote()
            }

            override fun cancel() {
            }

        }
        uiCommunicationListener.onResponseReceived(
            response = buildResponse(
                getString(R.string.are_you_sure_delete),
                UIComponentType.AreYouSureDialog(callback),
                MessageType.Info
            ),
            stateMessageCallback = object : StateMessageCallback {
                override fun removeMessageFromStack() {
                    //TODO maybe a bug
//                    viewModel.clearStateMessage()
                }

            }
        )
    }

    private fun deleteNote() {
        viewModel.viewState.value?.detailTransFields?.let {
            viewModel.launchNewJob(
                TransactionStateEvent.OneShotOperationsTransactionStateEvent.DeleteTransaction(
                    it
                )
            )
        }
        findNavController().navigateUp()
    }

    private fun disableContentInteraction(edt: EditText) {
        edt.keyListener = null
        edt.isFocusable = false
        edt.isFocusableInTouchMode = false
        edt.isCursorVisible = false
        edt.clearFocus()
    }

    private fun enableContentInteraction(edt: EditText) {
        val tempEditText = EditText(this.requireContext())
        if (edt.id == R.id.edt_money_detail) {
            //set input type for money editText to number
            tempEditText.inputType = InputType.TYPE_CLASS_NUMBER
        }
        edt.keyListener = tempEditText.keyListener
        edt.isFocusable = true
        edt.isFocusableInTouchMode = true
        edt.isCursorVisible = true
        edt.requestFocus()
        //force to open keyboard
        val imm: InputMethodManager =
            activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(edt, InputMethodManager.SHOW_IMPLICIT)
        if (edt.text != null) {
            edt.setSelection(edt.text.length)
        }
        //reset to on click
        edt.setOnClickListener(null)
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.edt_money_detail -> {
                enableContentInteraction(view as EditText)
            }
            R.id.edt_memo_detail -> {
                enableContentInteraction(view as EditText)
            }
            R.id.txt_date_detail -> {
                //TODO Open date picker
            }
            R.id.txt_time_detail -> {
                //TODO open time picker
            }
        }
    }

    private val onTextChangedListener = object : TextWatcher {
        override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

        override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

        override fun afterTextChanged(p0: Editable?) {
            edt_money_detail.removeTextChangedListener(this)

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
                edt_money_detail.setText(formattedString)
                edt_money_detail.setSelection(edt_money_detail.text!!.length)

                //check for set to editable mode
                Log.d(
                    TAG,
                    "afterTextChanged: long ${longval.toInt()} & trans: ${transaction?.money}"
                )
                val originalValue = if (transaction?.money ?: 0 >= 0) {
                    transaction?.money ?: 0
                } else {
                    transaction?.money?.unaryMinus() ?: 0
                }
                if (longval.toInt() != originalValue) {
                    setToEditMode(MONEY)
                } else {
                    setToViewMode(MONEY)
                }
            } catch (nfe: NumberFormatException) {
                nfe.printStackTrace()
            } catch (e: Exception) {
                Log.e(TAG, "afterTextChanged: ", e)
            }

            edt_money_detail.addTextChangedListener(this)
        }

    }

    fun setToEditMode(key: String) {
        fab_submitChanges.show()
        changeList.add(key)
        fake_txt.text = changeList.toString()

    }

    fun setToViewMode(key: String) {
        changeList.remove(key)
        if (changeList.isEmpty()) {
            fab_submitChanges.hide()
        }
        fake_txt.text = changeList.toString()
    }

    companion object {
        private const val CATEGORY = "CATEGORY"
        private const val MONEY = "MONEY"
        private const val MEMO = "MEMO"
        private const val DATE = "DATE"
        private const val TIME = "TIME"
    }
}