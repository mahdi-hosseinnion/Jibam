package com.example.jibi.models

data class SummaryMoney(
    var balance: Double = 0.0,
    var income: Double = 0.0,
    var expenses: Double = 0.0
)
//extra
/*
@Entity(tableName = "summaryMoney")
data class SummaryMoney(
    //start date of -1 is for all
    @PrimaryKey(autoGenerate = false)
    @ColumnInfo(name = "startOfDay")
    val startOfDay: Int,
    @ColumnInfo(name = "balance")
    val balance: Int,
    @ColumnInfo(name = "income")
    val income: Int = 0,
    @ColumnInfo(name = "expenses")
    //category id +"_"+category type
    val expenses: Int = 0,

)
 */