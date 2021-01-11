package com.example.jibi.ui.main.transaction

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
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
import kotlinx.android.synthetic.main.fragment_add_transaction.*
import kotlinx.android.synthetic.main.fragment_detail_trans.*
import kotlinx.android.synthetic.main.layout_transaction_list_item.view.*
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
) {

    private val TAG = "DetailTransFragment"
    var transactionId: Int = -1

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setHasOptionsMenu(true)
        subscribeObservers()
        edt_money_detail.addTextChangedListener(onTextChangedListener)

    }

    private fun subscribeObservers() {
        viewModel.viewState.observe(viewLifecycleOwner, Observer { viewState ->
            viewState?.detailTransFields?.let {
                setTranProperties(it)
                transactionId = it.id
            }
            viewState?.categoryList?.let {
                Log.d(TAG, "subscribeObservers: called with $transactionId * 00$it")
                if (transactionId >= 0) {
                    for (item in it) {
                        if (item.id == transactionId) {
                            setCategoryProperties(item)
                            break
                        }
                    }
                }
            }
        })
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
                edt_money_detail.setSelection(edt_money.text!!.length)
            } catch (nfe: NumberFormatException) {
                nfe.printStackTrace()
            } catch (e: Exception) {
                Log.e(TAG, "afterTextChanged: ", e)
            }

            edt_money_detail.addTextChangedListener(this)
        }

    }
}