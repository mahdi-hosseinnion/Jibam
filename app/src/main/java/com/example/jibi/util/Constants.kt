package com.example.jibi.util

object Constants {


    const val BASE_URL = "https://open-api.xyz/api/"
    const val PASSWORD_RESET_URL: String = "https://open-api.xyz/password_reset/"


    const val NETWORK_TIMEOUT = 6000L
    const val CACHE_TIMEOUT = 2500L
    const val SEARCH_DEBOUNCE = 300L
    const val TESTING_NETWORK_DELAY = 0L // fake network delay for testing
    const val TESTING_CACHE_DELAY = 0L // fake cache delay for testing


    const val PAGINATION_PAGE_SIZE = 10

    const val GALLERY_REQUEST_CODE = 201
    const val PERMISSIONS_REQUEST_READ_STORAGE: Int = 301
    const val CROP_IMAGE_INTENT_CODE: Int = 401

    const val PERSIAN_LANG_CODE = "fa"
    const val ENGLISH_LANG_CODE = "en"
    const val SYSTEM_DEFAULT_LANG_CODE = "SYSTEM_DEFAULT_LANG_CODE"
    const val APP_DEFAULT_LANGUAGE = SYSTEM_DEFAULT_LANG_CODE

    const val EXPENSES_TYPE_MARKER = 1
    const val INCOME_TYPE_MARKER = 2
}