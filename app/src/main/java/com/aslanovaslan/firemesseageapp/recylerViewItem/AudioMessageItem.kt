package com.aslanovaslan.firemesseageapp.recylerViewItem

import android.app.Activity
import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import android.os.Handler
import android.view.View
import android.widget.SeekBar
import com.aslanovaslan.firemesseageapp.R
import com.aslanovaslan.firemesseageapp.SingingActivity
import com.aslanovaslan.firemesseageapp.model.AudioMessage
import com.aslanovaslan.firemesseageapp.util.StateRecord
import com.aslanovaslan.firemesseageapp.util.StorageUtil
import com.xwray.groupie.Item
import com.xwray.groupie.kotlinandroidextensions.GroupieViewHolder
import kotlinx.android.synthetic.main.item_audio_message.*
import kotlinx.coroutines.Runnable
import java.io.IOException

class AudioMessageItem(
    val message: AudioMessage,
    val context: Context,
    val activity: Activity
) : MessageItem(message) {
    private var isPlayingState = StateRecord.STOP
    private lateinit var mediaPlayer: MediaPlayer
    private lateinit var seekBarHandler: Handler
    private var isFirstTime = true
    private lateinit var updateSeekBar: Runnable
    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        StorageUtil.pathToReference(message.audioPath).downloadUrl.addOnSuccessListener { uri ->
            fetchedRecord(uri, viewHolder)
        }

      /*  if (mediaPlayer.isPlaying){
            activity.onBackPressed().apply {
                stopPlayingRecord(viewHolder)
            }
        }*/
        super.bind(viewHolder, position)
    }

    private fun fetchedRecord(
        uri: Uri?,
        viewHolder: GroupieViewHolder
    ) {
        if (uri != null) {
            viewHolder.progressBar_play_image.visibility = View.GONE
            viewHolder.image_view_audio_play.setOnClickListener {
                if (isPlayingState == StateRecord.START
                ) {
                    stopPlayingRecord(viewHolder)
                } else if (isPlayingState == StateRecord.STOP && isFirstTime
                ) {
                    startPlayingRecord(uri, viewHolder)
                    viewHolder.image_view_audio_play.setBackgroundResource(R.drawable.ic_pause)
                } else if (isPlayingState == StateRecord.RESUME && mediaPlayer.isPlaying && !isFirstTime) {
                    pauseAudio(viewHolder)
                } else {
                    resumeAudio(viewHolder)
                }
                isFirstTime = false
            }
            viewHolder.seekBar_audio_item.setOnSeekBarChangeListener(object :
                SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(
                    seekBar: SeekBar?,
                    progress: Int,
                    fromUser: Boolean
                ) {

                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {
                    pauseAudio(viewHolder)
                }

                override fun onStopTrackingTouch(seekBar: SeekBar?) {
                    if (seekBar != null) {
                        val progress = seekBar.progress
                        mediaPlayer.seekTo(progress)
                        resumeAudio(viewHolder)
                    }

                }

            })
        }
    }

    private fun pauseAudio(viewHolder: GroupieViewHolder) {
        viewHolder.image_view_audio_play.setBackgroundResource(R.drawable.ic_send_black_24dp)
        mediaPlayer.pause()
        isPlayingState = StateRecord.PAUSE
        seekBarHandler.removeCallbacks(updateSeekBar)
    }

    private fun resumeAudio(viewHolder: GroupieViewHolder) {
        viewHolder.image_view_audio_play.setBackgroundResource(R.drawable.ic_pause)
        mediaPlayer.start()
        isPlayingState = StateRecord.RESUME
        seekBarHandler = Handler()
        updateRunable(viewHolder)
        seekBarHandler.postDelayed(updateSeekBar, 500)
    }

    private fun stopPlayingRecord(viewHolder: GroupieViewHolder) {
        viewHolder.image_view_audio_play.setBackgroundResource(R.drawable.ic_send_black_24dp)
        mediaPlayer.stop()
        isPlayingState = StateRecord.STOP
        seekBarHandler.removeCallbacks(updateSeekBar)
    }

    private fun startPlayingRecord(
        uri: Uri,
        viewHolder: GroupieViewHolder
    ) {
        isPlayingState = StateRecord.RESUME
        mediaPlayer = MediaPlayer().apply {
            try {
                setDataSource(context, uri)
                prepare()
                start()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        viewHolder.seekBar_audio_item.max = mediaPlayer.duration
        seekBarHandler = Handler()
        updateRunable(viewHolder)
        seekBarHandler.postDelayed(updateSeekBar, 0)

    }

    private fun updateRunable(viewHolder: GroupieViewHolder) {
        updateSeekBar = object : Runnable {
            override fun run() {
                viewHolder.seekBar_audio_item.progress = mediaPlayer.currentPosition
                seekBarHandler.postDelayed(this, 500)
            }
        }
    }

    override fun getLayout(): Int {
        return R.layout.item_audio_message
    }

    override fun isSameAs(other: Item<*>): Boolean {
        if (other !is AudioMessageItem) {
            return false
        }
        if (other.message != this.message) {
            return false
        }
        return true
    }

    override fun equals(other: Any?): Boolean {
        return isSameAs(other as AudioMessageItem)
    }

    override fun hashCode(): Int {
        var result = message.hashCode()
        result = 31 * result + context.hashCode()
        return result
    }
}