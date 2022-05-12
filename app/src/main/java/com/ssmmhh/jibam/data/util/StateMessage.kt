package com.ssmmhh.jibam.data.util

import android.content.Context
import android.content.res.Resources
import android.view.View
import java.lang.StringBuilder

data class StateMessage(val response: Response)

data class Response(
    /**
     * Represent message in an array of string resource ids
     */
    val message: IntArray?,
    val uiComponentType: UIComponentType,
    val messageType: MessageType
) {
    /**
     * Override equals only for [message] field b/c its recommended for data class.
     */
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Response

        if (message != null) {
            if (other.message == null) return false
            if (!message.contentEquals(other.message)) return false
        } else if (other.message != null) return false
        if (uiComponentType != other.uiComponentType) return false
        if (messageType != other.messageType) return false

        return true
    }

    /**
     * Override hashCode only for [message] field b/c its recommended for data class.
     */
    override fun hashCode(): Int {
        var result = message?.contentHashCode() ?: 0
        result = 31 * result + uiComponentType.hashCode()
        result = 31 * result + messageType.hashCode()
        return result
    }

    /**
     * Convert message that is list of string resource ids to string by resources.getString(resId).
     */
    fun getStringMessage(resources: Resources): String? {
        if (message == null) return null
        val result = StringBuilder()
        for (resId in message) {
            result.append(resources.getString(resId))
        }
        return result.toString()
    }
    /**
     * Convert message that is list of string resource ids to string by context.getString(resId).
     */
    fun getStringMessage(context: Context): String? {
        if (message == null) return null
        val result = StringBuilder()
        for (resId in message) {
            result.append(context.getString(resId))
        }
        return result.toString()
    }
}

sealed class UIComponentType {

    object Toast : UIComponentType()

    object Dialog : UIComponentType()

    class AreYouSureDialog(
        val callback: AreYouSureCallback
    ) : UIComponentType()

    class DiscardOrSaveDialog(
        val callback: DiscardOrSaveCallback
    ) : UIComponentType()

    class UndoSnackBar(
        val callback: UndoCallback,
        val parentView: View? = null
    ) : UIComponentType()

    object None : UIComponentType()
}

sealed class MessageType {

    object Success : MessageType()

    object Error : MessageType()

    object Info : MessageType()

    object None : MessageType()
}

//TODO("Replace interface callbacks with kotlin Lambda")
interface StateMessageCallback {

    fun removeMessageFromStack()
}

interface AreYouSureCallback {

    fun proceed()

    fun cancel()
}

interface DiscardOrSaveCallback {

    fun save()

    fun discard()

    fun cancel()
}


interface UndoCallback {

    fun undo()

    fun onDismiss()

}