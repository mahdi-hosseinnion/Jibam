package com.ssmmhh.jibam.util


interface StateEvent {

    /**
     * state event error message represented in string resource id.
     */
    val errorInfo: Int

    val getId: String
}