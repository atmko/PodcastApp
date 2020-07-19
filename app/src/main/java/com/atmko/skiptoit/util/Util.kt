package com.atmko.skiptoit.util

import android.text.Editable
import android.widget.ImageView
import com.atmko.skiptoit.R
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestOptions
import java.lang.IllegalArgumentException

fun String.toEditable() : Editable {
    return Editable.Factory.getInstance().newEditable(this)
}

fun Int.toBoolean() : Boolean {
    if (this != 1 && this != 0) throw IllegalArgumentException("only values of 0 or 1 allowed")
    return this == 1
}

fun ImageView.loadNetworkImage(urlString: String) {
    val options = RequestOptions()
        .placeholder(R.color.color_unloaded_image)
        .error(android.R.drawable.ic_menu_gallery);

    Glide.with(this.context)
        .load(urlString)
        .transition(DrawableTransitionOptions.withCrossFade())
        .apply(options)
        .into(this)
}