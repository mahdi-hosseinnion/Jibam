package com.ssmmhh.jibam.persistence

import androidx.room.TypeConverter
import java.math.BigDecimal

class BigDecimalTypeConverter {

    @TypeConverter
    fun bigDecimalToString(input: BigDecimal?): String = input?.toPlainString() ?: ""


    @TypeConverter
    fun stringToBigDecimal(input: String?): BigDecimal =
        if (input.isNullOrBlank())
            BigDecimal.valueOf(0.0)
        else
            input.toBigDecimalOrNull() ?: BigDecimal.valueOf(0.0)


}