package com.aslanovaslan.firemesseageapp

import android.accessibilityservice.GestureDescription
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.aslanovaslan.firemesseageapp.ui.account.MyAccountFragment
import com.aslanovaslan.firemesseageapp.ui.people.PeopleFragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.onesignal.OSSubscriptionObserver
import com.onesignal.OSSubscriptionStateChanges
import com.onesignal.OneSignal


class MainActivity : AppCompatActivity(), OSSubscriptionObserver {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val navView: BottomNavigationView = findViewById(R.id.navigation)
        replaceFragment(MyAccountFragment())
        OneSignal.addSubscriptionObserver(this);



        navView.setOnNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.navigation_people -> {
                    replaceFragment(PeopleFragment())
                    true
                }
                R.id.navigation_account -> {
                    replaceFragment(MyAccountFragment())
                    //displayNotification()
                    true
                }
                else -> false
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val mChannel=NotificationChannel(CHANNEL_ID,CHANNEL_NAME,NotificationManager.IMPORTANCE_DEFAULT)
            mChannel.description=CHANNEL_DESCRIPTION
            val nManager= getSystemService(NotificationManager::class.java) ?: return
            nManager.createNotificationChannel(mChannel)
        }
    }

    private fun replaceFragment(fragment: Fragment) {
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.nav_host_fragment, fragment)
        transaction.commit()
    }


    companion object{
        public val CHANNEL_ID = "com.aslanovaslan.firemesseageapp"
        private const val TAG = "MainActivity"
        private val CHANNEL_NAME = "com.aslanovaslan.firemesseageapp"
        private val CHANNEL_DESCRIPTION = "com.aslanovaslan.firemesseageapp"
    }

    override fun onOSSubscriptionChanged(stateChanges: OSSubscriptionStateChanges?) {
        if (!stateChanges!!.from.subscribed &&
            stateChanges.to.subscribed
        ) {
            AlertDialog.Builder(this)
                .setMessage("You've successfully subscribed to push notifications!")
                .show()
            // get player ID
            stateChanges.to.userId
        }

        Log.i("Debug", "onOSPermissionChanged: $stateChanges")
    }
}