package com.example.jibi.ui.main.transaction

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.jibi.R
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class CreateNewTransBottomSheet : BottomSheetDialogFragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.bottom_sheet_create_new_trans, container, false)
    companion object {
        const val TAG = "ModalBottomSheet"
    }
}