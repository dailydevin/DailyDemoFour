package com.example.dailydemofour

import android.content.ContentValues.TAG
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ToggleButton
import androidx.appcompat.app.AppCompatActivity
import co.daily.CallClient
import co.daily.CallClientListener
import co.daily.model.CallState
import co.daily.model.OutboundMediaType
import co.daily.model.Participant
import co.daily.settings.Disable
import co.daily.settings.Enable
import co.daily.settings.InputSettingsUpdate
import co.daily.view.VideoView


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        // Create call client
        val call = CallClient(applicationContext)

        // Create map of video views
        val videoViews = mutableMapOf<String, VideoView>()

        // Listen for events
        call.addListener(object : CallClientListener {

            override fun onCallStateUpdated(state: CallState) {
                if (state == CallState.joined) {
                    Log.d(TAG, "You joined the call!")
                }
            }

            // Handle a remote participant joining
            override fun onParticipantJoined(participant: Participant) {
                Log.d(TAG, "Participant ${participant.id} joined the call!")
                val videoView = VideoView(applicationContext)
                videoViews[participant.id.toString()] = videoView

                val videoLayoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                videoLayoutParams.setMargins(20, 20, 20, 20)
                videoLayoutParams.height = 500
                videoView.layoutParams = videoLayoutParams

                val layout = findViewById<LinearLayout>(R.id.videoLinearLayout)

                layout.addView(videoView)

                videoView.track = participant.media?.camera?.track
            }

            // Handle a participant updating (e.g. their tracks changing)
            override fun onParticipantUpdated(participant: Participant) {
                val videoView = videoViews[participant.id.toString()]
                videoView?.track = participant.media?.camera?.track
            }
        })

        // Join the call
        call.join(url = "https://devinr.daily.co/meet") {
            it.error?.apply {
                Log.e(TAG, "Got error while joining call: $msg")
            }
            it.success?.apply {
                Log.i(TAG, "Successfully joined call.")
            }
        }

        findViewById<Button>(R.id.leave).setOnClickListener {
            Log.d("BUTTONS", "User tapped the Leave button")
            call.leave() {
            }
        }

        findViewById<ToggleButton>(R.id.toggleMicrophone).setOnCheckedChangeListener { _, isChecked ->
            Log.d("BUTTONS", "User tapped the Mute button")
            call?.setInputEnabled(OutboundMediaType.Microphone, isChecked)
        }

        findViewById<ToggleButton>(R.id.toggleCamera).setOnCheckedChangeListener { _, isChecked ->
            Log.d("BUTTONS", "User tapped the Cam button")
            call?.setInputEnabled(OutboundMediaType.Camera, isChecked)
        }
    }
}