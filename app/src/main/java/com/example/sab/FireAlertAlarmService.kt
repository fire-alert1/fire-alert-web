package com.example.sab

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

import androidx.core.app.NotificationCompat

class FireAlertAlarmService : Service() {

    companion object {

        // =====================================================
        // ACCIONES
        // =====================================================

        const val ACTION_INICIAR =
            "com.example.sab.ACTION_INICIAR"

        const val ACTION_DETENER =
            "com.example.sab.ACTION_DETENER"


        // =====================================================
        // NOTIFICACIÓN
        // =====================================================

        private const val CHANNEL_ID =
            "firealert_servicio_alarma_v7"

        private const val NOTIFICATION_ID =
            2001


        // =====================================================
        // PREFERENCIAS
        // =====================================================

        private const val PREFS =
            "firealert_local"

        private const val PREF_ALARMA_ACTIVA =
            "alarma_activa_id"

        private const val PREF_ALARMA_DESACTIVADA =
            "alarma_desactivada_id"


        // =====================================================
        // RECURSOS DEL SERVICIO
        // =====================================================

        private var mediaPlayer: MediaPlayer? = null

        private var vibrator: Vibrator? = null

        private var wakeLock: PowerManager.WakeLock? = null


        // =====================================================
        // DETENER ALARMA DESDE CUALQUIER PARTE DE LA APP
        // =====================================================

        fun detenerAlarmaLocal(
            context: Context
        ) {
            val intent =
                Intent(
                    context,
                    FireAlertAlarmService::class.java
                ).apply {
                    action = ACTION_DETENER
                }

            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(intent)
                } else {
                    context.startService(intent)
                }
            } catch (e: Exception) {
                try {
                    context.stopService(intent)
                } catch (_: Exception) {
                }
            }

            FireAlertAlarmState.activa = false
        }
    }


    // =========================================================
    // CREAR SERVICIO
    // =========================================================

    override fun onCreate() {
        super.onCreate()
        crearCanal()
        iniciarNotificacion()
        adquirirWakeLock()
    }


    // =========================================================
    // RECIBIR COMANDOS
    // =========================================================

    override fun onStartCommand(
        intent: Intent?,
        flags: Int,
        startId: Int
    ): Int {

        val action = intent?.action

        when (action) {

            ACTION_INICIAR -> {
                val alarmaId = intent.getStringExtra("alarmaId") ?: ""

                if (alarmaId.isNotEmpty()) {
                    getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                        .edit()
                        .putString(PREF_ALARMA_ACTIVA, alarmaId)
                        .remove(PREF_ALARMA_DESACTIVADA)
                        .apply()
                }

                FireAlertAlarmState.activa = true

                if (wakeLock == null || wakeLock?.isHeld == false) {
                    adquirirWakeLock()
                }

                iniciarSirena()
                iniciarVibracion()
            }

            ACTION_DETENER -> {
                detenerAlarma()

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    stopForeground(STOP_FOREGROUND_REMOVE)
                } else {
                    @Suppress("DEPRECATION")
                    stopForeground(true)
                }

                stopSelf()
            }

            else -> {
                // Por si el servicio se reinicia solo por el sistema
                if (action == null && FireAlertAlarmState.activa) {
                    iniciarSirena()
                }
            }
        }

        return START_NOT_STICKY
    }


    // =========================================================
    // WAKE LOCK
    // =========================================================

    private fun adquirirWakeLock() {
        try {
            val powerManager =
                getSystemService(Context.POWER_SERVICE) as PowerManager

            if (wakeLock != null && wakeLock?.isHeld == true) {
                return
            }

            wakeLock =
                powerManager.newWakeLock(
                    PowerManager.PARTIAL_WAKE_LOCK,
                    "FireAlert::AlarmServiceWakeLock"
                ).apply {
                    // Tiempo de seguridad sin errores de sintaxis
                    acquire(10 * 60 * 1000L) // 10 minutos máximo
                }

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    // =========================================================
    // SIRENA
    // =========================================================

    private fun iniciarSirena() {
        try {
            if (mediaPlayer != null && mediaPlayer?.isPlaying == true) {
                return
            }

            try {
                mediaPlayer?.release()
            } catch (_: Exception) {
            }

            mediaPlayer = null

            val audioAttributes =
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()

            val afd = resources.openRawResourceFd(R.raw.sirena_bomberos)

            mediaPlayer =
                MediaPlayer().apply {
                    setAudioAttributes(audioAttributes)
                    setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
                    afd.close()
                    isLooping = true
                    prepare()
                    start()
                }

        } catch (e: Exception) {
            e.printStackTrace()
            mediaPlayer = null
        }
    }


    // =========================================================
    // VIBRACIÓN
    // =========================================================

    private fun iniciarVibracion() {
        try {
            vibrator?.cancel()
        } catch (_: Exception) {
        }

        try {
            val patron = longArrayOf(0, 800, 400, 800, 400)

            vibrator =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    val manager = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
                    manager.defaultVibrator
                } else {
                    @Suppress("DEPRECATION")
                    getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createWaveform(patron, 0))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(patron, 0)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    // =========================================================
    // NOTIFICACIÓN FOREGROUND
    // =========================================================

    private fun iniciarNotificacion() {
        val launchIntent = packageManager.getLaunchIntentForPackage(packageName)

        val pendingIntent = launchIntent?.let { intent ->
            PendingIntent.getActivity(
                this,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        }

        // Intent para cuando el usuario deslice (swipe) la notificación y quiera apagarla
        val deleteIntent = Intent(this, FireAlertAlarmService::class.java).apply {
            action = ACTION_DETENER
        }
        val deletePendingIntent = PendingIntent.getService(
            this,
            1,
            deleteIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder =
            NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_alert)
                .setContentTitle("🚨 FIRE ALERT")
                .setContentText("¡ALARMA DE BOMBEROS ACTIVA! Toca para abrir")
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setOngoing(false) // Permite deslizar para descartar
                .setAutoCancel(true)
                .setDeleteIntent(deletePendingIntent) // Detiene la alarma al deslizar la notificación

        if (pendingIntent != null) {
            builder.setContentIntent(pendingIntent)
        }

        startForeground(NOTIFICATION_ID, builder.build())
    }


    // =========================================================
    // CREAR CANAL
    // =========================================================

    private fun crearCanal() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return
        }

        try {
            val manager = getSystemService(NotificationManager::class.java)

            val channel =
                NotificationChannel(
                    CHANNEL_ID,
                    "🚨 Servicio de alarma FireAlert",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Alarma de emergencia FireAlert"
                    setSound(null, null)
                    enableVibration(false)
                    lockscreenVisibility = NotificationCompat.VISIBILITY_PUBLIC
                }

            manager.createNotificationChannel(channel)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    // =========================================================
    // DETENER TODO
    // =========================================================

    private fun detenerAlarma() {
        val preferencias = getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val alarmaId = preferencias.getString(PREF_ALARMA_ACTIVA, "") ?: ""

        if (alarmaId.isNotEmpty()) {
            preferencias.edit()
                .putString(PREF_ALARMA_DESACTIVADA, alarmaId)
                .remove(PREF_ALARMA_ACTIVA)
                .apply()
        }

        try {
            mediaPlayer?.let { player ->
                try {
                    if (player.isPlaying) {
                        player.stop()
                    }
                } catch (_: Exception) {
                }

                try {
                    player.reset()
                } catch (_: Exception) {
                }

                try {
                    player.release()
                } catch (_: Exception) {
                }
            }
        } catch (_: Exception) {
        }

        mediaPlayer = null

        try {
            vibrator?.cancel()
        } catch (_: Exception) {
        }

        vibrator = null

        try {
            if (wakeLock?.isHeld == true) {
                wakeLock?.release()
            }
        } catch (_: Exception) {
        }

        wakeLock = null

        try {
            val manager = getSystemService(NotificationManager::class.java)
            manager.cancel(NOTIFICATION_ID)
        } catch (_: Exception) {
        }

        FireAlertAlarmState.activa = false
    }


    override fun onDestroy() {
        detenerAlarma()
        super.onDestroy()
    }


    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}