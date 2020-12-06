package com.example.jibi.ui.main.transaction

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import com.example.jibi.R
import com.example.jibi.di.main.MainScope
import com.example.jibi.models.Record
import kotlinx.android.synthetic.main.fragment_add_transaction.*
import kotlinx.android.synthetic.main.layout_transaction_list_item.*
import javax.inject.Inject

@MainScope
class AddTransactionFragment
@Inject
constructor(
    viewModelFactory: ViewModelProvider.Factory
) : BaseTransactionFragment(
    R.layout.fragment_add_transaction,
    viewModelFactory
) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setHasOptionsMenu(true)
    }

    private fun insertNewTrans() {

//        val trnsaction = Record(
//            id = 0,
//            money = edt_money.text.toString().toInt(),
//            memo =,
//            cat_id =,
//            date = getCurrentTimeInSecond()
//        )
        Toast.makeText(this.requireContext(), "CHECKED", Toast.LENGTH_LONG).show()
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
        //TODO SAVE THE DATA TO VIEWSTATE
    }
}