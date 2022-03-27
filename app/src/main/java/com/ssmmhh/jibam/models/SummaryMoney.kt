package com.ssmmhh.jibam.models

import java.math.BigDecimal

data class SummaryMoney(
    val income: BigDecimal,
    val expenses: BigDecimal,
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