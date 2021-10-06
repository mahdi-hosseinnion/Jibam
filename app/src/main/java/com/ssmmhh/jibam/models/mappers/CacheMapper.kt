package com.ssmmhh.jibam.models.mappers

import com.ssmmhh.jibam.models.Transaction
import com.ssmmhh.jibam.models.TransactionEntity


fun Transaction.toTransactionEntity(): TransactionEntity = TransactionEntity(
    id = this.id,
    money = this.money,
    memo = this.memo,
    cat_id = this.categoryId,
    date = this.date
)
