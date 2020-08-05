package com.aslanovaslan.firemesseageapp.recylerViewItem

import android.content.Context
import com.aslanovaslan.firemesseageapp.R
import com.aslanovaslan.firemesseageapp.glide.GlideApp
import com.aslanovaslan.firemesseageapp.model.ImageMessage
import com.aslanovaslan.firemesseageapp.util.StorageUtil
import com.xwray.groupie.kotlinandroidextensions.GroupieViewHolder
import kotlinx.android.synthetic.main.item_image_message.*


class ImageMessageItem(
    val message: ImageMessage,
    val context: Context
) : MessageItem(message) {
    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        GlideApp.with(context)
            .load(StorageUtil.pathToReference(message.imagePath))
            .placeholder(R.drawable.ic_image_black_24dp)
            .into(viewHolder.imageView_message_image)
        super.bind(viewHolder, position)
    }

    override fun getLayout(): Int {
        return R.layout.item_image_message
    }

    override fun isSameAs(other: com.xwray.groupie.Item<*>): Boolean {
        if (other !is ImageMessageItem)
            return false
        if (this.message != other.message)
            return false
        return true
    }

    override fun equals(other: Any?): Boolean {
        return isSameAs(other as ImageMessageItem)
    }

    override fun hashCode(): Int {
        var result = message.hashCode()
        result = 31 * result + context.hashCode()
        return result
    }
}