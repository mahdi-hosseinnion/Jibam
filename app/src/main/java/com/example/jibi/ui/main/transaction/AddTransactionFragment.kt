package com.example.jibi.ui.main.transaction

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.jibi.R
import com.example.jibi.di.main.MainScope
import com.example.jibi.models.Record
import com.example.jibi.ui.main.transaction.state.TransactionStateEvent
import kotlinx.android.synthetic.main.fragment_add_transaction.*
import kotlinx.android.synthetic.main.layout_category_list_item.*
import kotlinx.android.synthetic.main.layout_transaction_list_item.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
    private val args: AddTransactionFragmentArgs by navArgs()
    private var categoryId: Int? = null
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setHasOptionsMenu(true)
        categoryId = args.categoryId
        setTransProperties(cat_id = categoryId)
    }

    private fun setTransProperties(record: Record) {

    }

    private fun setTransProperties(
        money: Int? = null,
        cat_id: Int? = null,
        memo: String? = null,
        specificDate: Int? = null,
        wallet_id: Int? = null,
        with: String? = null,
    ) {
        money?.let { edt_money.setText(it) }
        memo?.let { edt_memo.setText(memo) }
        cat_id?.let {
            if (it == -1) {
                return@let
            }
            viewModel.viewState.value?.categoryList?.let { categoryList ->
                for (category in categoryList) {
                    if (category.id == cat_id) {
                        edt_category.setText(category.name)
                        break
                    }
                }
            }
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
            val transaction = Record(
                id = 0,
                money = edt_money.text.toString().toInt(),
                memo = memo,
                cat_id = categoryId!!,
                date = getCurrentTimeInSecond()
            )
            //b/c we navigate back and fragment get destroyed so viewModel get destroyed and job
            //get cancelled so we should launch this as global job in activity
            uiCommunicationListener.launchNewGlobalJob(
                TransactionStateEvent.OneShotOperationsTransactionStateEvent.InsertTransaction(
                    Record(
                        id = 0,
                        money = edt_money.text.toString().toInt(),
                        memo = memo,
                        cat_id = categoryId!!,
                        date = getCurrentTimeInSecond()
                    )
                )
            )
            uiCommunicationListener.hideSoftKeyboard()
            findNavController().navigateUp()
        }
    }

    private fun handleInsertingErrors(): Boolean {
        if (edt_money.text.toString().toInt() < 0) {
            edt_money.error = "Please insert some money"
            return false
        }
        if (categoryId == null) {
            edt_category.error = "Please select category"
            return false
        }
        if (categoryId!! < 1) {
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
        //TODO SAVE THE DATA TO VIEW STATE
    }
}