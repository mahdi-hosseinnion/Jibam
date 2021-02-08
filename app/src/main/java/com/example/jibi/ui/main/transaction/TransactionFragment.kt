package com.example.jibi.ui.main.transaction

import android.content.Context
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.RequestManager
import com.example.jibi.R
import com.example.jibi.di.main.MainScope
import com.example.jibi.models.Category
import com.example.jibi.models.Record
import com.example.jibi.models.SearchModel
import com.example.jibi.repository.buildResponse
import com.example.jibi.ui.main.transaction.bottomSheet.CreateNewTransBottomSheet
import com.example.jibi.ui.main.transaction.state.TransactionStateEvent
import com.example.jibi.util.*
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_COLLAPSED
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_add_transaction.*
import kotlinx.android.synthetic.main.fragment_transaction.*
import kotlinx.android.synthetic.main.layout_transaction_list_item.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.launch
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
    private var normalAppBarHeight = 0


    private var lastSlideValue = -1f

    private var searchModel = SearchModel()

    private sealed class SearchViewState {
        object VISIBLE : SearchViewState()
        object INVISIBLE : SearchViewState()

        fun isVisible(): Boolean = this == VISIBLE
        fun isInvisible(): Boolean = this == INVISIBLE
    }

//    private fun SearchViewState.isVisible(): Boolean {
//        return (this == SearchViewStateEnum.VISIBLE)
//    }

    private var searchViewState: SearchViewState = SearchViewState.INVISIBLE

    private val bottomSheetCallback = object : BottomSheetBehavior.BottomSheetCallback() {

        override fun onStateChanged(bottomSheet: View, newState: Int) {
            onBottomSheetStateChanged(newState)

        }

        override fun onSlide(bottomSheet: View, slideOffset: Float) {
            onBottomSheetSlide(slideOffset)
        }
    }
    private val backStackForBottomSheet = object : OnBackPressedCallback(false) {
        override fun handleOnBackPressed() {
            //check for search view
            if (searchViewState.isVisible()) {
                disableSearchMode()
            } else {
                transaction_recyclerView.scrollToPosition(0)
                bottomSheetBehavior.state = STATE_COLLAPSED
                this.isEnabled = false
            }
        }

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requireActivity().onBackPressedDispatcher.addCallback(backStackForBottomSheet)
        //set width for closeButtonAnimation
        closeBottomWidth = convertDpToPx(56)
        bottomSheetRadios = convertDpToPx(16)
        normalAppBarHeight = convertDpToPx(40)

        bottomSheetBehavior = BottomSheetBehavior.from(main_standardBottomSheet)

        bottomSheetBehavior.addBottomSheetCallback(bottomSheetCallback)

        initRecyclerView()
        subscribeObservers()
        fab.setOnClickListener { view ->
            showBottomSheet()
        }
        txt_balance.setOnClickListener {
            insertRandomTransaction()
        }
        main_bottom_sheet_back_arrow.setOnClickListener {
            //check for search view
            if (searchViewState.isVisible()) {
                disableSearchMode()
            } else {
                transaction_recyclerView.scrollToPosition(0)
                bottomSheetBehavior.state = STATE_COLLAPSED
            }
        }
        //search view stuff
        main_bottom_sheet_search_btn.setOnClickListener {
            enableSearchMode()
        }
        bottom_sheet_search_clear.setOnClickListener {
            bottom_sheet_search_edt.setText("")
        }


    }

    private fun enableSearchMode() {
        if (searchViewState.isInvisible()) {
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED


            //force to slide
//            onBottomSheetStateChanged(BottomSheetBehavior.STATE_EXPANDED)
//            onBottomSheetSlide(1f)

            //visible search stuff
            bottom_sheet_search_edt.visibility = View.VISIBLE
            bottom_sheet_search_clear.visibility = View.INVISIBLE

            bottom_sheet_search_edt.addTextChangedListener(onSearchViewTextChangeListener)
            forceKeyBoardToOpenForMoneyEditText(bottom_sheet_search_edt)
            //invisible search stuff
            main_bottom_sheet_search_btn.visibility = View.GONE
            main_bottom_sheet_filter_btn.visibility = View.GONE
            bottom_sheet_title.visibility = View.GONE
            //this should come after all
            searchViewState = SearchViewState.VISIBLE
        }
    }

    private fun disableSearchMode() {
        if (searchViewState.isVisible()) {
            Log.d(TAG, "disableSearchMode: 765 called and setted to invisible")

            //invisible search stuff
            bottom_sheet_search_edt.visibility = View.GONE
            bottom_sheet_search_clear.visibility = View.GONE

            uiCommunicationListener.hideSoftKeyboard()
            bottom_sheet_search_edt.removeTextChangedListener(onSearchViewTextChangeListener)
            bottom_sheet_search_edt.setText("")
            //visible search stuff
            main_bottom_sheet_search_btn.visibility = View.VISIBLE
            main_bottom_sheet_filter_btn.visibility = View.VISIBLE
            bottom_sheet_title.visibility = View.VISIBLE

            //this should come after all
            searchViewState = SearchViewState.INVISIBLE
            //clear search
            searchModel = searchModel.copy(query = "")
            //submit that
            lifecycleScope.launch {
//                viewModel.queryChannel.value = SearchModel(query = p0.toString())
                viewModel.queryChannel.emit(searchModel)
                //add new search query
                Log.d(TAG, "searchDEBUG: clear")
            }
        }
    }

    private val onSearchViewTextChangeListener = object : TextWatcher {
        override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

        }

        override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            if (p0.isNullOrEmpty()) {
                bottom_sheet_search_clear.visibility = View.INVISIBLE
            } else {
                bottom_sheet_search_clear.visibility = View.VISIBLE
            }
            //search for something
        }

        override fun afterTextChanged(p0: Editable?) {
            searchModel = searchModel.copy(query = p0.toString())

            //submit that
            lifecycleScope.launch {
//                viewModel.queryChannel.value = SearchModel(query = p0.toString())
                viewModel.queryChannel.emit(searchModel)
                //add new search query
                Log.d(TAG, "searchDEBUG: 1-- $p0")
            }
        }
    }

    private fun insertRandomTransaction() {
        if (Random.nextBoolean()) {
            viewModel.launchNewJob(
                TransactionStateEvent.OneShotOperationsTransactionStateEvent.InsertTransaction(
                    Record(
                        id = 0,
                        money = Random.nextInt(-1000, 1000).toDouble(),
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
                        money = Random.nextInt(-1000, 1000).toDouble(),
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
                val action =
                    TransactionFragmentDirections.actionTransactionFragmentToCreateTransactionFragment(
                        categoryId = item.id
                    )
                findNavController().navigate(action)
            }

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
                    txt_balance.text = separate3By3AndRoundIt(summeryMoney.balance)
                    txt_expenses.text = separate3By3AndRoundIt(summeryMoney.expenses)
                    txt_income.text = separate3By3AndRoundIt(summeryMoney.income)
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

    private fun separate3By3AndRoundIt(money: Double): String {
        //TIP this method will round -354.3999999999942 to -354.4
        Log.d("456987", "separate3By3AndRoundIt: start with $money")

        //seprate 3 by 3 part
//        val finalResult = if (money > 1_000.0 && money < -1_000.0) {
//            money.toString()
//        } else {
        val formatter: DecimalFormat = NumberFormat.getInstance(Locale.US) as DecimalFormat
        formatter.applyPattern("#,###,###,###.###")
        //if you use this pattern it will round to two decimal
//        formatter.applyPattern("#,###,###,###.##")
        val finalResult = formatter.format(money).toString()
//        }

        if ((finalResult.indexOf('.')) == -1) {
            return finalResult
        }

        //round part
        if (finalResult.substring(finalResult.lastIndex.minus(1)) == ".0") {
            //convert 15.0 to 15
            return finalResult.substring(
                startIndex = 0,
                endIndex = finalResult.lastIndex.minus(1)
            )
        }

        val periodPosition = finalResult.indexOf('.')

        return if (periodPosition > -1 && periodPosition.plus(3) < finalResult.length) {
            //convert 19.23423424 to 19.23
            finalResult.substring(0, periodPosition.plus(3))
        } else {
            finalResult
        }

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
            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    if (bottomSheetBehavior.state == BottomSheetBehavior.STATE_EXPANDED) {
                        if (dy > 0) {
                            fab.hide()
                        } else {
                            fab.show()
                        }

                    }
                }
//                override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
//                    super.onScrollStateChanged(recyclerView, newState)
//                    val layoutManager = recyclerView.layoutManager as LinearLayoutManager
//                    val lastPosition = layoutManager.findLastVisibleItemPosition()
//                    if (lastPosition == recyclerAdapter.itemCount.minus(1)) {
//                        Log.d(TAG, "BlogFragment: attempting to load next page...")
//                        viewModel.nextPage()
//                    }
//                }
            })

//            //swipe to delete
            val swipeHandler =
                object : SwipeToDeleteCallback(this@TransactionFragment.requireContext()) {
                    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {

                        val adapter = transaction_recyclerView.adapter as TransactionListAdapter
                        val deletedTrans = adapter.getRecord(viewHolder.adapterPosition)
//                        delete from list
                        val removedHeader = adapter.removeAt(viewHolder.adapterPosition)
                        //add to recently deleted
                        viewModel.setRecentlyDeletedTrans(
                            deletedTrans,
                            viewHolder.adapterPosition,
                            removedHeader
                        )
                        //delete from database
                        viewModel.launchNewJob(
                            TransactionStateEvent.OneShotOperationsTransactionStateEvent.DeleteTransaction(
                                deletedTrans
                            )
                        )
                        //show snackBar
                        showUndoSnackBar()

                    }
                }

            val itemTouchHelper = ItemTouchHelper(swipeHandler)
            itemTouchHelper.attachToRecyclerView(transaction_recyclerView)

            transaction_recyclerView.isNestedScrollingEnabled = true
            adapter = recyclerAdapter
        }

    }

    private fun showUndoSnackBar() {
        val undoCallback = object : UndoCallback {
            override fun undo() {
                insertRecentlyDeletedTrans()
            }

            override fun onDismiss() {
                viewModel.setRecentlyDeletedTransToNull()
            }
        }
        uiCommunicationListener.onResponseReceived(
            buildResponse(
                "Transaction successfully deleted",
                UIComponentType.UndoSnackBar(undoCallback, fragment_transacion_root),
                MessageType.Info
            ), object : StateMessageCallback {
                override fun removeMessageFromStack() {
                }
            }
        )
    }

    private fun insertRecentlyDeletedTrans() {
        viewModel.viewState.value?.recentlyDeletedFields?.let { recentlyDeleted ->
            if (recentlyDeleted.recentlyDeletedTrans != null) {
                //insert to list
                recyclerAdapter.insertRecordAt(
                    recentlyDeleted.recentlyDeletedTrans!!,
                    recentlyDeleted.recentlyDeletedTransPosition,
                    recentlyDeleted.recentlyDeletedHeader
                )
                //insert into database
                viewModel.launchNewJob(
                    TransactionStateEvent.OneShotOperationsTransactionStateEvent.InsertTransaction(
                        recentlyDeleted.recentlyDeletedTrans!!
                    )
                )
            } else {
                viewModel.addToMessageStack(
                    message = "Something went wrong \n cannot return deleted transaction back",
                    uiComponentType = UIComponentType.Dialog,
                    messageType = MessageType.Error
                )
            }
        }
        viewModel.setRecentlyDeletedTransToNull()
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
        if (bottomSheetBehavior.state == BottomSheetBehavior.STATE_EXPANDED)
            resetIt(1f)

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
        //reset it
//        onBottomSheetStateChanged(bottomSheetBehavior.state)
//        if (bottomSheetBehavior.state == BottomSheetBehavior.STATE_EXPANDED) {
//            onBottomSheetSlide(0f)
//        }

//        bottomSheetBehavior.addBottomSheetCallback(bottomSheetCallback)

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

    private fun onBottomSheetStateChanged(newState: Int) {


        if (BottomSheetBehavior.STATE_EXPANDED == newState) {
            last_transacion_app_bar.isLiftOnScroll = false
            //enable backStack
            backStackForBottomSheet.isEnabled = true
        } else {
            //disable backStack
            backStackForBottomSheet.isEnabled = false
        }
        last_transacion_app_bar.isLiftOnScroll = true

        if (STATE_COLLAPSED == newState) {

            disableSearchMode()
        }
        if (BottomSheetBehavior.STATE_DRAGGING == newState) {
            fab.hide()
        } else {
            fab.show()
        }

    }

    private fun onBottomSheetSlide(slideOffset: Float) {

        //prevent from calling when recyclerview scrolled
        if (slideOffset == lastSlideValue) {
            return
        } else {
            lastSlideValue = slideOffset
        }
        if (slideOffset <= 0f && bottomSheetBehavior.state != STATE_COLLAPSED) {
            //some bugs happens when keyboard get opened so we should do this
            //when keyboard opens this onSlide method called with value 0
            return
        } else {

            main_bottom_sheet_back_arrow.alpha = slideOffset
            if (bottomSheetRadios < 1) {
                bottomSheetRadios = convertDpToPx(16)
            }
            val bottomSheetBackGround = main_standardBottomSheet.background as GradientDrawable

            val topHeight = (bottomSheetRadios * (1f - slideOffset))

            //change bottom sheet raidus
            bottomSheetBackGround.cornerRadius = topHeight
            main_standardBottomSheet.background = bottomSheetBackGround
            //change app bar
            if (normalAppBarHeight < 1) {
                normalAppBarHeight = convertDpToPx(40)
            }
            last_transacion_app_bar.background = bottomSheetBackGround
//            last_transacion_app_bar.setPadding(topHeight.toInt(), 0, topHeight.toInt(), 0)
            //change app bar height
            val appbarViewParams = last_transacion_app_bar.layoutParams
            appbarViewParams.height = (normalAppBarHeight + (bottomSheetRadios - topHeight.toInt()))
            last_transacion_app_bar.layoutParams = appbarViewParams
            //change buttons height
            val buttonsViewParams = main_bottom_sheet_search_btn.layoutParams
            buttonsViewParams.width = (normalAppBarHeight + (bottomSheetRadios - topHeight.toInt()))

            main_bottom_sheet_search_btn.layoutParams = buttonsViewParams
            main_bottom_sheet_filter_btn.layoutParams = buttonsViewParams
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

    fun resetIt(slideOffset: Float) {
        main_bottom_sheet_back_arrow.alpha = slideOffset
        if (bottomSheetRadios < 1) {
            bottomSheetRadios = convertDpToPx(16)
        }
        val bottomSheetBackGround = main_standardBottomSheet.background as GradientDrawable

        val topHeight = (bottomSheetRadios * (1f - slideOffset))

        //change bottom sheet raidus
        bottomSheetBackGround.cornerRadius = topHeight
        main_standardBottomSheet.background = bottomSheetBackGround
        //change app bar
        if (normalAppBarHeight < 1) {
            normalAppBarHeight = convertDpToPx(40)
        }
        last_transacion_app_bar.background = bottomSheetBackGround
//            last_transacion_app_bar.setPadding(topHeight.toInt(), 0, topHeight.toInt(), 0)
        //change app bar height
        val appbarViewParams = last_transacion_app_bar.layoutParams
        appbarViewParams.height = (normalAppBarHeight + (bottomSheetRadios - topHeight.toInt()))
        last_transacion_app_bar.layoutParams = appbarViewParams
        //change buttons height
        val buttonsViewParams = main_bottom_sheet_search_btn.layoutParams
        buttonsViewParams.width = (normalAppBarHeight + (bottomSheetRadios - topHeight.toInt()))

        main_bottom_sheet_search_btn.layoutParams = buttonsViewParams
        main_bottom_sheet_filter_btn.layoutParams = buttonsViewParams
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

    private fun forceKeyBoardToOpenForMoneyEditText(editText: EditText) {
        editText.requestFocus()
        val imm: InputMethodManager =
            activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT)
    }
}