package com.aslanovaslan.firemesseageapp.ui.people

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.aslanovaslan.firemesseageapp.AppConstants
import com.aslanovaslan.firemesseageapp.ChatActivity
import com.aslanovaslan.firemesseageapp.R
import com.aslanovaslan.firemesseageapp.recylerViewItem.PersonItem
import com.aslanovaslan.firemesseageapp.util.FirestoreUtil
import com.google.firebase.firestore.ListenerRegistration
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.OnItemClickListener
import com.xwray.groupie.Section
import com.xwray.groupie.kotlinandroidextensions.GroupieViewHolder
import com.xwray.groupie.kotlinandroidextensions.Item
import kotlinx.android.synthetic.main.fragment_people.*
import org.jetbrains.anko.support.v4.startActivity

class PeopleFragment : Fragment() {

    private lateinit var userRegistrationListener: ListenerRegistration
    private var shouldInitRecyclerView = true
    private lateinit var peopleSection: Section


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        userRegistrationListener =
            FirestoreUtil.addUserListener(this.requireContext(), this::updateRecyclerViewItem)

        return inflater.inflate(R.layout.fragment_people, container, false)
    }

    private fun updateRecyclerViewItem(items: List<Item>) {
        fun init() {
            recycler_view_people.apply {
                layoutManager = LinearLayoutManager(this@PeopleFragment.requireContext())
                adapter = GroupAdapter<GroupieViewHolder>().apply {
                    peopleSection = Section(items)
                    add(peopleSection)
                    setOnItemClickListener(onItemClick)
                }
            }
            shouldInitRecyclerView = false
        }

        fun updateItems()=peopleSection.update(items)
        if (shouldInitRecyclerView) {
            init()
        } else updateItems()
    }

    override fun onDestroy() {
        super.onDestroy()
        FirestoreUtil.removeListener(userRegistrationListener)
        shouldInitRecyclerView = true
    }

    private val onItemClick = OnItemClickListener { item, view ->
        if (item is PersonItem) {
            startActivity<ChatActivity>(
                AppConstants.USER_NAME to item.person.name,
                AppConstants.USER_ID to item.userId
            )
        }

    }
}