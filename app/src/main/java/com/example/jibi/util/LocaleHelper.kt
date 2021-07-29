package com.example.jibi.util

import android.annotation.TargetApi
import android.content.Context
import android.content.SharedPreferences
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Build
import com.example.jibi.util.PreferenceKeys.APP_LANGUAGE_PREF
import java.util.*


object LocaleHelper {
    private const val defaultLanguage = Constants.APP_DEFAULT_LANGUAGE

    fun onAttach(context: Context?): Context? {
        if (context == null) return null
        val lang = getPersistedData(context)
        if (lang == Constants.SYSTEM_DEFAULT_LANG_CODE) return context
        return setLocale(context, lang)
    }

    fun onAttachToApplication(context: Context?): Context? {
        if (context == null) return null
        val lang = getPersistedData(context)
        if (lang == Constants.SYSTEM_DEFAULT_LANG_CODE) return context
        return setLocale(context, lang)
    }

    fun getLanguage(context: Context): String? {
        return getPersistedData(context)
    }

    fun setLocale(context: Context, language: String?): Context? {
        persist(context, language)
        if (language == Constants.SYSTEM_DEFAULT_LANG_CODE) return context
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            updateResources(context, language)
        } else updateResourcesLegacy(context, language)
    }

    fun getLocale(context: Context): Context? {
        val language = getLanguage(context)
        if (language == Constants.SYSTEM_DEFAULT_LANG_CODE) return context
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            updateResources(context, language)
        } else updateResourcesLegacy(context, language)
    }

    private fun getPersistedData(context: Context): String? {
        val preferences = getPreferences(context)
        return preferences.getString(APP_LANGUAGE_PREF, defaultLanguage)
    }

    private fun getPreferences(context: Context): SharedPreferences = context.getSharedPreferences(
        PreferenceKeys.APP_MAIN_PREFERENCES,
        Context.MODE_PRIVATE
    )

    private fun persist(context: Context, language: String?) {
        val preferences = getPreferences(context)
        val editor = preferences.edit()
        editor.putString(APP_LANGUAGE_PREF, language)
        editor.commit()
    }

    @TargetApi(Build.VERSION_CODES.N)
    private fun updateResources(context: Context, language: String?): Context? {
        val locale = Locale(language)
        Locale.setDefault(locale)
        val configuration: Configuration = context.resources.configuration
        configuration.setLocale(locale)
        configuration.setLayoutDirection(locale)
        return context.createConfigurationContext(configuration)
    }

    private fun updateResourcesLegacy(context: Context, language: String?): Context? {
        val locale = Locale(language)
        Locale.setDefault(locale)
        val resources: Resources = context.resources
        val configuration: Configuration = resources.configuration
        configuration.locale = locale
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            configuration.setLayoutDirection(locale)
        }
        resources.updateConfiguration(configuration, resources.displayMetrics)
        return context
    }
}