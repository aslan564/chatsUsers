package com.aslanovaslan.firemesseageapp.notfication

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.aslanovaslan.firemesseageapp.MainActivity
import com.aslanovaslan.firemesseageapp.R
import com.aslanovaslan.firemesseageapp.SplashActivity
import org.jetbrains.anko.db.DEFAULT

class NotificationHelper {
    companion object {
        const val PENDING_INTENT_CODE=150
        fun displayNotification(
            context: Context,
            title: String,
            body: String,
            bildiriimData: MutableMap<String, String>?=null
        ) {

            val intent = Intent(context, SplashActivity::class.java)
            val pendingIntent=PendingIntent.getActivity(context,PENDING_INTENT_CODE,intent,PendingIntent.FLAG_CANCEL_CURRENT)
            val mBuilder =
                NotificationCompat.Builder(context, MainActivity.CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_baseline_message_24)
                    .setContentTitle(title)
                    .setContentText(body)
                    .setSound(Settings.System.DEFAULT_RINGTONE_URI)
                    .setContentIntent(pendingIntent)
                    .setAutoCancel(true)
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            // .setContentIntent(pendingIntent)
            val mNotification = NotificationManagerCompat.from(context)
            mNotification.notify(1, mBuilder.build())
        }
    }

}