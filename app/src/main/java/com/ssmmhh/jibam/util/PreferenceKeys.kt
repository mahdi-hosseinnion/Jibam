package com.ssmmhh.jibam.util

import java.util.*

object PreferenceKeys {
    // Shared Preference Files:
    const val APP_MAIN_PREFERENCES: String = "APP_MAIN_PREFERENCES"

    // Shared Preference Keys
    const val APP_CALENDAR_PREFERENCE = "APP_CALENDAR_PREFERENCE"
    const val CALENDAR_GREGORIAN = "gregorian"
    const val CALENDAR_SOLAR = "solar"

    //Promote stuff
    const val PROMOTE_FAB_TRANSACTION_FRAGMENT = "PREFERENCES_PROMOTE_FAB_TRANSACTION_FRAGMENT"
    const val PROMOTE_ADD_TRANSACTION = "PREFERENCES_PROMOTE_ADD_TRANSACTION"
    const val PROMOTE_CATEGORY_LIST = "PREFERENCES_PROMOTE_CATEGORY_LIST"
    const val PROMOTE_VIEW_CATEGORY_LIST = "PREFERENCES_PROMOTE_VIEW_CATEGORY_LIST"
    const val PROMOTE_ADD_CATEGORY_NAME = "PREFERENCES_PROMOTE_ADD_CATEGORY_NAME"
    const val PROMOTE_SUMMERY_MONEY = "PREFERENCES_PROMOTE_SUMMERY_MONEY"
    const val PROMOTE_MONTH_MANGER = "PREFERENCES_PROMOTE_MONTH_MANGER"
    const val APP_INTRO_PREFERENCE = "APP_INTRO_PREFERENCE"


    fun calendarDefault(currentLocale: Locale): String =
        if (currentLocale.isFarsi()) {
            CALENDAR_SOLAR
        } else {
            CALENDAR_GREGORIAN
        }

}