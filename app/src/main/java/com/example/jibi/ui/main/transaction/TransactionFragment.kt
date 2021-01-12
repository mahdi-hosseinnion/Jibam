package com.example.jibi.ui.main.transaction

import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.LinearLayout
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.setPadding
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.RequestManager
import com.example.jibi.R
import com.example.jibi.di.main.MainScope
import com.example.jibi.models.Category
import com.example.jibi.models.Record
import com.example.jibi.ui.main.transaction.bottomSheet.CreateNewTransBottomSheet
import com.example.jibi.ui.main.transaction.state.TransactionStateEvent
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_COLLAPSED
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_transaction.*
import kotlinx.android.synthetic.main.layout_transaction_list_item.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import java.text.DecimalFormat
import java.text.NumberFormat
import java.util.*
import javax.inject.Inject
import kotlin.random.Random

@FlowPreview
@ExperimentalCoroutinesApi
@MainScope
class TransactionFragment
@Inject
constructor(
    viewModelFactory: ViewModelProvider.Factory,
    private val requestManager: RequestManager
) : BaseTransactionFragment(
    R.layout.fragment_transaction,
    viewModelFactory
), TransactionListAdapter.Interaction {

    private val TAG = "TransactionFragment"

    private lateinit var recyclerAdapter: TransactionListAdapter

    private lateinit var bottomSheetBehavior: BottomSheetBehavior<LinearLayout>

    private var bottomSheetPeekHeight = 0

    private var closeBottomWidth = 0
    private var bottomSheetRadios = 0
    private var lastRecyclerState = 0

    private val bottomSheetCallback = object : BottomSheetBehavior.BottomSheetCallback() {

        override fun onStateChanged(bottomSheet: View, newState: Int) {
            if (BottomSheetBehavior.STATE_EXPANDED == newState) {
                showTransactionListToolBar()
                last_transacion_app_bar.isLiftOnScroll = false
            } else {
//                if (BottomSheetBehavior.STATE_COLLAPSED == newState) {
                last_transacion_app_bar.isLiftOnScroll = true
                hideTransactionListToolBar()
            }

        }

        override fun onSlide(bottomSheet: View, slideOffset: Float) {
            main_bottom_sheet_back_arrow.alpha = slideOffset
            if (bottomSheetRadios < 1) {
                bottomSheetRadios = convertDpToPx(8)
            }
            val bottomSheetBackGround = main_standardBottomSheet.background as GradientDrawable
            val topHeight = (bottomSheetRadios * (1f - slideOffset))

            //change bottom sheet raidus
            bottomSheetBackGround.cornerRadius = topHeight
//            main_standardBottomSheet.setPadding(0, topHeight.toInt(), 0, 0)
            //change top of bottomSheet height
            val viewParams = view_hastam.layoutParams
            viewParams.height = topHeight.toInt()
            view_hastam.layoutParams = viewParams
            view_hastam2.alpha = (1f - slideOffset)

            // make the toolbar close button animation
            if (closeBottomWidth < 1) {
                closeBottomWidth = convertDpToPx(56)
            }
            val closeButtonParams =
                main_bottom_sheet_back_arrow.layoutParams as ViewGroup.LayoutParams
            closeButtonParams.width = (slideOffset * closeBottomWidth).toInt()
            main_bottom_sheet_back_arrow.layoutParams = closeButtonParams

        }
    }

    fun showTransactionListToolBar() {
//        view_hastam.visibility = View.GONE
//        lastTransaction.visibility = View.GONE
//        bottom_sheet_toolbar.visibility = View.VISIBLE
//        main_standardBottomSheet.setBackgroundResource(R.color.white)
    }

    fun hideTransactionListToolBar() {
//        view_hastam.visibility = View.VISIBLE
//        lastTransaction.visibility = View.VISIBLE
//        bottom_sheet_toolbar.visibility = View.GONE
//        main_standardBottomSheet.setBackgroundResource(R.drawable.bottom_sheet_bg)

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //set width for closeButtonAnimation
        closeBottomWidth = convertDpToPx(56)
        bottomSheetRadios = convertDpToPx(8)

        bottomSheetBehavior = BottomSheetBehavior.from(main_standardBottomSheet)

        initRecyclerView()
        subscribeObservers()
        fab.setOnClickListener { view ->
            showBottomSheet()
        }
        txt_balance.setOnClickListener {
            insertRandomTransaction()
        }
        main_bottom_sheet_back_arrow.setOnClickListener {
            bottomSheetBehavior.state = STATE_COLLAPSED
        }
    }


    private fun insertRandomTransaction() {
        if (Random.nextBoolean()) {
            viewModel.launchNewJob(
                TransactionStateEvent.OneShotOperationsTransactionStateEvent.InsertTransaction(
                    Record(
                        id = 0,
                        money = Random.nextInt(-1000, 1000),
                        memo = null,
                        cat_id = Random.nextInt(42),
                        date = Random.nextInt(
                            System.currentTimeMillis().minus(604_800L).toInt(),
                            System.currentTimeMillis().toInt()
                        )
                    )
                )
            )
        } else {
            viewModel.launchNewJob(
                TransactionStateEvent.OneShotOperationsTransactionStateEvent.InsertTransaction(
                    Record(
                        id = 0,
                        money = Random.nextInt(-1000, 1000),
                        memo = "memo ${Random.nextInt(451252)}",
                        cat_id = Random.nextInt(42),
                        date = Random.nextInt(
                            System.currentTimeMillis().minus(604_800L).toInt(),
                            System.currentTimeMillis().toInt()
                        )
                    )
                )
            )
        }
    }

    private fun convertDpToPx(dp: Int): Int {
        val r = resources
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            dp.toFloat(),
            r.displayMetrics
        ).toInt()
    }

    private fun showBottomSheet() {
//        activity?.let {
        val modalBottomSheet =
            CreateNewTransBottomSheet(viewModel.viewState.value!!.categoryList!!, requestManager)
//            modalBottomSheet.show(it.supportFragmentManager, "CreateNewTransBottomSheet")
//        }
        modalBottomSheet.show(parentFragmentManager, "CreateNewTransBottomSheet")
    }

    private fun subscribeObservers() {
        viewModel.countOfActiveJobs.observe(viewLifecycleOwner, Observer {
            showProgressBar(viewModel.areAnyJobsActive())
        })
        viewModel.viewState.observe(viewLifecycleOwner) { viewState ->
            viewState?.let {
                Log.d(TAG, "submitList: called transacionLIst: ${it.transactionList}")
                it.transactionList?.let { transactionList ->
                    recyclerAdapter.submitList(transactionList, true)
                }
                it.summeryMoney?.let { summeryMoney ->
                    summeryMoney.balance = (summeryMoney.income + summeryMoney.expenses)
                    txt_balance.text = separate3By3(summeryMoney.balance)
                    txt_expenses.text = separate3By3(summeryMoney.expenses)
                    txt_income.text = separate3By3(summeryMoney.income)
                }
            }
        }
        viewModel.stateMessage.observe(viewLifecycleOwner) { stateMessage ->
            stateMessage?.let {
                /*               if (isPaginationDone(stateMessage.response.message)) {
                viewModel.setQueryExhausted(true)
                viewModel.clearStateMessage()
            } else {
            uiCommunicationListener.onResponseReceived(
                response = it.response,
                stateMessageCallback = object : StateMessageCallback {
                    override fun removeMessageFromStack() {
                        viewModel.clearStateMessage()
                    }
                }
            )
        }*/
                Toast.makeText(
                    this@TransactionFragment.context,
                    "Message: ${it.response.message} \n Type: ${it.response.uiComponentType} \n MessageType: ${it.response.messageType}",
                    Toast.LENGTH_LONG
                ).show()
                viewModel.clearStateMessage()
            }
        }
    }

    private fun separate3By3(money1: Int): String {
        var money = money1
        if (money < 0) {
            money *= -1
        }
        if (money < 1000) {
            return money.toString()
        }
        val formatter: DecimalFormat = NumberFormat.getInstance(Locale.US) as DecimalFormat
        formatter.applyPattern("#,###,###,###")
        return formatter.format(money)
    }

    private fun initRecyclerView() {

        transaction_recyclerView.apply {
            layoutManager = LinearLayoutManager(this@TransactionFragment.context)
            recyclerAdapter = object : TransactionListAdapter(
                requestManager,
                this@TransactionFragment,
                this@TransactionFragment.requireActivity().packageName
            ) {

                override fun getCategoryByIdFromRoot(id: Int): Category {
                    viewModel.viewState.value?.categoryList?.let {
                        for (item in it) {
                            if (item.id == id) {
                                return item
                            }
                        }
                    }
                    return Category(
                        -1,
                        -1,
                        "UNKWON CATEGORY id: $id and size: ${viewModel.viewState.value?.categoryList?.size}",
                        "NULL",
                        -1
                    )
                }
            }
//            addOnScrollListener(object: RecyclerView.OnScrollListener(){
//
//                override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
//                    super.onScrollStateChanged(recyclerView, newState)
//                    val layoutManager = recyclerView.layoutManager as LinearLayoutManager
//                    val lastPosition = layoutManager.findLastVisibleItemPosition()
//                    if (lastPosition == recyclerAdapter.itemCount.minus(1)) {
//                        Log.d(TAG, "BlogFragment: attempting to load next page...")
//                        viewModel.nextPage()
//                    }
//                }
//            })
            transaction_recyclerView.isNestedScrollingEnabled = true
            adapter = recyclerAdapter
        }

    }

    private fun showProgressBar(isLoading: Boolean) {
        if (isLoading) {
            progressBar.visibility = View.VISIBLE
        } else {
            progressBar.visibility = View.INVISIBLE
        }
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume: called")
        //set bottom sheet peek height
        fragment_transacion_root.viewTreeObserver.addOnGlobalLayoutListener(object :
            ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                fragment_transacion_root.viewTreeObserver.removeOnGlobalLayoutListener(this)
                val rootHeight = fragment_transacion_root.height
                val layoutHeight = transaction_fragment_view.height
                Log.d(TAG, "onGlobalLayout: rootHeight:$rootHeight and layoutHeight $layoutHeight ")
                if (bottomSheetPeekHeight < 1) {
                    bottomSheetPeekHeight = rootHeight - layoutHeight
                }
                bottomSheetBehavior.peekHeight = bottomSheetPeekHeight
            }

        })

        bottomSheetBehavior.addBottomSheetCallback(bottomSheetCallback)

        uiCommunicationListener.hideToolbar()

        viewModel.countOfNonCancellableJobs.observe(viewLifecycleOwner, Observer {
            it?.let {
                if (it > 0) {
                    viewModel.runPendingJobs()
                }
            }

        })

    }


    override fun onItemSelected(position: Int, item: Record) {
        viewModel.setDetailTransFields(item)
        findNavController().navigate(R.id.action_transactionFragment_to_detailTransFragment)
    }

    override fun restoreListPosition() {
    }
}