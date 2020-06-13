package com.atmko.podcastapp.util

import android.text.Editable
import android.widget.ImageView
import com.atmko.podcastapp.R
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestOptions

fun String.toEditable() : Editable {
    return Editable.Factory.getInstance().newEditable(this)
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