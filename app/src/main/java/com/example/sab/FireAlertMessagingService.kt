package com.example.sab

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.util.Log

import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage


class FireAlertMessagingService : FirebaseMessagingService() {

    companion object {
        private const val TAG = "FIREALERT_FCM"
        private const val CHANNEL_ID_UPDATE = "firealert_updates_channel"
        private const val APK_UPDATE_URL = "https://github.com/fire-alert1/fire-alert-web/raw/refs/heads/main/fire_alert_V2.1.apk"
    }


    // =========================================================
    // MENSAJE FCM RECIBIDO
    // =========================================================

    override fun onMessageReceived(
        remoteMessage: RemoteMessage
    ) {
        super.onMessageReceived(remoteMessage)

        Log.d(TAG, "====================================")
        Log.d(TAG, "FCM RECIBIDO")
        Log.d(TAG, "Data: ${remoteMessage.data}")
        Log.d(TAG, "Notification: ${remoteMessage.notification}")

        // 1. DETECTAR SI ES UNA NOTIFICACIÓN DE ACTUALIZACIÓN
        val tipoMensaje = remoteMessage.data["tipo"] ?: ""
        val urlDescarga = remoteMessage.data["url"] ?: remoteMessage.data["download_url"]

        if (tipoMensaje == "update" || !urlDescarga.isNullOrEmpty() || remoteMessage.data.containsKey("url")) {
            val finalUrl = if (!urlDescarga.isNullOrEmpty()) urlDescarga else APK_UPDATE_URL
            val titulo = remoteMessage.notification?.title
                ?: remoteMessage.data["titulo"]
                ?: "🚀 ACTUALIZACIÓN OBLIGATORIA (v2.1)"
            val mensaje = remoteMessage.notification?.body
                ?: remoteMessage.data["mensaje"]
                ?: "Toca aquí para descargar la nueva versión de FireAlert."

            mostrarNotificacionActualizacion(titulo, mensaje, finalUrl)
            return
        }

        // 2. LÓGICA DE ALARMAS DE EMERGENCIAS (MANTENIDA)
        val auth = FirebaseAuth.getInstance()
        val usuarioActual = auth.currentUser

        if (usuarioActual == null) {
            Log.w(TAG, "FCM descartado: no hay usuario autenticado")
            return
        }

        val uid = usuarioActual.uid
        val db = FirebaseFirestore.getInstance()

        db.collection("usuarios")
            .document(uid)
            .get()
            .addOnSuccessListener { documento ->
                if (!documento.exists()) {
                    Log.w(TAG, "FCM descartado: usuario no existe")
                    return@addOnSuccessListener
                }

                val activo = documento.getBoolean("activo") ?: false
                if (!activo) {
                    Log.w(TAG, "FCM descartado: usuario desactivado")
                    return@addOnSuccessListener
                }

                // LEER CUARTEL ID DEL USUARIO
                val cuartelIdUsuario = documento.getString("cuartelId") ?: ""
                val cuartelIdMensaje = remoteMessage.data["cuartelId"] ?: ""

                if (cuartelIdMensaje.isNotEmpty() && cuartelIdUsuario.isNotEmpty() && cuartelIdMensaje != cuartelIdUsuario) {
                    Log.w(TAG, "FCM descartado: La alarma es de otro cuartel (Mensaje: $cuartelIdMensaje, Usuario: $cuartelIdUsuario)")
                    return@addOnSuccessListener
                }

                // LEER ROLES
                val rolesFirestore = documento.get("roles")
                val roles = if (rolesFirestore is List<*>) {
                    rolesFirestore
                        .filterIsInstance<String>()
                        .map { it.trim().lowercase() }
                        .filter { it.isNotEmpty() }
                        .distinct()
                } else {
                    emptyList()
                }

                val rolesFinales = if (roles.isNotEmpty()) {
                    roles
                } else {
                    val rolAntiguo = documento.getString("rol")?.trim()?.lowercase()
                    if (!rolAntiguo.isNullOrEmpty()) listOf(rolAntiguo) else emptyList()
                }

                // Permitir recibir la notificación a bomberos, cuarteleros y administradores
                val esRolOperativo = rolesFinales.contains("bombero") ||
                        rolesFinales.contains("cuartelero") ||
                        rolesFinales.contains("administrador")

                if (!esRolOperativo) {
                    Log.w(TAG, "FCM descartado: usuario sin rol operativo válido")
                    return@addOnSuccessListener
                }

                Log.d(TAG, "Usuario válido y del mismo cuartel. Recibiendo alarma.")

                val alarmaId = remoteMessage.data["alarmaId"]
                    ?: remoteMessage.data["id"]
                    ?: remoteMessage.data["alarma_id"]
                    ?: ""

                val intent = Intent(
                    this,
                    FireAlertAlarmService::class.java
                ).apply {
                    action = FireAlertAlarmService.ACTION_INICIAR
                    putExtra("alarmaId", alarmaId)
                }

                try {
                    ContextCompat.startForegroundService(this, intent)
                    Log.d(TAG, "FireAlertAlarmService iniciado correctamente")
                } catch (e: Exception) {
                    Log.e(TAG, "Error iniciando FireAlertAlarmService", e)
                }
            }
            .addOnFailureListener { error ->
                Log.e(TAG, "FCM descartado: error verificando usuario", error)
            }
    }


    // =========================================================
    // MOSTRAR NOTIFICACIÓN QUE ABRE EL NAVEGADOR
    // =========================================================

    private fun mostrarNotificacionActualizacion(titulo: String, mensaje: String, urlDescarga: String) {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Crear canal de notificación para Android 8.0+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val canal = NotificationChannel(
                CHANNEL_ID_UPDATE,
                "Actualizaciones de FireAlert",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notificaciones para descargas de actualizaciones"
            }
            notificationManager.createNotificationChannel(canal)
        }

        // Intent explícito para abrir la URL en el navegador por defecto del teléfono
        val intentDescarga = Intent(Intent.ACTION_VIEW, Uri.parse(urlDescarga)).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            System.currentTimeMillis().toInt(),
            intentDescarga,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(this, CHANNEL_ID_UPDATE)
            .setSmallIcon(android.R.drawable.stat_sys_download) // Icono del sistema de descarga
            .setContentTitle(titulo)
            .setContentText(mensaje)
            .setStyle(NotificationCompat.BigTextStyle().bigText("$mensaje\n\n👉 Toca aquí para instalar la v2.1"))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        notificationManager.notify(2001, builder.build())
        Log.d(TAG, "Notificación de actualización enviada con link: $urlDescarga")
    }


    // =========================================================
    // TOKEN FCM RENOVADO
    // =========================================================

    override fun onNewToken(token: String) {
        super.onNewToken(token)

        Log.d(TAG, "====================================")
        Log.d(TAG, "NUEVO TOKEN FCM")
        Log.d(TAG, token)

        val usuario = FirebaseAuth.getInstance().currentUser
        if (usuario == null) {
            Log.d(TAG, "No hay usuario autenticado.")
            return
        }

        val db = FirebaseFirestore.getInstance()
        db.collection("usuarios").document(usuario.uid).get()
            .addOnSuccessListener { documento ->
                val cuartelId = documento.getString("cuartelId") ?: ""
                if (cuartelId.isNotBlank()) {
                    val topicCuartel = "cuartel_$cuartelId"

                    FirebaseMessaging.getInstance().unsubscribeFromTopic("bomberos")

                    FirebaseMessaging.getInstance().subscribeToTopic(topicCuartel)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                Log.d(TAG, "Token nuevo suscripto correctamente a $topicCuartel")
                            } else {
                                Log.e(TAG, "Error suscribiendo token nuevo al topic", task.exception)
                            }
                        }
                }
            }
    }
}