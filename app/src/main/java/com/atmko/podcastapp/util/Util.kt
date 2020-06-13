package com.atmko.podcastapp.util

import android.text.Editable

fun String.toEditable() : Editable {
    return Editable.Factory.getInstance().newEditable(this)
}