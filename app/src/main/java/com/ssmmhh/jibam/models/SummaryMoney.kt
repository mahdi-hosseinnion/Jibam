package com.ssmmhh.jibam.models

import java.math.BigDecimal

data class SummaryMoney(
    val income: BigDecimal = BigDecimal.ZERO,
    val expenses: BigDecimal = BigDecimal.ZERO,
) {
    val balance get() = this.expenses + this.income

    fun inNotNull(): Boolean {
        return (balance != BigDecimal.ZERO
                ||
                income != BigDecimal.ZERO
                ||
                expenses != BigDecimal.ZERO)
    }
}