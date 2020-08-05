package com.aslanovaslan.firemesseageapp.util

import android.app.Activity
import android.content.Context
import android.util.Log
import com.aslanovaslan.firemesseageapp.model.*
import com.aslanovaslan.firemesseageapp.recylerViewItem.AudioMessageItem
import com.aslanovaslan.firemesseageapp.recylerViewItem.ImageMessageItem
import com.aslanovaslan.firemesseageapp.recylerViewItem.PersonItem
import com.aslanovaslan.firemesseageapp.recylerViewItem.TextMessageItem
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.xwray.groupie.kotlinandroidextensions.Item
 var typeMessage:String?=null
object FirestoreUtil {
    private val firestoreInstance: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }

    private val currentUserDocRef: DocumentReference
        get() = firestoreInstance.document(
            "users/${FirebaseAuth.getInstance().currentUser!!.uid}"
        )
    private val chatChannelsCollectionRef = firestoreInstance.collection("sohbetKanali")

    fun initCurrentUserIfFirstTime(onComplete: () -> Unit) {
        currentUserDocRef.get().addOnSuccessListener { documentSnapshot ->
            if (!documentSnapshot.exists()) {
                val newUser = UserModel(
                    FirebaseAuth.getInstance().currentUser!!.displayName ?: "",
                    "", null, ""
                )
                currentUserDocRef.set(newUser).addOnSuccessListener {
                    onComplete()
                }
            } else {
                onComplete()
            }

        }
    }

    fun updateCurrentUser(name: String = "", bio: String = "", profilePicturePath: String? = null) {
        val userFieldMap = mutableMapOf<String, Any>()
        if (name.isNotBlank()) userFieldMap["name"] = name
        if (bio.isNotBlank()) userFieldMap["bio"] = bio
        if (profilePicturePath != null) {
            userFieldMap["profilePicturePath"] = profilePicturePath
        }
        currentUserDocRef.update(userFieldMap)
    }

    fun getCurrentUser(onComplete: (UserModel) -> Unit) {
        currentUserDocRef.get().addOnSuccessListener {
            onComplete(it.toObject(UserModel::class.java)!!)
        }
    }

    fun addUserListener(context: Context, onListen: (List<Item>) -> Unit): ListenerRegistration {
        return firestoreInstance.collection("users")
            .addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                if (firebaseFirestoreException != null) {
                    Log.e("FIRESTORE", "User listener error: ", firebaseFirestoreException)
                    return@addSnapshotListener
                }
                val items = mutableListOf<Item>()
                querySnapshot!!.documents.forEach { document ->
                    if (document.id != FirebaseAuth.getInstance().currentUser?.uid) {
                        items.add(
                            PersonItem(
                                document.toObject(UserModel::class.java)!!,
                                document.id,
                                context
                            )
                        )
                    }

                }
                onListen(items)
            }
    }

    fun removeListener(registration: ListenerRegistration) = registration.remove()


    fun getOrCreateChatChannel(
        otherUserId: String,
        onComplete: (channelId: String) -> Unit
    ) {
        currentUserDocRef.collection("interaktivSohbetKanali")
            .document(otherUserId).get().addOnSuccessListener {
                if (it.exists()) {
                    onComplete(it["channelId"] as String)
                    return@addOnSuccessListener
                }

                val currentUserId = FirebaseAuth.getInstance().currentUser!!.uid

                val newChannel = chatChannelsCollectionRef.document()
                newChannel.set(ChatChannel(mutableListOf(currentUserId, otherUserId)))

                currentUserDocRef
                    .collection("interaktivSohbetKanali")
                    .document(otherUserId)
                    .set(mapOf("channelId" to newChannel.id))

                firestoreInstance.collection("users").document(otherUserId)
                    .collection("interaktivSohbetKanali")
                    .document(currentUserId)
                    .set(mapOf("channelId" to newChannel.id))

                onComplete(newChannel.id)
            }
    }


    fun addChatMessagesListener(
        channelId: String, context: Context, activity: Activity,
        onListen: (List<Item>) -> Unit
    ): ListenerRegistration {
        return chatChannelsCollectionRef.document(channelId).collection("messages")
            .orderBy("time")
            .addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                if (firebaseFirestoreException != null) {
                    Log.e("FIRESTORE", "ChatMessagesListener error.", firebaseFirestoreException)
                    return@addSnapshotListener
                }

                val items = mutableListOf<Item>()
                querySnapshot!!.documents.forEach {
                    val textMessage = it.toObject(TextMessage::class.java)
                    when {
                        textMessage!!.mesajinTipi == MessageType.TEXT -> {
                            items.add(
                                TextMessageItem(
                                    it.toObject(TextMessage::class.java)!!,
                                    context
                                )
                            )
                            typeMessage=MessageType.TEXT
                        }
                        textMessage.mesajinTipi == MessageType.IMAGE -> {
                            items.add(
                                ImageMessageItem(
                                    it.toObject(ImageMessage::class.java)!!,
                                    context
                                )
                            )
                            typeMessage=MessageType.IMAGE
                        }
                        else -> {
                            items.add(
                                AudioMessageItem(
                                    it.toObject(AudioMessage::class.java)!!,
                                    context,
                                    activity
                                )
                            )
                            typeMessage=MessageType.AUDIO
                        }
                    }
                    return@forEach
                }
                onListen(items)
            }
    }

    fun sendMessage(message: Message, channelId: String) {
        chatChannelsCollectionRef.document(channelId)
            .collection("messages")
            .add(message)
    }

    fun getFCMRegistrationTokens(onComplete: (token: String) -> Unit) {
        currentUserDocRef.get().addOnSuccessListener {
            val userModel = it.toObject(UserModel::class.java)
            if (userModel != null) {
                onComplete(userModel.registrationTokens)
            }
        }
    }

    fun setFCMRegistrationTokens(registrationTokens: String) {
        currentUserDocRef.update("registrationTokens", registrationTokens)
    }
    private fun getOtherUserSignalId(otherUserId: String?) {
        if (otherUserId != null) {

            FirebaseFirestore.getInstance().document("users/$otherUserId").get()
                .addOnSuccessListener { dataDir ->
                    if (dataDir != null) {
                        val signalIdData = dataDir.get("oneSignalId").toString()
                        //checkMessageType(signalIdData)
                    }
                }.addOnFailureListener {
                    Log.e("oneSignalId", "onCreate: ", it)
                }
        }
    }
}