package com.ssmmhh.jibam.ui.main.transaction.common

import android.content.res.Resources
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.ssmmhh.jibam.R
import com.ssmmhh.jibam.databinding.BottomSheetMonthPickerBinding
import com.ssmmhh.jibam.util.DateUtils
import com.ssmmhh.jibam.util.SolarCalendar

class MonthPickerBottomSheet
constructor(
    private val interaction: Interaction,
    private val isShamsi: Boolean,
    private val _resources: Resources,
    private val defaultMonth: Int,
    private val defaultYear: Int,
    private val isDefaultMonthTheCurrentMonth: Boolean
) : BottomSheetDialogFragment() {

    private var _binding: BottomSheetMonthPickerBinding? = null

    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = BottomSheetMonthPickerBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
    }

    private fun initViews() {

        if (isDefaultMonthTheCurrentMonth) {
            binding.backToCurrentMonthTxt.visibility = View.GONE
        } else {
            binding.backToCurrentMonthTxt.visibility = View.VISIBLE
        }

        binding.monthNumberPicker.minValue = 1
        binding.monthNumberPicker.maxValue = 12
        if (isShamsi) {
            binding.yearNumberPicker.minValue = SolarCalendar.minShamsiYear
            binding.yearNumberPicker.maxValue = SolarCalendar.maxShamsiYear
            binding.monthNumberPicker.displayedValues =
                DateUtils.shamsiMonths.map { strId -> getString(strId) }.toTypedArray()
        } else {
            binding.yearNumberPicker.minValue = SolarCalendar.minGregorianYear
            binding.yearNumberPicker.maxValue = SolarCalendar.maxGregorianYear
            binding.monthNumberPicker.displayedValues =
                DateUtils.gregorianMonths.map { strId -> getString(strId) }.toTypedArray()
        }
        binding.monthNumberPicker.value = defaultMonth
        binding.yearNumberPicker.value = defaultYear
        binding.confirmMonthPicker.text = _resources.getString(R.string.confirm)
        binding.backToCurrentMonthTxt.text =
            _resources.getString(R.string.back_to_current_month)
        binding.confirmMonthPicker.setOnClickListener {
            if (binding.yearNumberPicker.value != defaultYear
                ||
                binding.monthNumberPicker.value != defaultMonth
            ) {
                interaction.onNewMonthSelected(
                    binding.monthNumberPicker.value,
                    binding.yearNumberPicker.value
                )
            }
            dismiss()
        }
        //on clicks
        binding.backToCurrentMonthTxt.setOnClickListener {
            interaction.onNavigateToCurrentMonthClicked()
            dismiss()
        }

    }

    interface Interaction {
        fun onNewMonthSelected(month: Int, year: Int)
        fun onNavigateToCurrentMonthClicked()
    }
}