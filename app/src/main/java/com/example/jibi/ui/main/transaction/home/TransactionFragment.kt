package com.example.jibi.ui.main.transaction.home

import android.content.Context
import android.content.SharedPreferences
import android.content.res.Resources
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
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
import androidx.lifecycle.observe
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
import com.example.jibi.ui.main.transaction.BaseTransactionFragment
import com.example.jibi.ui.main.transaction.state.TransactionStateEvent
import com.example.jibi.util.*
import com.example.jibi.util.PreferenceKeys.PROMOTE_FAB_TRANSACTION_FRAGMENT
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetBehavior.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_add_transaction.*
import kotlinx.android.synthetic.main.fragment_transaction.*
import kotlinx.android.synthetic.main.layout_transaction_list_item.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.launch
import uk.co.samuelwall.materialtaptargetprompt.MaterialTapTargetPrompt
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
    private val requestManager: RequestManager,
    private val currentLocale: Locale,
    private val sharedPreferences: SharedPreferences,
    private val sharedPrefsEditor: SharedPreferences.Editor,
    private val _resources: Resources
) : BaseTransactionFragment(
    R.layout.fragment_transaction,
    viewModelFactory,
    R.id.transaction_toolbar,
    _resources
), TransactionListAdapter.Interaction {

    private val TAG = "TransactionFragment"

    private lateinit var recyclerAdapter: TransactionListAdapter

    private lateinit var bottomSheetBehavior: BottomSheetBehavior<LinearLayout>

    private var bottomSheetPeekHeight = 0

    private val closeBottomWidth by lazy { convertDpToPx(56) }
    private val bottomSheetRadios by lazy { convertDpToPx(16) }
    private val normalAppBarHeight by lazy { convertDpToPx(40) }


    private var searchModel = SearchModel()

    private sealed class SearchViewState {
        object VISIBLE : SearchViewState()
        object INVISIBLE : SearchViewState()

        fun isVisible(): Boolean = this == VISIBLE
        fun isInvisible(): Boolean = this == INVISIBLE
    }

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

        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            backStackForBottomSheet
        )

        bottomSheetBehavior = BottomSheetBehavior.from(main_standardBottomSheet)

        bottomSheetBehavior.addBottomSheetCallback(bottomSheetCallback)
        onBottomSheetStateChanged(bottomSheetBehavior.state)

        initRecyclerView()
        subscribeObservers()

        savedInstanceState?.getString(SEARCH_QUERY)?.let { query ->
            //if user rotate the screen and fragment get rebuild
            Log.d(TAG, "onViewCreated:savedInstanceState withThisQuery: $query")
            if (query.isNotEmpty()) {
                enableSearchMode(query)
            }
        }

        if (searchViewState.isVisible()) {
            enableSearchMode()
        }

        fab.setOnClickListener {
            navigateToAddTransactionFragment(true)
        }
        //TODO DELETE THIS LINE FOR FINAL PROJECT JUST OF TESTING
        txt_balance.setOnClickListener {
            insertRandomTransaction()
        }
        //TODO DELETE THIS LINE FOR FINAL PROJECT JUST OF TESTING
        txt_expenses.setOnClickListener {
            //TODD JUST FOR TESTING
//            resetPromoteState()
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
        checkForGuidePromote()
    }

    private fun resetPromoteState() {
        sharedPrefsEditor.putBoolean(
            PROMOTE_FAB_TRANSACTION_FRAGMENT,
            true
        ).apply()
        sharedPrefsEditor.putBoolean(
            PreferenceKeys.PROMOTE_ADD_TRANSACTION,
            true
        ).apply()
        sharedPrefsEditor.putBoolean(
            PreferenceKeys.PROMOTE_CATEGORY_LIST,
            true
        ).apply()
        sharedPrefsEditor.putBoolean(
            PreferenceKeys.PROMOTE_VIEW_CATEGORY_LIST,
            true
        ).apply()
        sharedPrefsEditor.putBoolean(
            PreferenceKeys.PROMOTE_ADD_CATEGORY_NAME,
            true
        ).apply()
        Toast.makeText(requireContext(), "PROMOTE GUIDE STATE RESET", Toast.LENGTH_SHORT).show()
    }

    private fun enableSearchMode(query: String? = null) {
        bottomSheetBehavior.state = STATE_EXPANDED
        //user shouldn't be able to drag down when searchView is enable
        bottomSheetBehavior.isDraggable = false
        //disconnect recyclerView to bottomSheet
        transaction_recyclerView.isNestedScrollingEnabled = false

        //visible search stuff
        bottom_sheet_search_edt.visibility = View.VISIBLE
        bottom_sheet_search_clear.visibility = View.INVISIBLE

        bottom_sheet_search_edt.addTextChangedListener(onSearchViewTextChangeListener)
        forceKeyBoardToOpenForMoneyEditText(bottom_sheet_search_edt)
        //invisible search stuff
        main_bottom_sheet_search_btn.visibility = View.GONE
        bottom_sheet_title.visibility = View.GONE
        //this should come after all
        searchViewState = SearchViewState.VISIBLE

        query?.let {
            bottom_sheet_search_edt.setText(it)
        }
    }

    private fun disableSearchMode() {

        uiCommunicationListener.hideSoftKeyboard()
        //invisible search stuff
        bottom_sheet_search_edt.visibility = View.GONE
        bottom_sheet_search_clear.visibility = View.GONE

        bottom_sheet_search_edt.removeTextChangedListener(onSearchViewTextChangeListener)
        bottom_sheet_search_edt.setText("")
        //visible search stuff
        main_bottom_sheet_search_btn.visibility = View.VISIBLE
        bottom_sheet_title.visibility = View.VISIBLE

        //this should come after all
        searchViewState = SearchViewState.INVISIBLE
        //clear search
        searchModel = searchModel.copy(query = "")

        //make bottom sheet draggable again after disabling searchView
        bottomSheetBehavior.isDraggable = true
        //connect recyclerView to bottomSheet
        transaction_recyclerView.isNestedScrollingEnabled = true

        //submit that
        lifecycleScope.launch {
//                viewModel.queryChannel.value = SearchModel(query = p0.toString())
            viewModel.queryChannel.emit(searchModel)
            //add new search query
            Log.d(TAG, "searchDEBUG: clear")
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
        val categoryId = Random.nextInt(1,42)
        var money = Random.nextInt(0, 1000).toDouble()
        //it just hardcoded TODO CHANGE IT LATER
        //we mark money with "-" base of category id
        if (categoryId <= 30) {
            money *= -1
        }
        if (Random.nextBoolean()) {
            viewModel.launchNewJob(
                TransactionStateEvent.OneShotOperationsTransactionStateEvent.InsertTransaction(
                    Record(
                        id = 0,
                        money = money,
                        memo = null,
                        cat_id = categoryId,
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
                        money = money,
                        memo = "memo ${Random.nextInt(451252)}",
                        cat_id = categoryId,
                        date = Random.nextInt(
                            System.currentTimeMillis().minus(604_800L).toInt(),
                            System.currentTimeMillis().toInt()
                        )
                    )
                )
            )
        }
    }


    private fun navigateToAddTransactionFragment(isNewTransaction: Boolean) {
        //on category selected and bottomSheet hided
        val action =
            TransactionFragmentDirections.actionTransactionFragmentToCreateTransactionFragment(
                isNewTransaction = isNewTransaction
            )
        findNavController().navigate(action)
    }

    private fun subscribeObservers() {
        txt_balance.text = separate3By3AndRoundIt(0.0, currentLocale)
        txt_expenses.text = separate3By3AndRoundIt(0.0, currentLocale)
        txt_income.text = separate3By3AndRoundIt(0.0, currentLocale)
        viewModel.viewState.observe(viewLifecycleOwner) { viewState ->
            viewState?.let {
                Log.d(TAG, "submitList: called transacionLIst: ${it.transactionList}")
                it.transactionList?.let { transactionList ->
                    recyclerAdapter.submitList(transactionList, true)
                }
                it.summeryMoney?.let { summeryMoney ->
                    summeryMoney.balance = (summeryMoney.income.plus(summeryMoney.expenses))
                    txt_balance.text = separate3By3AndRoundIt(summeryMoney.balance, currentLocale)
                    txt_expenses.text =
                        separate3By3AndRoundIt(summeryMoney.expenses.times(-1), currentLocale)
                    txt_income.text = separate3By3AndRoundIt(summeryMoney.income, currentLocale)
                }
            }
        }

    }


    private fun initRecyclerView() {

        transaction_recyclerView.apply {
            layoutManager = LinearLayoutManager(this@TransactionFragment.context)
            recyclerAdapter = object : TransactionListAdapter(
                requestManager,
                this@TransactionFragment,
                this@TransactionFragment.requireActivity().packageName,
                currentLocale, _resources
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

                override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                    super.onScrollStateChanged(recyclerView, newState)
                    val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                    val lastPosition = layoutManager.findFirstCompletelyVisibleItemPosition()
                    if (lastPosition == 0) {
                        //user should be able to only drag the bottom sheet down when it's in first
                        //position
                        if (searchViewState.isInvisible()) {
                            //while searching you should not be able to drag down
                            bottomSheetBehavior.isDraggable = true
                        }
                    } else {
                        bottomSheetBehavior.isDraggable = false
                    }
                }
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


    override fun onResume() {
        super.onResume()
        uiCommunicationListener.changeDrawerState(false)
        //enable backStack listener when user  navigate to next fragment or rotate screen
        if (bottomSheetBehavior.state == STATE_EXPANDED || searchViewState.isVisible()) {
            backStackForBottomSheet.isEnabled = true
        }
        //set bottom sheet peek height
        if (bottomSheetBehavior.state == BottomSheetBehavior.STATE_EXPANDED)
            resetIt(1f)

        fragment_transacion_root.viewTreeObserver.addOnGlobalLayoutListener(object :
            ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                //TODO BIG BUG HERE
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN) {
                    fragment_transacion_root.viewTreeObserver.removeOnGlobalLayoutListener(this)
                }
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

    override fun onStop() {
        super.onStop()
        uiCommunicationListener.changeDrawerState(true)
    }

    override fun onItemSelected(position: Int, item: Record) {
        viewModel.setDetailTransFields(item)
        navigateToAddTransactionFragment(false)
    }

    override fun restoreListPosition() {
    }

    private fun onBottomSheetStateChanged(newState: Int) {
        Log.d(TAG, "onBottomSheetStateChanged: state changed to $newState ")

        if (STATE_EXPANDED == newState) {
            last_transacion_app_bar.isLiftOnScroll = false
//            last_transacion_app_bar.liftOnScrollTargetViewId = R.id.transaction_recyclerView
            last_transacion_app_bar.setLiftable(false)
            //enable backStack
            backStackForBottomSheet.isEnabled = true
        } else {
            last_transacion_app_bar.isLiftOnScroll = true
            last_transacion_app_bar.setLiftable(true)
            //disable backStack
            backStackForBottomSheet.isEnabled = false
        }

        if (STATE_DRAGGING == newState) {
            fab.hide()
        } else {
            fab.show()
        }

    }

    private fun onBottomSheetSlide(slideOffset: Float) {
        main_bottom_sheet_back_arrow.alpha = slideOffset

        val bottomSheetBackGround = main_standardBottomSheet.background as GradientDrawable

        val topHeight = (bottomSheetRadios * (1f - slideOffset))

        //change bottom sheet raidus
        bottomSheetBackGround.cornerRadius = topHeight
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN) {
            main_standardBottomSheet.background = bottomSheetBackGround
        } else {
            main_standardBottomSheet.setBackgroundColor(resources.getColor(R.color.white))
        }
        //change app bar

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN) {
            last_transacion_app_bar.background = bottomSheetBackGround
        } else {
            last_transacion_app_bar.setBackgroundColor(resources.getColor(R.color.white))
        }
//            last_transacion_app_bar.setPadding(topHeight.toInt(), 0, topHeight.toInt(), 0)
        //change app bar height
        val appbarViewParams = last_transacion_app_bar.layoutParams
        appbarViewParams.height = (normalAppBarHeight + (bottomSheetRadios - topHeight.toInt()))
        last_transacion_app_bar.layoutParams = appbarViewParams
        //change buttons height
        val buttonsViewParams = main_bottom_sheet_search_btn.layoutParams
        buttonsViewParams.width = (normalAppBarHeight + (bottomSheetRadios - topHeight.toInt()))

        main_bottom_sheet_search_btn.layoutParams = buttonsViewParams
//            main_standardBottomSheet.setPadding(0, topHeight.toInt(), 0, 0)
        //change top of bottomSheet height
        val viewParams = view_hastam.layoutParams
        viewParams.height = topHeight.toInt()
        view_hastam.layoutParams = viewParams
        view_hastam2.alpha = (1f - slideOffset)

        // make the toolbar close button animation
        val closeButtonParams =
            main_bottom_sheet_back_arrow.layoutParams as ViewGroup.LayoutParams
        closeButtonParams.width = (slideOffset * closeBottomWidth).toInt()
        main_bottom_sheet_back_arrow.layoutParams = closeButtonParams
    }

    private fun resetIt(slideOffset: Float) {

        if (slideOffset == 1f) {//if its full screen then set it to liftable
            last_transacion_app_bar.isLiftOnScroll = false
            last_transacion_app_bar.setLiftable(false)
        }

        main_bottom_sheet_back_arrow.alpha = slideOffset
        val bottomSheetBackGround = main_standardBottomSheet.background as GradientDrawable

        val topHeight = (bottomSheetRadios * (1f - slideOffset))

        //change bottom sheet raidus
        bottomSheetBackGround.cornerRadius = topHeight
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN) {
            main_standardBottomSheet.background = bottomSheetBackGround
        } else {
            main_standardBottomSheet.setBackgroundColor(resources.getColor(R.color.white))
        }
        //change app bar
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN) {
            last_transacion_app_bar.background = bottomSheetBackGround
        } else {
            last_transacion_app_bar.setBackgroundColor(resources.getColor(R.color.white))
        }
//            last_transacion_app_bar.setPadding(topHeight.toInt(), 0, topHeight.toInt(), 0)
        //change app bar height
        val appbarViewParams = last_transacion_app_bar.layoutParams
        appbarViewParams.height = (normalAppBarHeight + (bottomSheetRadios - topHeight.toInt()))
        last_transacion_app_bar.layoutParams = appbarViewParams
        //change buttons height
        val buttonsViewParams = main_bottom_sheet_search_btn.layoutParams
        buttonsViewParams.width = (normalAppBarHeight + (bottomSheetRadios - topHeight.toInt()))

        main_bottom_sheet_search_btn.layoutParams = buttonsViewParams
//            main_standardBottomSheet.setPadding(0, topHeight.toInt(), 0, 0)
        //change top of bottomSheet height
        val viewParams = view_hastam.layoutParams
        viewParams.height = topHeight.toInt()
        view_hastam.layoutParams = viewParams
        view_hastam2.alpha = (1f - slideOffset)

        // make the toolbar close button animation
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

    override fun onSaveInstanceState(outState: Bundle) {

        //save searchResult and search state for screen rotation
        val searchQuery =
            if (searchViewState.isVisible()) (bottom_sheet_search_edt.text.toString())
            else null

        outState.putString(SEARCH_QUERY, searchQuery)

        super.onSaveInstanceState(outState)
    }

    private fun checkForGuidePromote() {
        if (sharedPreferences.getBoolean(PROMOTE_FAB_TRANSACTION_FRAGMENT, true)) {
            trySafe { showFabPromote() }
        }

    }

    fun showFabPromote() {
        val mFabPrompt = MaterialTapTargetPrompt.Builder(this)
            .setTarget(R.id.fab)
            .setPrimaryText(_getString(R.string.fab_tap_target_primary))
            .setSecondaryText(_getString(R.string.fab_tap_target_secondary))
            .setPromptStateChangeListener { _, state ->
                if (state == MaterialTapTargetPrompt.STATE_FOCAL_PRESSED || state == MaterialTapTargetPrompt.STATE_DISMISSING) {
                    sharedPrefsEditor.putBoolean(
                        PROMOTE_FAB_TRANSACTION_FRAGMENT,
                        false
                    ).apply()
                }
            }
            .create()
        mFabPrompt!!.show()
    }


    override fun setTextToAllViews() {
        txt_balance_viewHolder.text = _getString(R.string.total_balance)
        txt_income_viewHolder.text = _getString(R.string.income)
        txt_expenses_viewHolder.text = _getString(R.string.expenses)
        bottom_sheet_title.text = _getString(R.string.transactions)
        bottom_sheet_title.text = _getString(R.string.transactions)
        bottom_sheet_search_edt.hint = _getString(R.string.search)
    }

    companion object {
        private const val SEARCH_QUERY = "SEARCHVIEWWSTATE VISIBLE >>>>"

    }

}