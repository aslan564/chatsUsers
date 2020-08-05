package com.aslanovaslan.firemesseageapp

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.aslanovaslan.firemesseageapp.util.FirestoreUtil
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.ErrorCodes
import com.firebase.ui.auth.IdpResponse
import com.google.firebase.iid.FirebaseInstanceId
import com.onesignal.OneSignal
import kotlinx.android.synthetic.main.activity_singin.*
import org.jetbrains.anko.*
import org.jetbrains.anko.design.longSnackbar
import java.lang.Error



@Suppress("DEPRECATION")
class SingingActivity : AppCompatActivity() {
    private val AA_SIGN_IN = 1
    private val singIngProviders = listOf(
        AuthUI.IdpConfig.PhoneBuilder().build(),
        AuthUI.IdpConfig.EmailBuilder()
            .setAllowNewAccounts(true)
            .setRequireName(true)
            .build()
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_singin)
        account_sign_in.setOnClickListener {
            val intent = AuthUI.getInstance().createSignInIntentBuilder()
                .setAvailableProviders(singIngProviders)
                .setLogo(R.drawable.ic_fire_emoji)
                .build()
            startActivityForResult(intent, AA_SIGN_IN)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == AA_SIGN_IN) {
            val response = IdpResponse.fromResultIntent(data)
            if (resultCode == Activity.RESULT_OK) {
                val dialog = progressDialog(message = "Please wait a bit…", title = "Creating account")
                FirestoreUtil.initCurrentUserIfFirstTime {
                    startActivity(intentFor<MainActivity>().newTask().clearTask())
                    FirebaseInstanceId.getInstance().instanceId
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                val token = task.result!!.token
                                FirestoreUtil.setFCMRegistrationTokens(token)
                            } else {
                                return@addOnCompleteListener
                            }
                        }

                    dialog.dismiss()
                }
            } else if (resultCode == Activity.RESULT_CANCELED) {
                if (response == null) return
                when (response.error!!.errorCode) {
                    ErrorCodes.NO_NETWORK -> {
                        longSnackbar(constraint_layout, "No network")
                    }
                    ErrorCodes.UNKNOWN_ERROR -> {
                        longSnackbar(constraint_layout, "Unnown error")
                    }
                }
            }
        }
    }


}