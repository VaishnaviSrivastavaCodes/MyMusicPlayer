package com.example.mymusicplayer


import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.example.mymusicplayer.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var exoPlayer: ExoPlayer
    private lateinit var binding: ActivityMainBinding
    private lateinit var btnPlayPause: Button
    private var isPlaying = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        btnPlayPause = binding.btnPlayPause
        exoPlayer = ExoPlayer.Builder(this).build()
        val mediaItem =
            MediaItem.fromUri("https://www.bensound.com/bensound-music/bensound-acousticbreeze.mp3")
        exoPlayer.setMediaItem(mediaItem)
        exoPlayer.prepare()

        btnPlayPause.setOnClickListener {
            if (isPlaying) {
                exoPlayer.pause()
                btnPlayPause.text = getString(R.string.play)
            } else {
                exoPlayer.play()
                btnPlayPause.text = getString(R.string.pause)
            }
            isPlaying = !isPlaying
            println("Player State: ${exoPlayer.playbackState}")
        }
        exoPlayer.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(state: Int) {
                when (state) {
                    Player.STATE_IDLE -> {
                        // Player is idle (not prepared)
                        println("ExoPlayer State: IDLE")
                    }
                    Player.STATE_BUFFERING -> {
                        // Player is buffering
                        println("ExoPlayer State: BUFFERING")
                    }
                    Player.STATE_READY -> {
                        // Player is ready (playback can start)
                        println("ExoPlayer State: READY")
                    }
                    Player.STATE_ENDED -> {
                        // Playback ended
                        println("ExoPlayer State: ENDED")
                    }
                }
            }

            override fun onPlayerError(error: PlaybackException) {
                println("ExoPlayer Error: ${error.message}")
            }
        })



    }

    override fun onDestroy() {
        super.onDestroy()
        exoPlayer.release()
    }
}
