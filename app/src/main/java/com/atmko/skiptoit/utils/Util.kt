package com.atmko.skiptoit.utils

import android.os.Build
import android.text.Editable
import android.text.Html
import android.widget.ImageView
import android.widget.TextView
import com.atmko.skiptoit.R
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestOptions
import java.lang.IllegalArgumentException


fun TextView.showLimitedText(maxLines: Int, description: String?) {
    this.maxLines = maxLines
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        this.text =
            Html.fromHtml(description, Html.FROM_HTML_MODE_COMPACT)
    } else {
        this.text = Html.fromHtml(description)
    }
}

fun TextView.showFullText(description: String?) {
    this.maxLines = Int.MAX_VALUE
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        this.text =
            Html.fromHtml(description, Html.FROM_HTML_MODE_COMPACT)
    } else {
        this.text = Html.fromHtml(description)
    }
}

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