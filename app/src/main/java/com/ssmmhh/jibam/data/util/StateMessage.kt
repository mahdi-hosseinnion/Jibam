package com.ssmmhh.jibam.data.util

import android.view.View

data class StateMessage(val response: Response)

data class Response(
    val message: String?,
    val uiComponentType: UIComponentType,
    val messageType: MessageType
)

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
        val parentView: View
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