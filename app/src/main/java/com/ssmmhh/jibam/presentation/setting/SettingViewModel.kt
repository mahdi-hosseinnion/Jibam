package com.ssmmhh.jibam.presentation.setting

import android.content.SharedPreferences
import android.util.Log
import androidx.databinding.BaseObservable
import androidx.databinding.Bindable
import androidx.databinding.Observable
import androidx.databinding.PropertyChangeRegistry
import androidx.databinding.library.baseAdapters.BR
import androidx.lifecycle.ViewModel
import com.ssmmhh.jibam.util.PreferenceKeys
import com.ssmmhh.jibam.util.isCalendarSolar
import java.util.*
import javax.inject.Inject

class SettingViewModel
@Inject
constructor(
    locale: Locale,
    sharedPreferences: SharedPreferences,
    private val sharedPrefEditor: SharedPreferences.Editor
) : ViewModel(), Observable {

    private val TAG = "SettingViewModel"

    //For two way binding
    private val callbacks: PropertyChangeRegistry by lazy { PropertyChangeRegistry() }

    private var isCalendarSolarHijri: Boolean = (sharedPreferences.isCalendarSolar(locale))

    //Necessary For two way binding (field isGregorianChecked)
    @Bindable
    fun getIsGregorianChecked(): Boolean {
        Log.d(TAG, "getIsGregorianChecked: called")
        return !isCalendarSolarHijri
    }

    fun setIsGregorianChecked(isNewCalendarGregorian: Boolean) {
        Log.d(TAG, "setIsGregorianChecked: called")
        //If boolean value is not the same as [isCalendarSolarHijri] so it does not need to change.
        // prevent infinite loop.
        if (isNewCalendarGregorian != isCalendarSolarHijri) return
        Log.d(TAG, "setIsGregorianChecked: changed")

        isCalendarSolarHijri = !(isNewCalendarGregorian)

        if (isNewCalendarGregorian) {
            //Save calendar type as gregorian in shared preferences.
            sharedPrefEditor.apply {
                putString(
                    PreferenceKeys.APP_CALENDAR_PREFERENCE,
                    PreferenceKeys.CALENDAR_GREGORIAN
                )
                apply()
            }
        }
        // It's radio group so if one of radioButtons isChecked is changed the other one should change too
        notifyChange()
    }

    //Necessary For two way binding (field isSolarHijriChecked)
    @Bindable
    fun getIsSolarHijriChecked(): Boolean  {
        Log.d(TAG, "getIsSolarHijriChecked: called")
        return isCalendarSolarHijri
    }

    fun setIsSolarHijriChecked(isNewCalendarSolar: Boolean) {
        Log.d(TAG, "setIsSolarHijriChecked: called")
        // if new calendar value is solarHijri and saved valued is solar hijri then do nothing.
        // prevent infinite loop.
        if (isNewCalendarSolar == isCalendarSolarHijri) return
        Log.d(TAG, "setIsSolarHijriChecked: change")

        isCalendarSolarHijri = isNewCalendarSolar

        if (isNewCalendarSolar) {
            //Save calendar type as solar hijri in shared preferences.
            sharedPrefEditor.apply {
                putString(
                    PreferenceKeys.APP_CALENDAR_PREFERENCE,
                    PreferenceKeys.CALENDAR_SOLAR
                )
                apply()
            }
        }
        // It's radio group so if one of radioButtons isChecked is changed the other ones value should change too.
        notifyChange()

    }


    /**
     * Notifies listeners that all properties of this instance have changed.
     * It's used for two way binding.
     */
    private fun notifyChange() {
        callbacks.notifyCallbacks(this, 0, null)
    }

    /**
     * Notifies listeners that a specific property (with [fieldId]) has changed.
     * It's used for two way binding.
     */
    private fun notifyPropertyChanged(fieldId: Int) {
        callbacks.notifyCallbacks(this, fieldId, null);
    }

    override fun addOnPropertyChangedCallback(callback: Observable.OnPropertyChangedCallback) {
        callbacks.add(callback)
    }

    override fun removeOnPropertyChangedCallback(callback: Observable.OnPropertyChangedCallback) {
        callbacks.remove(callback)
    }
}