package com.example.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.data.local.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AdhanBroadcastReceiver : BroadcastReceiver() {
    private val TAG = "AdhanBroadcastReceiver"

    override fun onReceive(context: Context, intent: Intent) {
        val prayerName = intent.getStringExtra("PRAYER_NAME") ?: "Prayer"
        val pendingResult = goAsync()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val db = AppDatabase.getDatabase(context)
                val dao = db.appDao()

                // Check global master notification preference
                val alertsPref = dao.getPreference("pref_adhan_enabled")?.value ?: "true"
                if (alertsPref == "false") {
                    Log.d(TAG, "Adhan alerts disabled globally. Skipping alert.")
                    return@launch
                }

                // Check individual prayer toggle
                val prayerAlertPref = dao.getPreference("pref_alert_$prayerName")?.value ?: "true"
                if (prayerAlertPref == "false") {
                    Log.d(TAG, "Adhan alerts disabled for $prayerName. Skipping alert.")
                    return@launch
                }

                // Get selected audio style
                val adhanStyle = dao.getPreference("pref_adhan_style")?.value ?: "Mishary Al-Fasy"

                // Launch notification and sound on main thread/background players
                triggerAdhanAlert(context, prayerName, adhanStyle)

            } catch (e: Exception) {
                Log.e(TAG, "Error in onReceive: ${e.message}", e)
            } finally {
                pendingResult.finish()
            }
        }
    }

    private fun triggerAdhanAlert(context: Context, prayerName: String, style: String) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "adhan_alerts_channel"

        // 1. Create notification channel for modern Android
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Adhan Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Periodic alerts matching exact Islamic prayer timings"
                enableLights(true)
                enableVibration(true)
            }
            notificationManager.createNotificationChannel(channel)
        }

        // 2. Formulate Notification details
        val title = "Salah Al-$prayerName"
        val message = "It is time for the $prayerName prayer. Hayya 'alas-Salah!"

        val notificationBuilder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)

        notificationManager.notify(prayerName.hashCode(), notificationBuilder.build())

        // 3. Audio / Vibrate feedback respecting system profiles
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
        val ringerMode = audioManager?.ringerMode ?: AudioManager.RINGER_MODE_NORMAL

        if (ringerMode == AudioManager.RINGER_MODE_NORMAL) {
            // Normal Sound Mode - play Adhan audio
            playAdhanSound(context, style)
        } else if (ringerMode == AudioManager.RINGER_MODE_VIBRATE) {
            // Vibrate Mode - execute customized Islamic sequence
            vibrateDevice(context)
        }
    }

    private fun vibrateDevice(context: Context) {
        val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator ?: return
        val pattern = longArrayOf(0, 800, 200, 800, 200, 800)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createWaveform(pattern, -1))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(pattern, -1)
        }
    }

    private fun playAdhanSound(context: Context, style: String) {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                // High-quality public streaming direct links for diverse styles of offline or fallback stream,
                // and a standard ringtone backup if there is no internet.
                val audioUrl = when (style) {
                    "Makkah Adhan" -> "https://www.islamcan.com/audio/adhan/azan1.mp3"
                    "Medina Adhan" -> "https://www.islamcan.com/audio/adhan/azan2.mp3"
                    "Mishary Al-Fasy" -> "https://www.islamcan.com/audio/adhan/azan15.mp3"
                    else -> null
                }

                if (audioUrl != null) {
                    val uri = Uri.parse(audioUrl)
                    val player = MediaPlayer().apply {
                        setDataSource(context, uri)
                        setVolume(1.0f, 1.0f)
                        prepareAsync()
                        setOnPreparedListener { start() }
                        setOnCompletionListener { release() }
                        setOnErrorListener { _, _, _ ->
                            // Fallback to local default alarm if url play errors out (e.g. offline)
                            playSystemAlarmFallback(context)
                            true
                        }
                    }
                } else {
                    playSystemAlarmFallback(context)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed playing online stream, falling back: ${e.message}")
                playSystemAlarmFallback(context)
            }
        }
    }

    private fun playSystemAlarmFallback(context: Context) {
        try {
            val alarmUri: Uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            MediaPlayer().apply {
                setDataSource(context, alarmUri)
                setVolume(0.8f, 0.8f)
                prepare()
                start()
                setOnCompletionListener { release() }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed playing fallbacks: ${e.message}")
        }
    }
}
