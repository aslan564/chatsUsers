package com.aslanovaslan.firemesseageapp.services

import android.annotation.SuppressLint
import android.content.Intent
import android.util.Log
import androidx.core.content.ContextCompat.startActivity
import org.jetbrains.anko.startActivity
import com.aslanovaslan.firemesseageapp.ChatActivity
import com.firebase.ui.auth.AuthUI.getApplicationContext
import com.onesignal.OSInAppMessageAction
import com.onesignal.OneSignal


class MyFirebaseMessagingService : OneSignal.InAppMessageClickHandler  {
    private val ACTION_ID_MY_CUSTOM_ID = "MY_CUSTOM_ID"
                                                        
    @SuppressLint("RestrictedApi")
    override fun inAppMessageClicked(result: OSInAppMessageAction?) {
        /*if (ACTION_ID_MY_CUSTOM_ID == result!!.clickName) {
            Log.i("OneSignalExample", "Custom Action took place! Starting YourActivity!")
            val intent = Intent(getApplicationContext().applicationContext, ChatActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(getApplicationContext(),intent)
        }*/
    }


    /* override fun onMessageReceived(remoteMessage: RemoteMessage) {
         super.onMessageReceived(remoteMessage)
         if (remoteMessage.notification != null) {
             val title = remoteMessage.notification!!.title
             val body = remoteMessage.notification!!.body
             val bildiriimData=remoteMessage.data
             if (title != null && body != null) {
                 NotificationHelper.displayNotification(applicationContext, title, body,bildiriimData)
             }
         }
     }

     override fun onNewToken(token: String) {
         if (FirebaseAuth.getInstance().currentUser!=null) {
             setFCMRegistrationTokens(token)
         }
     }*/
}