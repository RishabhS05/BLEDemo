package com.example.bleexampleapp.utils

import android.util.Log

object DebugLog {

    var className : String? = null
    var methodName : String? = null
    var lineNumber = 0
    val isDebuggable : Boolean
        get() = true

    private fun createLog(log : String) : String {
        return "[" + methodName + ":" + lineNumber + "]" + log
    }

    private fun getMethodNames(sElements : Array<StackTraceElement>) {
        className = sElements[1].fileName
        methodName = sElements[1].methodName
        lineNumber = sElements[1].lineNumber
    }

    fun e(message : String) {
        if(!isDebuggable) return
        // Throwable instance must be created before any methods
        getMethodNames(Throwable().stackTrace)
        Log.e(className, createLog(message))
    }

    fun i(message : String) {
        if(!isDebuggable) return
        getMethodNames(Throwable().stackTrace)
        Log.i(className, createLog(message))
    }

    fun d(message : String) {
        if(!isDebuggable) return
        getMethodNames(Throwable().stackTrace)
        Log.d(className, createLog(message))
    }

    fun v(message : String) {
        if(!isDebuggable) return
        getMethodNames(Throwable().stackTrace)
        Log.v(className, createLog(message))
    }

    fun w(message : String) {
        if(!isDebuggable) return
        getMethodNames(Throwable().stackTrace)
        Log.w(className, createLog(message))
    }

    fun wtf(message : String) {
        if(!isDebuggable) return
        getMethodNames(Throwable().stackTrace)
        Log.wtf(className, createLog(message))
    }
}