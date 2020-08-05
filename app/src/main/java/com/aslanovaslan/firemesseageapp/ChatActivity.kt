package com.aslanovaslan.firemesseageapp

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.media.MediaRecorder
import android.net.Uri
import android.os.Bundle
import android.os.SystemClock
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.aslanovaslan.firemesseageapp.model.*
import com.aslanovaslan.firemesseageapp.util.FirestoreUtil
import com.aslanovaslan.firemesseageapp.util.StorageUtil
import com.aslanovaslan.firemesseageapp.util.typeMessage
import com.google.android.gms.tasks.OnFailureListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.onesignal.OneSignal
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Section
import com.xwray.groupie.kotlinandroidextensions.GroupieViewHolder
import com.xwray.groupie.kotlinandroidextensions.Item
import kotlinx.android.synthetic.main.activity_chat.*
import org.json.JSONException
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

private const val AA_SELECT_IMAGE = 2
private const val LOG_TAG = "AudioRecordTest"
private const val REQUEST_RECORD_AUDIO_PERMISSION = 200

@Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS", "DEPRECATION")
class ChatActivity : AppCompatActivity(), TextWatcher, View.OnClickListener {
    private val PERMISSION_RECORD_AUDIO: Int = 5
    private lateinit var currentChannelId: String
    private lateinit var currentUser: UserModel
    private lateinit var otherUserId: String
    private lateinit var messageToSendContent: String
    private var mediaRecorder: MediaRecorder? = null

    private var isRecording = false

    private lateinit var messagesListenerRegistration: ListenerRegistration
    private var shouldInitRecyclerView = true
    private lateinit var messagesSection: Section
    private lateinit var recordFromFile: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)
        editText_message.addTextChangedListener(this)
        image_sound_send.setOnClickListener(this)


        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = intent.getStringExtra(AppConstants.USER_NAME)

        FirestoreUtil.getCurrentUser {
            currentUser = it
        }

        otherUserId = intent.getStringExtra(AppConstants.USER_ID)
        FirestoreUtil.getOrCreateChatChannel(otherUserId) { channelId ->
            currentChannelId = channelId

            messagesListenerRegistration =
                FirestoreUtil.addChatMessagesListener(
                    channelId,
                    this,
                    this,
                    this::updateRecyclerView
                )

            text_message_send.setOnClickListener {
                if (editText_message.text.isNullOrEmpty()) return@setOnClickListener
                val messageToSend =
                    TextMessage(
                        editText_message.text.toString().trim(), Calendar.getInstance().time,
                        FirebaseAuth.getInstance().currentUser!!.uid,
                        otherUserId, currentUser.name
                    )
                editText_message.setText("")
                FirestoreUtil.sendMessage(messageToSend, channelId)
                messageToSendContent = messageToSend.text
                getOtherUserSignalId(otherUserId)
            }

            fab_send_image.setOnClickListener {
                val intent = Intent().apply {
                    type = "image/*"
                    action = Intent.ACTION_GET_CONTENT
                    putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("image/jpeg", "image/png"))
                }
                startActivityForResult(
                    Intent.createChooser(intent, "Select Image"),
                    AA_SELECT_IMAGE
                )

            }
        }
        OneSignal.startInit(this)
            .inFocusDisplaying(OneSignal.OSInFocusDisplayOption.Notification)
            .unsubscribeWhenNotificationsAreDisabled(true)
            .init()
        OneSignal.idsAvailable { userId, registrationId ->
            FirebaseFirestore.getInstance()
                .document("users/${FirebaseAuth.getInstance().currentUser!!.uid}")
                .update("oneSignalId", userId).addOnSuccessListener {
                    Log.d("oneSignalId", "onCreate: $userId")
                }.addOnFailureListener(OnFailureListener {
                    Log.e("oneSignalId", "onCreate: ", it)
                })
        }

    }

    private fun sendNotificationMessage(
        signalIdData: String
    ) {
        try {
            OneSignal.postNotification(
                JSONObject("{'contents': {'en':'${messageToSendContent}'}, 'include_player_ids': ['$signalIdData']}"),
                object : OneSignal.PostNotificationResponseHandler {
                    override fun onSuccess(response: JSONObject?) {
                        Log.i(
                            "OneSignalExample",
                            "postNotification Success: " + response.toString()
                        );
                    }

                    override fun onFailure(response: JSONObject?) {
                        Log.e(
                            "OneSignalExample",
                            "postNotification Failure: " + response.toString()
                        );
                    }

                }
            )
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }


    private fun getOtherUserSignalId(otherUserId: String?) {
        if (otherUserId != null) {

            FirebaseFirestore.getInstance().document("users/$otherUserId").get()
                .addOnSuccessListener { dataDir ->
                    if (dataDir != null) {
                        val signalIdData = dataDir.get("oneSignalId").toString()
                        sendNotificationMessage(signalIdData)
                    }
                }.addOnFailureListener {
                    Log.e("oneSignalId", "onCreate: ", it)
                }
        }
    }

    /*  private var permissionToRecordAccepted = false
      private var permissions: Array<String> = arrayOf(Manifest.permission.RECORD_AUDIO)
  */
    /*   override fun onRequestPermissionsResult(
           requestCode: Int,
           permissions: Array<String>,
           grantResults: IntArray
       ) {
           super.onRequestPermissionsResult(requestCode, permissions, grantResults)
           permissionToRecordAccepted = if (requestCode == REQUEST_RECORD_AUDIO_PERMISSION) {
               grantResults[0] == PackageManager.PERMISSION_GRANTED
           } else {
               false
           }
           if (!permissionToRecordAccepted) finish()
       }*/
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == AA_SELECT_IMAGE && resultCode == Activity.RESULT_OK && data != null &&
            data.data != null
        ) {
            val selectedImagePath = data.data
            val selectedImageBitmap =
                MediaStore.Images.Media.getBitmap(contentResolver, selectedImagePath)
            val outputStream = ByteArrayOutputStream()
            selectedImageBitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)
            val selectedImageBytes = outputStream.toByteArray()
            StorageUtil.uploadMessageImage(selectedImageBytes) { imagePath ->
                val messageToSend = ImageMessage(
                    imagePath,
                    Calendar.getInstance().time,
                    FirebaseAuth.getInstance().currentUser!!.uid,
                    otherUserId, currentUser.name
                )
                FirestoreUtil.sendMessage(messageToSend, currentChannelId)
                messageToSendContent = "yeni goruntu mesajiniz var"
                getOtherUserSignalId(otherUserId)
            }
        }
    }

    private fun updateRecyclerView(message: List<Item>) {
        fun init() {
            recycler_view_messages.apply {
                layoutManager = LinearLayoutManager(this@ChatActivity)
                setHasFixedSize(true)
                setItemViewCacheSize(100)
                adapter = GroupAdapter<GroupieViewHolder>().apply {
                    messagesSection = Section(message)
                    this.add(messagesSection)
                    notifyDataSetChanged()
                }

            }
            shouldInitRecyclerView = false
        }

        fun updateItems() = messagesSection.update(message)
        if (shouldInitRecyclerView) {
            init()
        } else {
            updateItems()
        }
        recycler_view_messages.scrollToPosition(recycler_view_messages.adapter!!.itemCount - 1)
    }

    override fun afterTextChanged(s: Editable?) {

    }

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

    }

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        if (s != null) {
            if (s.isEmpty()) {
                image_sound_send.visibility = View.VISIBLE
                text_message_send.visibility = View.GONE
            } else {
                image_sound_send.visibility = View.GONE
                text_message_send.visibility = View.VISIBLE
            }

        }
    }

    @SuppressLint("SetTextI18n")
    override fun onClick(v: View) {
        when (v.id) {
            R.id.image_sound_send -> {
                isRecording = if (isRecording) {
                    stopRecording()
                    image_sound_send.setImageResource(R.drawable.ic_stop_record_mic_none_24)

                    false
                } else {
                    if (checkMyPermission()) {
                        startRecording()
                        image_sound_send.setImageResource(R.drawable.ic_start_record_mic_none_24)
                    }
                    true
                }

            }
        }
    }

    private fun startRecording() {
        record_timer.visibility = View.VISIBLE
        editText_message.visibility = View.GONE
        record_timer.base = SystemClock.elapsedRealtime()
        record_timer.start()
        editText_message.hint = "recording..."
        val dateFormat = SimpleDateFormat("yyyy_MM_dd_hh_mm_ss", Locale.getDefault())
        val recorderPath = this.getExternalFilesDir("/")!!.absolutePath
        val dateNow = Date()
        val recordFile = "Recording${dateFormat.format(dateNow)}.3gp"
        recordFromFile = "$recorderPath/$recordFile"
        mediaRecorder = MediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
            setOutputFile(recordFromFile)
            setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)

            try {
                prepare()
            } catch (e: IOException) {
                Log.e(LOG_TAG, "prepare() failed")
            }

            start()
        }
    }

    private fun stopRecording() {
        record_timer.visibility = View.GONE
        editText_message.visibility = View.VISIBLE
        record_timer.stop()
        editText_message.hint = ""
        mediaRecorder?.stop()
        mediaRecorder?.release()
        mediaRecorder = null
        uploadStorage()

    }

    private fun uploadStorage() {
        val uri = Uri.fromFile(File(recordFromFile))
        StorageUtil.uploadMessageAudio(uri) { audioPath ->
            val messageToSend = AudioMessage(
                audioPath,
                Calendar.getInstance().time,
                FirebaseAuth.getInstance().currentUser!!.uid,
                otherUserId, currentUser.name
            )
            FirestoreUtil.sendMessage(messageToSend, currentChannelId)
        }
    }

    private fun checkMyPermission(): Boolean {
        return if (ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            true
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.RECORD_AUDIO),
                PERMISSION_RECORD_AUDIO
            )
            false
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()

    }
}