package com.ssmmhh.jibam

import com.ssmmhh.jibam.models.Category
import com.ssmmhh.jibam.models.TransactionEntity

object TestUtil{
    val CATEGORY1=Category(1,1,"gameNet","R.drawable.gameNet",1)
    val CATEGORY2=Category(2,2,"poolToJibi","R.drawable.poolToJibi",2)
    val RECORD1=TransactionEntity(1,923.0,"asd;flk",7,2147483647)
    val RECORD2=TransactionEntity(2,-4546.0,"asd;flask",8,214753647)
    val CATEGORY_NAME2="i am the name 2"
}