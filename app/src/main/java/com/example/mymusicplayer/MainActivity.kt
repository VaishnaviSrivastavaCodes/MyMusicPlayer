package com.example.mymusicplayer

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.example.mymusicplayer.databinding.ActivityMainBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {

    private lateinit var exoPlayer: ExoPlayer
    private lateinit var binding: ActivityMainBinding
    private lateinit var btnPlayPause: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var tvCurrentTime: TextView
    private lateinit var tvTotalDuration: TextView
    private var progressJob: Job? = null
    private var isPlaying = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        btnPlayPause = binding.btnPlayPause
        progressBar = binding.progressBar
        tvCurrentTime = binding.tvCurrentTime
        tvTotalDuration = binding.tvTotalDuration

        exoPlayer = ExoPlayer.Builder(this).build()
        val mediaItem =
            MediaItem.fromUri("https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3")

        exoPlayer.setMediaItem(mediaItem)
        exoPlayer.prepare()

        btnPlayPause.setOnClickListener {
            if (isPlaying) {
                exoPlayer.pause()
                btnPlayPause.text = getString(R.string.play)
            } else {
                exoPlayer.play()
                startUpdatingProgress()
                btnPlayPause.text = getString(R.string.pause)
            }
            isPlaying = !isPlaying
        }

        exoPlayer.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                when (playbackState) {
                    Player.STATE_READY -> {
                        val duration = exoPlayer.duration
                        if (duration > 0) {
                            progressBar.max = duration.toInt()
                            tvTotalDuration.text = formatTime(duration)
                        }
                    }

                    Player.STATE_ENDED -> {
                        stopUpdatingProgress()
                        progressBar.progress = 0
                        tvCurrentTime.text = "00:00"
                    }
                }
            }

            override fun onPlayerError(error: PlaybackException) {
                Log.d("Player", "Error: ${error.message}")
            }
        })
    }

    private fun startUpdatingProgress() {
        stopUpdatingProgress()

        progressJob = lifecycleScope.launch(Dispatchers.Default) {
            while (isActive) {
                val isPlaying = withContext(Dispatchers.Main) { exoPlayer.isPlaying }

                if (!isPlaying) break

                val currentPosition =
                    withContext(Dispatchers.Main) { exoPlayer.currentPosition.toInt() }

                extraWork() // Background work

                withContext(Dispatchers.Main) {
                    progressBar.progress = currentPosition
                    tvCurrentTime.text = formatTime(currentPosition.toLong())
                }

                delay(500)
            }
        }
    }

    private fun stopUpdatingProgress() {
        progressJob?.cancel()
        progressJob = null
    }

    private fun extraWork() {
        // Non UI related work
    }

    override fun onDestroy() {
        super.onDestroy()
        exoPlayer.release()
        stopUpdatingProgress()
    }

    private fun formatTime(ms: Long): String {
        val minutes = TimeUnit.MILLISECONDS.toMinutes(ms)
        val seconds = TimeUnit.MILLISECONDS.toSeconds(ms) % 60
        return String.format("%02d:%02d", minutes, seconds)
    }
}
