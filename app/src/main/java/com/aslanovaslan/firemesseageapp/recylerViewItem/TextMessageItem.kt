package com.aslanovaslan.firemesseageapp.recylerViewItem

import android.content.Context
import com.aslanovaslan.firemesseageapp.model.TextMessage
import com.xwray.groupie.kotlinandroidextensions.GroupieViewHolder
import kotlinx.android.synthetic.main.text_message_row.*

class TextMessageItem(
    val message: TextMessage,
    val context: Context
) : MessageItem(message){
    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        viewHolder.textView_message_text_right.text = message.text

        super.bind(viewHolder, position)
    }



    override fun getLayout(): Int {
        return com.aslanovaslan.firemesseageapp.R.layout.text_message_row
    }

    override fun isSameAs(other: com.xwray.groupie.Item<*>): Boolean {
        if (other !is TextMessageItem)
            return false
        if (this.message != other.message)
            return false
        return true
    }

    override fun equals(other: Any?): Boolean {
        return isSameAs(other as TextMessageItem)
    }

    override fun hashCode(): Int {
        var result = message.hashCode()
        result = 31 * result + context.hashCode()
        return result
    }
}