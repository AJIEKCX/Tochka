package com.example.ajiekc.tochka.extensions

import android.graphics.drawable.BitmapDrawable
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory
import android.widget.ImageView
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import java.lang.Exception

fun Picasso.loadRoundedImage(url: String?, imageView: ImageView, placeholder: Int) {
    Picasso.get()
            .load(url)
            .fit()
            .centerCrop()
            .placeholder(placeholder)
            .into(imageView, object : Callback {
                override fun onSuccess() {
                    val bitmapDrawable = imageView.drawable as BitmapDrawable
                    val imageBitmap = bitmapDrawable.bitmap
                    val imageDrawable = RoundedBitmapDrawableFactory.create(imageView.resources, imageBitmap)
                    imageDrawable.isCircular = true
                    imageDrawable.cornerRadius = Math.max(imageBitmap.width, imageBitmap.height) / 2.0f;
                    imageView.setImageDrawable(imageDrawable)
                }

                override fun onError(e: Exception?) {

                }
            })
}
