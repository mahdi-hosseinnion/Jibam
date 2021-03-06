package com.ssmmhh.jibam.presentation.common

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.ssmmhh.jibam.R
import com.ssmmhh.jibam.databinding.BottomSheetMonthPickerBinding
import com.ssmmhh.jibam.util.*
import com.ssmmhh.jibam.util.DateUtils.maxGregorianYear
import com.ssmmhh.jibam.util.DateUtils.maxSolarHijriYear
import com.ssmmhh.jibam.util.DateUtils.minGregorianYear
import com.ssmmhh.jibam.util.DateUtils.minSolarHijriYear

class MonthPickerBottomSheet
constructor(
    private val interaction: Interaction,
    private val isShamsi: Boolean,
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
            binding.yearNumberPicker.minValue = minSolarHijriYear
            binding.yearNumberPicker.maxValue = maxSolarHijriYear
            binding.monthNumberPicker.displayedValues =
                DateUtils.shamsiMonths.map { strId -> getString(strId) }.toTypedArray()
        } else {
            binding.yearNumberPicker.minValue = minGregorianYear
            binding.yearNumberPicker.maxValue = maxGregorianYear
            binding.monthNumberPicker.displayedValues =
                DateUtils.gregorianMonths.map { strId -> getString(strId) }.toTypedArray()
        }
        binding.monthNumberPicker.value = defaultMonth
        binding.yearNumberPicker.value = defaultYear
        binding.confirmMonthPicker.text = resources.getString(R.string.confirm)
        binding.backToCurrentMonthTxt.text =
            resources.getString(R.string.back_to_current_month)
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