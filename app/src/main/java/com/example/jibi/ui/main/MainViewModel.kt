package com.example.jibi.ui.main

import com.example.jibi.di.main.MainScope
import com.example.jibi.repository.main.MainRepository
import com.example.jibi.ui.BaseViewModel
import com.example.jibi.ui.main.transaction.state.TransactionStateEvent
import com.example.jibi.ui.main.transaction.state.TransactionViewState
import com.example.jibi.util.DataState
import com.example.jibi.util.StateEvent
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@MainScope
class MainViewModel
@Inject
constructor(
    private val mainRepository: MainRepository
) : BaseViewModel<TransactionViewState>() {
    override suspend fun getResultByStateEvent(stateEvent: StateEvent): DataState<TransactionViewState> {
        TODO("Not yet implemented")
    }

    override fun initNewViewState(): TransactionViewState {
        TODO("Not yet implemented")
    }

    override fun handleNewData(viewState: TransactionViewState) {
        TODO("Not yet implemented")
    }
}
