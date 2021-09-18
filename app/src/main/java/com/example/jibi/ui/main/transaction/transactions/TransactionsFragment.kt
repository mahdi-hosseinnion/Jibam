package com.example.jibi.ui.main.transaction.transactions

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
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.RequestManager
import com.example.jibi.R
import com.example.jibi.di.main.MainScope
import com.example.jibi.models.Month
import com.example.jibi.models.Transaction
import com.example.jibi.models.TransactionEntity
import com.example.jibi.models.mappers.toTransactionEntity
import com.example.jibi.repository.buildResponse
import com.example.jibi.ui.main.transaction.common.BaseFragment
import com.example.jibi.ui.main.transaction.transactions.state.TransactionsStateEvent
import com.example.jibi.ui.main.transaction.transactions.state.TransactionsViewState
import com.example.jibi.util.*
import com.example.jibi.util.PreferenceKeys.PROMOTE_FAB_TRANSACTION_FRAGMENT
import com.example.jibi.util.PreferenceKeys.PROMOTE_MONTH_MANGER
import com.example.jibi.util.PreferenceKeys.PROMOTE_SUMMERY_MONEY
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetBehavior.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_add_transaction.*
import kotlinx.android.synthetic.main.fragment_transaction.*
import kotlinx.android.synthetic.main.layout_chart_list_item.*
import kotlinx.android.synthetic.main.layout_transaction_list_item.*
import kotlinx.android.synthetic.main.toolbar_month_changer.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import uk.co.samuelwall.materialtaptargetprompt.MaterialTapTargetPrompt
import uk.co.samuelwall.materialtaptargetprompt.extras.backgrounds.RectanglePromptBackground
import uk.co.samuelwall.materialtaptargetprompt.extras.focals.RectanglePromptFocal
import java.util.*
import javax.inject.Inject
import kotlin.random.Random

@FlowPreview
@ExperimentalCoroutinesApi
@MainScope
class TransactionsFragment
@Inject
constructor(
    viewModelFactory: ViewModelProvider.Factory,
    private val requestManager: RequestManager,
    private val currentLocale: Locale,
    private val sharedPreferences: SharedPreferences,
    private val sharedPrefsEditor: SharedPreferences.Editor
) : BaseFragment(
    R.layout.fragment_transaction,
    R.id.transaction_toolbar
), TransactionsListAdapter.Interaction {

    private val TAG = "TransactionFragment"

    private val viewModel by viewModels<TransactionsViewModel> { viewModelFactory }


    private lateinit var recyclerAdapter: TransactionsListAdapter

    private lateinit var bottomSheetBehavior: BottomSheetBehavior<LinearLayout>

    private var bottomSheetPeekHeight = 0

    private val closeBottomWidth by lazy { convertDpToPx(56) }
    private val bottomSheetRadios by lazy { convertDpToPx(16) }
    private val normalAppBarHeight by lazy { convertDpToPx(40) }


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
            if (viewModel.isSearchVisible()) {
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

        if (viewModel.isSearchVisible()) {
            enableSearchMode()
        }

        fab.setOnClickListener {
            navigateToAddTransactionFragment()
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
        //TODO DELETE THIS LINE FOR FINAL PROJECT JUST OF TESTING
        txt_income.setOnClickListener {
//            showSummeryMoneyPromote()
        }
        main_bottom_sheet_back_arrow.setOnClickListener {
            //check for search view
            if (viewModel.isSearchVisible()) {
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
        toolbar_month.setOnClickListener {
            viewModel.showMonthPickerBottomSheet(parentFragmentManager)
        }
        month_manager_previous.setOnClickListener {
            viewModel.navigateToPreviousMonth()
        }
        month_manager_next.setOnClickListener {
            viewModel.navigateToNextMonth()
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
        sharedPrefsEditor.putBoolean(
            PreferenceKeys.APP_INTRO_PREFERENCE,
            true
        ).apply()
        sharedPrefsEditor.putBoolean(
            PreferenceKeys.PROMOTE_SUMMERY_MONEY,
            true
        ).apply()
        sharedPrefsEditor.putBoolean(
            PreferenceKeys.PROMOTE_MONTH_MANGER,
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

        bottom_sheet_search_clear.visibility = View.INVISIBLE

        bottom_sheet_search_edt.addTextChangedListener(onSearchViewTextChangeListener)
        //invisible search stuff
        main_bottom_sheet_search_btn.visibility = View.GONE
        bottom_sheet_title.visibility = View.GONE
        //this should come after all
        viewModel.setSearchViewState(TransactionsViewState.SearchViewState.VISIBLE)

        query?.let {
            bottom_sheet_search_edt.setText(it)
        }
        if (bottomSheetBehavior.state == STATE_EXPANDED) {
            bottom_sheet_search_edt.visibility = View.VISIBLE
            forceKeyBoardToOpenForMoneyEditText(bottom_sheet_search_edt)
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
        viewModel.setSearchViewState(TransactionsViewState.SearchViewState.INVISIBLE)

        //make bottom sheet draggable again after disabling searchView
        bottomSheetBehavior.isDraggable = true
        //connect recyclerView to bottomSheet
        transaction_recyclerView.isNestedScrollingEnabled = true

        //submit that
        viewModel.setSearchQuery("")
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
            viewModel.setSearchQuery(p0.toString())
        }
    }

    //JUST FOR TEST
    var startOfMonth = System.currentTimeMillis().div(1_000).toInt()
    var endOfMonth = System.currentTimeMillis().div(1_000).toInt()

    private fun insertRandomTransaction() {
        val categoryId = Random.nextInt(1, 42)
        var money = Random.nextInt(0, 1000).toDouble()
        //it just hardcoded TODO CHANGE IT LATER
        //we mark money with "-" base of category id
        val dateRange = 2_592_000_000L//30 day to millisecond
        if (categoryId <= 30) {
            money *= -1
        }
        if (Random.nextBoolean()) {
            viewModel.launchNewJob(
                TransactionsStateEvent.InsertTransaction(
                    TransactionEntity(
                        id = 0,
                        money = money,
                        memo = null,
                        cat_id = categoryId,
                        date = Random.nextInt(
                            startOfMonth,
                            endOfMonth
                        )
                    )
                )
            )
        } else {
            viewModel.launchNewJob(
                TransactionsStateEvent.InsertTransaction(
                    TransactionEntity(
                        id = 0,
                        money = money,
                        memo = "memo ${Random.nextInt(451252)}",
                        cat_id = categoryId,
                        date = Random.nextInt(
                            ((System.currentTimeMillis()).minus(dateRange)).div(1_000).toInt(),
                            ((System.currentTimeMillis())).div(1_000).toInt()
                        )
                    )
                )
            )
        }
    }


    private fun navigateToAddTransactionFragment() {
        //on category selected and bottomSheet hided
        val action =
            TransactionsFragmentDirections.actionTransactionFragmentToCreateTransactionFragment()
        findNavController().navigate(action)
    }

    override fun handleLoading() {
        viewModel.countOfActiveJobs.observe(
            viewLifecycleOwner
        ) {
            Log.d(TAG, "handleLoading: activeJob: ${viewModel.getAllActiveJobs()}")

            showProgressBar(viewModel.areAnyJobsActive())
        }
    }

    override fun handleStateMessages() {
        viewModel.stateMessage.observe(viewLifecycleOwner) {
            it?.let {
                handleNewStateMessage(it) { viewModel.clearStateMessage() }
            }
        }
    }

    private fun subscribeObservers() {

        viewModel.viewState.observe(viewLifecycleOwner) { vs ->
            vs?.let { viewState ->
                viewState.currentMonth?.let { setMonthFieldsValues(it) }
            }
        }
        viewModel.transactions.observe(viewLifecycleOwner) { transactionList ->
            recyclerAdapter.submitList(transactionList, true)
        }
        viewModel.summeryMoney.observe(viewLifecycleOwner) { sm ->
            if (sm.inNotNull()) {
                checkForSummeryMoneyPromote()
            }
            sm.balance = (sm.income.plus(sm.expenses))
            txt_balance.text = separate3By3AndRoundIt(sm.balance, currentLocale)
            if (sm.expenses != 0.0) {
                txt_expenses.text =
                    separate3By3AndRoundIt(sm.expenses.times(-1), currentLocale)
            } else {
                txt_expenses.text = separate3By3AndRoundIt(0.0, currentLocale)
            }
            txt_income.text = separate3By3AndRoundIt(sm.income, currentLocale)
        }

    }

    private fun setMonthFieldsValues(month: Month) {
        toolbar_month.text = month.nameOfMonth
        startOfMonth = month.startOfMonth
        endOfMonth = month.endOfMonth
    }


    private fun initRecyclerView() {

        transaction_recyclerView.apply {
            layoutManager = LinearLayoutManager(this@TransactionsFragment.context)
            recyclerAdapter = TransactionsListAdapter(
                requestManager,
                this@TransactionsFragment,
                this@TransactionsFragment.requireActivity().packageName,
                currentLocale
            )
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
                        if (viewModel.isSearchInVisible()) {
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
                object : SwipeToDeleteCallback(this@TransactionsFragment.requireContext()) {
                    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {

                        val adapter = transaction_recyclerView.adapter as TransactionsListAdapter
                        val deletedTrans = adapter.getTransaction(viewHolder.adapterPosition)
//                        delete from list
                        val removedHeader = adapter.removeAt(viewHolder.adapterPosition)
                        //add to recently deleted
                        val recentlyDeletedHeader =
                            TransactionsViewState.RecentlyDeletedTransaction(
                                deletedTrans,
                                viewHolder.adapterPosition,
                                removedHeader
                            )
                        viewModel.setRecentlyDeletedTrans(
                            recentlyDeletedHeader
                        )
                        //delete from database
                        viewModel.launchNewJob(
                            TransactionsStateEvent.DeleteTransaction(
                                transactionEntity = deletedTrans.toTransactionEntity(),
                                showSuccessToast = false
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
                viewModel.setRecentlyDeletedTrans(null)
            }
        }
        uiCommunicationListener.onResponseReceived(
            buildResponse(
                getString(R.string.transaction_successfully_deleted),
                UIComponentType.UndoSnackBar(undoCallback, fragment_transacion_root),
                MessageType.Info
            ), object : StateMessageCallback {
                override fun removeMessageFromStack() {
                }
            }
        )
    }

    private fun insertRecentlyDeletedTrans() {
        val recentlyDeletedFields = viewModel.getCurrentViewStateOrNew().recentlyDeletedFields
        recentlyDeletedFields?.recentlyDeletedTrans?.let {
            //insert to list
            recyclerAdapter.insertTransactionAt(
                it,
                recentlyDeletedFields.recentlyDeletedTransPosition,
                recentlyDeletedFields.recentlyDeletedHeader
            )
            //insert into database
            viewModel.launchNewJob(
                TransactionsStateEvent.InsertTransaction(
                    it.toTransactionEntity()
                )
            )
        } ?: viewModel.addToMessageStack(
            message = "Something went wrong \n cannot return deleted transaction back",
            uiComponentType = UIComponentType.Dialog,
            messageType = MessageType.Error
        )
        viewModel.setRecentlyDeletedTrans(null)

    }


    override fun onResume() {
        super.onResume()
        uiCommunicationListener.changeDrawerState(false)
        //enable backStack listener when user  navigate to next fragment or rotate screen
        if (bottomSheetBehavior.state == STATE_EXPANDED || viewModel.isSearchVisible()) {
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

    }

    override fun onStop() {
        super.onStop()
        uiCommunicationListener.changeDrawerState(true)
    }

    override fun onItemSelected(position: Int, item: Transaction) {
        navigateToDetailTransactionFragment(item.id)
    }

    private fun navigateToDetailTransactionFragment(id: Int) {
        val action =
            TransactionsFragmentDirections.actionTransactionFragmentToDetailEditTransactionFragment(
                transactionId = id
            )
        findNavController().navigate(action)
    }

    override fun restoreListPosition() {
    }

    private fun onBottomSheetStateChanged(newState: Int) {
        Log.d(TAG, "onBottomSheetStateChanged: state changed to $newState ")

        if (STATE_EXPANDED == newState) {
            if (viewModel.isSearchVisible()) {
                // If theres bug with search bar this line is dangerous and bugkhiz
                //we should use this here b/c when user click on search and bottom sheet is in collapse state
                // edit text will crash the appbar
                bottom_sheet_search_edt.visibility = View.VISIBLE
                forceKeyBoardToOpenForMoneyEditText(bottom_sheet_search_edt)
            }
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
    //TODO SAVE SEARCH VIEW
/*    override fun onSaveInstanceState(outState: Bundle) {

        //save searchResult and search state for screen rotation
        val searchQuery =
            if (searchViewState.isVisible()) (bottom_sheet_search_edt.text.toString())
            else null

        outState.putString(SEARCH_QUERY, searchQuery)

        super.onSaveInstanceState(outState)
    }*/

    private fun checkForGuidePromote() {
        if (sharedPreferences.getBoolean(PROMOTE_FAB_TRANSACTION_FRAGMENT, true)) {
            trySafe { showFabPromote() }
        }
    }

    private fun checkForSummeryMoneyPromote() {
        if (!(sharedPreferences.getBoolean(PROMOTE_FAB_TRANSACTION_FRAGMENT, true))) {

            if (sharedPreferences.getBoolean(PROMOTE_SUMMERY_MONEY, true)) {
                trySafe { showSummeryMoneyPromote() }

            } else {

                if (sharedPreferences.getBoolean(PROMOTE_MONTH_MANGER, true)) {
                    trySafe { showMonthMangerPromote() }
                }
            }
        }
    }


    private fun showFabPromote() {
        val mFabPrompt = MaterialTapTargetPrompt.Builder(this)
            .setTarget(R.id.fab)
            .setPrimaryText(getString(R.string.fab_tap_target_primary))
            .setSecondaryText(getString(R.string.fab_tap_target_secondary))
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

    private fun showSummeryMoneyPromote() {
        val mFabPrompt = MaterialTapTargetPrompt.Builder(this)
            .setTarget(R.id.summery_money_root)
            .setPrimaryText(getString(R.string.summery_money_tap_target_primary))
            .setPromptBackground(RectanglePromptBackground().setCornerRadius(10f, 10f))
            .setPromptFocal(RectanglePromptFocal())
            .setPromptStateChangeListener { _, state ->
                if (state == MaterialTapTargetPrompt.STATE_FOCAL_PRESSED || state == MaterialTapTargetPrompt.STATE_DISMISSING) {
                    sharedPrefsEditor.putBoolean(
                        PROMOTE_SUMMERY_MONEY,
                        false
                    ).apply()
                    //check for month promote
                    if (sharedPreferences.getBoolean(PROMOTE_MONTH_MANGER, true)) {
                        trySafe { showMonthMangerPromote() }
                    }
                }
            }
            .create()
        mFabPrompt!!.show()
    }

    private fun showMonthMangerPromote() {
        val mFabPrompt = MaterialTapTargetPrompt.Builder(this)
            .setTarget(R.id.toolbar_month)
            .setPrimaryText(getString(R.string.month_manager_tap_target_primary))
            .setPromptStateChangeListener { _, state ->
                if (state == MaterialTapTargetPrompt.STATE_FOCAL_PRESSED || state == MaterialTapTargetPrompt.STATE_DISMISSING) {
                    sharedPrefsEditor.putBoolean(
                        PROMOTE_MONTH_MANGER,
                        false
                    ).apply()
                }
            }
            .create()
        mFabPrompt!!.show()
    }

    companion object {
        private const val SEARCH_QUERY = "SEARCHVIEWWSTATE VISIBLE >>>>"

    }

}