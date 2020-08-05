package com.aslanovaslan.firemesseageapp.recylerViewItem

import android.content.Context
import com.aslanovaslan.firemesseageapp.R
import com.aslanovaslan.firemesseageapp.glide.GlideApp
import com.aslanovaslan.firemesseageapp.model.UserModel
import com.aslanovaslan.firemesseageapp.util.StorageUtil
import com.xwray.groupie.kotlinandroidextensions.GroupieViewHolder
import com.xwray.groupie.kotlinandroidextensions.Item
import kotlinx.android.synthetic.main.activity_singin.*
import kotlinx.android.synthetic.main.item_person.*
import kotlinx.android.synthetic.main.item_person.view.*

class PersonItem(
    val person: UserModel,
    val userId: String,
    private val context: Context
) : Item() {
    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        viewHolder.itemView.textView_name.text = person.name
        viewHolder.itemView.textView_bio.text = person.bio
        if (person.profilePicturePath != null) {
            GlideApp.with(context)
                .load(StorageUtil.pathToReference(person.profilePicturePath))
                .placeholder(R.drawable.ic_baseline_account_circle_24)
                .into(viewHolder.imageView_profile_picture)
        }
    }

    override fun getLayout(): Int {
        return R.layout.item_person
    }
}