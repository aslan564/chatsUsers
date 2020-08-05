package com.aslanovaslan.firemesseageapp.ui.account

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.aslanovaslan.firemesseageapp.R
import com.aslanovaslan.firemesseageapp.SingingActivity
import com.aslanovaslan.firemesseageapp.glide.GlideApp
import com.aslanovaslan.firemesseageapp.notfication.NotificationHelper
import com.aslanovaslan.firemesseageapp.util.FirestoreUtil
import com.aslanovaslan.firemesseageapp.util.StorageUtil
import com.firebase.ui.auth.AuthUI
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.messaging.FirebaseMessaging
import com.onesignal.OneSignal
import kotlinx.android.synthetic.main.fragment_my_account.*
import kotlinx.android.synthetic.main.fragment_my_account.editText_bio
import kotlinx.android.synthetic.main.fragment_my_account.editText_name
import kotlinx.android.synthetic.main.fragment_my_account.imageView_profile_picture
import kotlinx.android.synthetic.main.fragment_my_account.view.*
import org.jetbrains.anko.clearTask
import org.jetbrains.anko.newTask
import org.jetbrains.anko.support.v4.intentFor
import org.jetbrains.anko.support.v4.toast
import java.io.ByteArrayOutputStream

@Suppress("DEPRECATION")
class MyAccountFragment : Fragment() {
    private val AA_SELECT_IMAGE = 2
    private lateinit var selectedImageBytes: ByteArray

    private var pictureJustChange = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val root = inflater.inflate(R.layout.fragment_my_account, container, false)
        /* NotificationHelper.displayNotification(
             this.requireContext(),
             "title",
             "body",
             mutableMapOf()
         )*/
        FirebaseMessaging.getInstance().subscribeToTopic("updates")

        root.apply {
            imageView_profile_picture.setOnClickListener {
                val intentGallery = Intent().apply {
                    type = "image/*"
                    action = Intent.ACTION_GET_CONTENT
                    putExtra(
                        Intent.EXTRA_MIME_TYPES,
                        arrayOf("image/jpeg", "image/jpg", "image/png")
                    )
                }
                startActivityForResult(
                    Intent.createChooser(intentGallery, "Select Image"),
                    AA_SELECT_IMAGE
                )
            }
            btn_save.setOnClickListener {
                if (::selectedImageBytes.isInitialized) {
                    StorageUtil.uploadProfilePhoto(selectedImageBytes) { imagePath ->
                        FirestoreUtil.updateCurrentUser(
                            editText_name.text.toString().trim(),
                            editText_bio.text.toString().trim(), imagePath
                        )
                        toast("profile updated")
                    }
                } else {
                    FirestoreUtil.updateCurrentUser(
                        editText_name.text.toString().trim(),
                        editText_bio.text.toString().trim(), null
                    )
                    toast("profile updated")
                }
            }

            btn_sign_out.setOnClickListener {
                AuthUI.getInstance().signOut(this@MyAccountFragment.requireContext())
                    .addOnSuccessListener {
                        startActivity(intentFor<SingingActivity>().newTask().clearTask())
                    }
            }
        }
        return root
    }



    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == AA_SELECT_IMAGE && resultCode == Activity.RESULT_OK && data != null && data.data != null) {
            val selectedImagePath = data.data
            val selectedImageBitmap: Bitmap
            if (selectedImagePath != null) {
                if (Build.VERSION.SDK_INT >= 28) {
                    val source = ImageDecoder.createSource(
                        requireActivity().contentResolver,
                        selectedImagePath
                    )
                    selectedImageBitmap = ImageDecoder.decodeBitmap(source)
                    val outputStream = ByteArrayOutputStream()
                    selectedImageBitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)
                    selectedImageBytes = outputStream.toByteArray()
                } else {
                    selectedImageBitmap = MediaStore.Images.Media.getBitmap(
                        requireActivity().contentResolver,
                        selectedImagePath
                    )
                    val outputStream = ByteArrayOutputStream()
                    selectedImageBitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)
                    selectedImageBytes = outputStream.toByteArray()
                }
            }

            GlideApp.with(this).load(selectedImagePath).into(imageView_profile_picture)
            progressBarMyAccount.visibility = View.INVISIBLE
            pictureJustChange = true
        }
    }



    override fun onStart() {
        super.onStart()
        FirestoreUtil.getCurrentUser { user ->
            if (this@MyAccountFragment.isVisible) {
                editText_name.setText(user.name.toString())
                editText_bio.setText(user.bio.toString())
                if (!pictureJustChange && user.profilePicturePath != null) {
                    GlideApp.with(this)
                        .load(StorageUtil.pathToReference(user.profilePicturePath))
                        .placeholder(R.drawable.ic_baseline_account_circle_24)
                        .into(imageView_profile_picture)
                    progressBarMyAccount.visibility = View.INVISIBLE
                } else
                    progressBarMyAccount.visibility = View.INVISIBLE

            }
        }
    }

    companion object {
        private const val TAG = "MyAccountFragment"
    }
}