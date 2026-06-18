package com.example.service

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.example.data.local.PrayerTimingsEntity
import java.text.SimpleDateFormat
import java.util.*

object AdhanAlarmScheduler {
    private const val TAG = "AdhanAlarmScheduler"

    @SuppressLint("ScheduleExactAlarm")
    fun scheduleAlarms(context: Context, timings: PrayerTimingsEntity, enabledPrayers: Set<String>) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val sdf = SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.US)
        val now = Calendar.getInstance()

        // List of core prayers with timings
        val prayerList = listOf(
            "Fajr" to timings.fajr,
            "Dhuhr" to timings.dhuhr,
            "Asr" to timings.asr,
            "Maghrib" to timings.maghrib,
            "Isha" to timings.isha
        )

        for ((name, timeStr) in prayerList) {
            if (!enabledPrayers.contains(name)) {
                cancelAlarm(context, name)
                continue
            }

            try {
                // Clear any non-digits from time string (e.g. "05:15 (EST)" -> "05:15")
                val cleanTime = timeStr.substringBefore(" ").trim()
                val dateStr = "${timings.date} $cleanTime"
                val prayerCal = Calendar.getInstance().apply {
                    val parsed = sdf.parse(dateStr)
                    if (parsed != null) {
                        time = parsed
                    }
                }

                // If prayer has already passed today, schedule for tomorrow
                if (prayerCal.before(now)) {
                    prayerCal.add(Calendar.DAY_OF_YEAR, 1)
                }

                val intent = Intent(context, AdhanBroadcastReceiver::class.java).apply {
                    action = "COM_EXAMPLE_PLAY_ADHAN"
                    putExtra("PRAYER_NAME", name)
                    putExtra("DATE_STRING", timings.date)
                }

                // Unique pending intent ID based on prayer hash code
                val requestCode = name.hashCode()
                val pendingIntent = PendingIntent.getBroadcast(
                    context,
                    requestCode,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        prayerCal.timeInMillis,
                        pendingIntent
                    )
                } else {
                    alarmManager.setExact(
                        AlarmManager.RTC_WAKEUP,
                        prayerCal.timeInMillis,
                        pendingIntent
                    )
                }
                Log.d(TAG, "Scheduled alarm for $name at ${prayerCal.time}")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to schedule alarm for $name: ${e.message}", e)
            }
        }
    }

    fun cancelAlarm(context: Context, prayerName: String) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val intent = Intent(context, AdhanBroadcastReceiver::class.java).apply {
            action = "COM_EXAMPLE_PLAY_ADHAN"
        }
        val requestCode = prayerName.hashCode()
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
            Log.d(TAG, "Cancelled alarm for $prayerName")
        }
    }
}
