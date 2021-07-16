package com.example.jibi.ui.main.transaction.common

import android.content.res.Resources
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.jibi.R
import com.example.jibi.util.DateUtils
import com.example.jibi.util.SolarCalendar
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.android.synthetic.main.bottom_sheet_month_picker.*

class MonthPickerBottomSheet
constructor(
    private val interaction: Interaction,
    private val isShamsi: Boolean,
    private val _resources: Resources,
    private val defaultMonth: Int,
    private val defaultYear: Int,
) : BottomSheetDialogFragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.bottom_sheet_month_picker, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
    }

    private fun initViews() {
        monthNumberPicker.minValue = 1
        monthNumberPicker.maxValue = 12
        if (isShamsi) {
            yearNumberPicker.minValue = SolarCalendar.minShamsiYear
            yearNumberPicker.maxValue = SolarCalendar.maxShamsiYear
            monthNumberPicker.displayedValues = DateUtils.shamsiMonths
        } else {
            yearNumberPicker.minValue = SolarCalendar.minGregorianYear
            yearNumberPicker.maxValue = SolarCalendar.maxGregorianYear
            monthNumberPicker.setDisplayedValues(DateUtils.gregorianMonths)
        }
        monthNumberPicker.value = defaultMonth
        yearNumberPicker.value = defaultYear
        confirm_monthPicker.text = _resources.getString(R.string.confirm)
        confirm_monthPicker.setOnClickListener {
            if (yearNumberPicker.value != defaultYear
                ||
                monthNumberPicker.value != defaultMonth
            ) {
                interaction.onNewMonthSelected(monthNumberPicker.value, yearNumberPicker.value)
            }
            dismiss()
        }

    }

    interface Interaction {
        fun onNewMonthSelected(month: Int, year: Int)
    }
}