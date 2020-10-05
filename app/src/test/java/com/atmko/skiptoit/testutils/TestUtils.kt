package com.atmko.skiptoit.testutils

import org.mockito.ArgumentCaptor
import org.mockito.Mockito

fun <T> ArgumentCaptor<T>.kotlinCapture(): T {
    return this.capture()
}

class TestUtils {
    companion object {
        fun <T> kotlinAny(type: Class<T>): T = Mockito.any<T>(type)
    }
}
