package com.example.jibi.models.mappers

import com.example.jibi.models.Transaction
import com.example.jibi.models.TransactionEntity


fun Transaction.toTransactionEntity(): TransactionEntity = TransactionEntity(
    id = this.id,
    money = this.money,
    memo = this.memo,
    cat_id = this.categoryId,
    date = this.date
)
