package com.ssmmhh.jibam.data.source.local.typeconverter

import androidx.room.TypeConverter
import java.math.BigDecimal

class BigDecimalTypeConverter {

    @TypeConverter
    fun bigDecimalToString(input: BigDecimal?): String = input?.toPlainString() ?: ""


    @TypeConverter
    fun stringToBigDecimal(input: String?): BigDecimal =
        if (input.isNullOrBlank())
            BigDecimal.ZERO
        else
            input.toBigDecimalOrNull() ?: BigDecimal.ZERO


}