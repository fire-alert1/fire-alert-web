package com.example.sab.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.widget.Toast

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import com.example.sab.FireAlertAlarmService
import com.example.sab.R

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query


// =============================================================
// PALETA FIRE ALERT
// =============================================================

private val FireRed = Color(0xFFE53935)
private val FireRedBright = Color(0xFFFF5148)
private val FireRedDark = Color(0xFF8F1717)

private val FireDark = Color(0xFF080A0C)
private val FireWhite = Color(0xFFF5F7F8)
private val FireGray = Color(0xFFAAB1B7)

private val FireCard = Color(0xE613171A)
private val FireCardLight = Color(0xED1A1F23)


// =============================================================
// MODELO HISTORIAL
// =============================================================

private data class AlarmaHistorial(
    val id: String,
    val usuario: String,
    val motivo: String,
    val estado: String,
    val fechaHora: String
)


// =============================================================
// HOME SCREEN
// =============================================================

@Composable
fun HomeScreen(
    esCuartelero: Boolean,
    esAdministrador: Boolean,
    esAdministradorGeneral: Boolean,
    cuartelActivo: Boolean,
    estadoSuscripcion: String,
    cuartelId: String?,
    onAbrirAdministracion: () -> Unit,
    onAbrirPanelAdministradorGeneral: () -> Unit,
    onAbrirHistorial: () -> Unit,
    onCerrarSesion: () -> Unit
) {
    val context = LocalContext.current
    val preferencias = remember {
        context.getSharedPreferences("firealert_local", Context.MODE_PRIVATE)
    }

    val auth = remember { FirebaseAuth.getInstance() }
    val db = remember { FirebaseFirestore.getInstance() }

    // =========================================================
    // ESTADOS DE ACTUALIZACIÓN OBLIGATORIA
    // =========================================================
    var mostrarDialogoActualizacion by remember { mutableStateOf(false) }
    var urlDescargaApk by remember { mutableStateOf("") }
    val versionCodeActual = 2

    val puedeVerHistorial = esCuartelero || esAdministrador || esAdministradorGeneral
    val puedeBorrarHistorial = esAdministrador || esAdministradorGeneral

    var historialAlarmas by remember { mutableStateOf<List<AlarmaHistorial>>(emptyList()) }
    var cargandoHistorial by remember { mutableStateOf(false) }
    var recargarHistorial by remember { mutableStateOf(0) }
    var mostrarConfirmacionBorrado by remember { mutableStateOf(false) }
    var borrandoHistorial by remember { mutableStateOf(false) }

    var mostrarDialogoMotivo by remember { mutableStateOf(false) }
    var activandoAlarma by remember { mutableStateOf(false) }

    val cuartelEfectivo = if (cuartelId.isNullOrBlank()) "monte-caseros" else cuartelId

    // =========================================================
    // COMPROBAR VERSIÓN MÍNIMA EN FIRESTORE
    // =========================================================
    LaunchedEffect(Unit) {
        db.collection("app_config").document("version")
            .get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val versionRequerida = document.getLong("min_version")?.toInt() ?: 1
                    val linkDescarga = document.getString("download_url") ?: ""

                    if (versionRequerida > versionCodeActual) {
                        urlDescargaApk = linkDescarga
                        mostrarDialogoActualizacion = true
                    }
                }
            }
            .addOnFailureListener { e ->
                Log.e("FIREALERT_VERSION", "Error al verificar versión", e)
            }
    }

    // =========================================================
    // CARGAR HISTORIAL FILTRADO POR CUARTEL
    // =========================================================
    LaunchedEffect(cuartelEfectivo, puedeVerHistorial, recargarHistorial) {
        if (!puedeVerHistorial) {
            historialAlarmas = emptyList()
            return@LaunchedEffect
        }

        cargandoHistorial = true

        db.collection("alarmas")
            .whereEqualTo("cuartelId", cuartelEfectivo)
            .orderBy("fechaHora", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { resultado ->
                historialAlarmas = resultado.documents.map { documento ->
                    val fecha = documento.getTimestamp("fechaHora")?.let {
                        val formatter = java.text.SimpleDateFormat(
                            "dd/MM/yyyy HH:mm",
                            java.util.Locale.getDefault()
                        )
                        formatter.format(it.toDate())
                    } ?: "Fecha no disponible"

                    AlarmaHistorial(
                        id = documento.id,
                        usuario = documento.getString("usuario") ?: "Usuario desconocido",
                        motivo = documento.getString("motivo") ?: "Sin especificar",
                        estado = documento.getString("estado") ?: "DESCONOCIDO",
                        fechaHora = fecha
                    )
                }
                cargandoHistorial = false
            }
            .addOnFailureListener { error ->
                Log.e("FIREALERT_HISTORIAL", "Error cargando historial", error)
                historialAlarmas = emptyList()
                cargandoHistorial = false
            }
    }

    // =========================================================
    // BORRAR HISTORIAL
    // =========================================================
    fun borrarHistorial() {
        if (!puedeBorrarHistorial) return

        borrandoHistorial = true

        db.collection("alarmas")
            .whereEqualTo("cuartelId", cuartelEfectivo)
            .get()
            .addOnSuccessListener { resultado ->
                val documentos = resultado.documents

                if (documentos.isEmpty()) {
                    historialAlarmas = emptyList()
                    borrandoHistorial = false
                    Toast.makeText(context, "No hay historial para borrar", Toast.LENGTH_SHORT).show()
                    return@addOnSuccessListener
                }

                val batch = db.batch()
                documentos.forEach { documento ->
                    batch.delete(documento.reference)
                }

                batch.commit()
                    .addOnSuccessListener {
                        historialAlarmas = emptyList()
                        recargarHistorial++
                        borrandoHistorial = false
                        Toast.makeText(context, "Historial eliminado correctamente", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener { error ->
                        Log.e("FIREALERT_HISTORIAL", "Error borrando historial", error)
                        borrandoHistorial = false
                        Toast.makeText(context, "No se pudo borrar el historial", Toast.LENGTH_SHORT).show()
                    }
            }
            .addOnFailureListener { error ->
                Log.e("FIREALERT_HISTORIAL", "Error buscando historial para borrar", error)
                borrandoHistorial = false
                Toast.makeText(context, "No se pudo acceder al historial", Toast.LENGTH_SHORT).show()
            }
    }

    // =========================================================
    // DIÁLOGO CONFIRMAR BORRADO
    // =========================================================
    if (mostrarConfirmacionBorrado) {
        AlertDialog(
            onDismissRequest = {
                if (!borrandoHistorial) {
                    mostrarConfirmacionBorrado = false
                }
            },
            containerColor = Color(0xFF15191E),
            title = {
                Text(
                    text = "⚠ BORRAR HISTORIAL",
                    color = FireRedBright,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "¿Está seguro de que desea eliminar todo el historial de alarmas de este cuartel?\n\nEsta acción no se puede deshacer.",
                    color = FireWhite,
                    fontSize = 14.sp,
                    lineHeight = 21.sp
                )
            },
            dismissButton = {
                TextButton(
                    onClick = { mostrarConfirmacionBorrado = false },
                    enabled = !borrandoHistorial
                ) {
                    Text(
                        text = "CANCELAR",
                        color = FireGray,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        mostrarConfirmacionBorrado = false
                        borrarHistorial()
                    },
                    enabled = !borrandoHistorial
                ) {
                    Text(
                        text = if (borrandoHistorial) "BORRANDO..." else "BORRAR",
                        color = FireRedBright,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        )
    }

    // =========================================================
    // BLOQUEO COMERCIAL
    // =========================================================
    var mensajeBloqueo by remember { mutableStateOf("") }
    var mostrarBloqueo by remember { mutableStateOf(false) }

    fun comprobarPermisoAlarma(): Boolean {
        if (esAdministradorGeneral) return true

        if (!cuartelActivo) {
            mensajeBloqueo = "El cuartel se encuentra desactivado por el administrador general.\n\nLa activación de alarmas está temporalmente bloqueada."
            mostrarBloqueo = true
            return false
        }

        when (estadoSuscripcion) {
            "activa" -> return true
            "pendiente" -> {
                mensajeBloqueo = "La suscripción del cuartel está pendiente de activación.\n\nNo es posible emitir alarmas hasta que el servicio sea activado."
                mostrarBloqueo = true
                return false
            }
            "vencida" -> {
                mensajeBloqueo = "La suscripción del cuartel se encuentra vencida.\n\nNo es posible emitir alarmas hasta regularizar el servicio."
                mostrarBloqueo = true
                return false
            }
            "suspendida" -> {
                mensajeBloqueo = "El servicio del cuartel se encuentra suspendido.\n\nLa activación de alarmas está bloqueada hasta que el administrador general restablezca el servicio."
                mostrarBloqueo = true
                return false
            }
            else -> {
                mensajeBloqueo = "El estado de la suscripción no permite activar alarmas.\n\nEstado actual: ${estadoSuscripcion.ifBlank { "desconocido" }}"
                mostrarBloqueo = true
                return false
            }
        }
    }

    // =========================================================
    // DIÁLOGO DE BLOQUEO COMERCIAL
    // =========================================================
    if (mostrarBloqueo) {
        AlertDialog(
            onDismissRequest = { mostrarBloqueo = false },
            containerColor = Color(0xFF15191E),
            title = {
                Text(
                    text = "🚫 ALARMA BLOQUEADA",
                    color = FireRedBright,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = mensajeBloqueo,
                    color = FireWhite,
                    fontSize = 14.sp,
                    lineHeight = 21.sp
                )
            },
            confirmButton = {
                TextButton(
                    onClick = { mostrarBloqueo = false }
                ) {
                    Text(
                        text = "ENTENDIDO",
                        color = FireRedBright,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        )
    }

    // =========================================================
    // ESTADOS DE ALARMA LOCAL
    // =========================================================
    var alarmaId by remember {
        mutableStateOf(preferencias.getString("alarma_activa_id", "") ?: "")
    }
    var alarmaActiva by remember {
        mutableStateOf(alarmaId.isNotEmpty())
    }

    LaunchedEffect(alarmaId) {
        if (alarmaId.isNotEmpty()) {
            val alarmaDesactivadaActual = preferencias.getString("alarma_desactivada_id", "") ?: ""
            if (alarmaId == alarmaDesactivadaActual) {
                alarmaActiva = false
                return@LaunchedEffect
            }

            db.collection("alarmas").document(alarmaId).get()
                .addOnSuccessListener { documento ->
                    val estado = documento.getString("estado")
                    if (estado == "ACTIVA" && alarmaId != preferencias.getString("alarma_desactivada_id", "")) {
                        alarmaActiva = true
                    } else {
                        alarmaActiva = false
                    }
                }
                .addOnFailureListener { error ->
                    Log.e("FIREALERT", "Error consultando alarma", error)
                }
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "fire_alert_alarm")
    val escala by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.045f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 850),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alarm_scale"
    )
    // =========================================================
    // FUNCIÓN PARA DISPARAR LA ALARMA
    // =========================================================
    fun dispararAlarma(motivoSeleccionado: String) {
        if (activandoAlarma) return
        activandoAlarma = true

        val usuarioActual = auth.currentUser
        val emailUsuario = usuarioActual?.email ?: "Desconocido"

        db.collection("usuarios")
            .document(usuarioActual?.uid ?: "")
            .get()
            .addOnSuccessListener { doc ->
                val nombreUsuario = doc.getString("nombre")
                    ?: doc.getString("nombreCompleto")
                    ?: emailUsuario

                val nuevaAlarmaRef = db.collection("alarmas").document()
                val idAlarmaGenerada = nuevaAlarmaRef.id

                val datosAlarma = mapOf(
                    "id" to idAlarmaGenerada,
                    "usuario" to nombreUsuario,
                    "email" to emailUsuario,
                    "motivo" to motivoSeleccionado,
                    "estado" to "ACTIVA",
                    "cuartelId" to cuartelEfectivo,
                    "fechaHora" to com.google.firebase.Timestamp.now()
                )

                nuevaAlarmaRef.set(datosAlarma)
                    .addOnSuccessListener {
                        preferencias.edit()
                            .putString("alarma_activa_id", idAlarmaGenerada)
                            .remove("alarma_desactivada_id")
                            .apply()

                        alarmaId = idAlarmaGenerada
                        alarmaActiva = true
                        activandoAlarma = false

                        // Nota: El envío de notificaciones push de servidor (FCM)
                        // debe manejarse desde un backend seguro o Cloud Functions,
                        // ya que quitamos las credenciales del cliente por seguridad.

                        Toast.makeText(context, "¡Alarma enviada con éxito!", Toast.LENGTH_SHORT).show()
                        recargarHistorial++
                    }
                    .addOnFailureListener { e ->
                        activandoAlarma = false
                        Toast.makeText(context, "Error al activar la alarma: ${e.message}", Toast.LENGTH_LONG).show()
                    }
            }
            .addOnFailureListener {
                activandoAlarma = false
                Toast.makeText(context, "Error al obtener datos del usuario", Toast.LENGTH_SHORT).show()
            }
    }

    // =========================================================
    // FUNCIÓN PARA DESACTIVAR Y SILENCIAR LA ALARMA
    // =========================================================
    fun desactivarAlarma() {
        FireAlertAlarmService.detenerAlarmaLocal(context)

        preferencias.edit()
            .putString("alarma_desactivada_id", alarmaId)
            .remove("alarma_activa_id")
            .apply()

        alarmaActiva = false
        Toast.makeText(context, "Alarma desactivada / silenciada", Toast.LENGTH_SHORT).show()

        if (alarmaId.isNotEmpty()) {
            db.collection("alarmas").document(alarmaId)
                .update("estado", "DESACTIVADA")
                .addOnSuccessListener {
                    recargarHistorial++
                }
                .addOnFailureListener { e ->
                    Log.e("FIREALERT", "Error al actualizar estado en Firestore", e)
                }
        }
    }

    // =========================================================
    // DIÁLOGO MOTIVO DE SALIDA
    // =========================================================
    if (mostrarDialogoMotivo) {
        AlertDialog(
            onDismissRequest = { mostrarDialogoMotivo = false },
            containerColor = Color(0xFF15191E),
            title = {
                Text(
                    text = "🚨 SELECCIONE MOTIVO",
                    color = FireRedBright,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    val motivos = listOf(
                        "Incendio Estructural",
                        "Incendio Forestal / Pastizales",
                        "Accidente Vehicular",
                        "Rescate y Salvamento",
                        "Servicio de Prevención",
                        "Otro / Varios"
                    )
                    motivos.forEach { motivo ->
                        Button(
                            onClick = {
                                mostrarDialogoMotivo = false
                                dispararAlarma(motivo)
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF20252B)),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text(text = motivo, color = Color.White, fontSize = 14.sp)
                        }
                    }
                }
            },
            confirmButton = {}
        )
    }

    // =========================================================
    // DISEÑO DE PANTALLA (UI)
    // =========================================================
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(FireDark)
    ) {
        Image(
            painter = painterResource(id = R.drawable.fondo_bomberos),
            contentDescription = "Fondo FireAlert",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.67f))
        )

        if (alarmaActiva) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                FireRed.copy(alpha = 0.18f),
                                Color.Transparent,
                                FireRed.copy(alpha = 0.05f)
                            )
                        )
                    )
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 18.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // =================================================
            // HEADER
            // =================================================
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "FIRE ALERT",
                        color = FireWhite,
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.2.sp
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = "SISTEMA DE ALERTA Y NOTIFICACIÓN",
                        color = FireGray,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(25.dp))

            // =================================================
            // BOTÓN GIGANTE DE ALARMA / DESACTIVAR
            // =================================================
            Box(
                modifier = Modifier
                    .size(200.dp)
                    .scale(if (alarmaActiva) escala else 1f)
                    .background(
                        brush = Brush.radialGradient(
                            colors = if (alarmaActiva) {
                                listOf(FireRedBright, FireRedDark)
                            } else {
                                listOf(FireRedDark, Color(0xFF4A0D0D))
                            }
                        ),
                        shape = CircleShape
                    )
                    .border(
                        width = 4.dp,
                        color = if (alarmaActiva) Color.White else FireRedBright.copy(alpha = 0.5f),
                        shape = CircleShape
                    )
                    .clickable {
                        if (alarmaActiva) {
                            desactivarAlarma()
                        } else {
                            val esRolOperativo = esCuartelero || esAdministrador || esAdministradorGeneral

                            if (!esRolOperativo) {
                                Toast.makeText(
                                    context,
                                    "⛔ Solo los Cuarteleros o Administradores pueden activar la alarma",
                                    Toast.LENGTH_LONG
                                ).show()
                            } else if (comprobarPermisoAlarma()) {
                                mostrarDialogoMotivo = true
                            }
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = if (alarmaActiva) "🔕" else "🚨",
                        fontSize = 38.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = if (alarmaActiva) "APAGAR\nALARMA" else "ACTIVAR\nALARMA",
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        letterSpacing = 1.1.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(30.dp))

            // =================================================
            // SECCIÓN HISTORIAL DE ALARMAS
            // =================================================
            if (puedeVerHistorial) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = FireCard)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "📋 HISTORIAL DE ALARMAS",
                                color = FireWhite,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.1.sp
                            )

                            if (puedeBorrarHistorial && historialAlarmas.isNotEmpty()) {
                                TextButton(onClick = { mostrarConfirmacionBorrado = true }) {
                                    Text(
                                        text = "BORRAR",
                                        color = FireRedBright,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        if (cargandoHistorial) {
                            Text(
                                text = "Cargando historial...",
                                color = FireGray,
                                fontSize = 13.sp,
                                modifier = Modifier.padding(vertical = 10.dp)
                            )
                        } else if (historialAlarmas.isEmpty()) {
                            Text(
                                text = "No hay registros de alarmas recientes.",
                                color = FireGray,
                                fontSize = 13.sp,
                                modifier = Modifier.padding(vertical = 10.dp)
                            )
                        } else {
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                historialAlarmas.take(5).forEach { item ->
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(10.dp),
                                        colors = CardDefaults.cardColors(containerColor = FireCardLight)
                                    ) {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(12.dp)
                                        ) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Text(
                                                    text = item.motivo,
                                                    color = Color.White,
                                                    fontSize = 14.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                                Text(
                                                    text = item.fechaHora,
                                                    color = FireGray,
                                                    fontSize = 11.sp
                                                )
                                            }
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = "Activó: ${item.usuario}",
                                                color = FireGray,
                                                fontSize = 12.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(25.dp))
            }

            // =================================================
            // BOTONES DE ADMINISTRACIÓN Y ACCESOS
            // =================================================
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (esAdministrador) {
                    Button(
                        onClick = onAbrirAdministracion,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = FireCardLight)
                    ) {
                        Text(
                            text = "⚙ PANEL DE ADMINISTRACIÓN",
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                if (esAdministradorGeneral) {
                    Button(
                        onClick = onAbrirPanelAdministradorGeneral,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E2630))
                    ) {
                        Text(
                            text = "🌐 PANEL ADMINISTRADOR GENERAL",
                            color = FireRedBright,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Button(
                    onClick = {
                        preferencias.edit().clear().apply()
                        auth.signOut()
                        onCerrarSesion()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF301515))
                ) {
                    Text(
                        text = "🚪 CERRAR SESIÓN",
                        color = FireRedBright,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }

    // =========================================================
    // DIÁLOGO BLOQUEANTE DE ACTUALIZACIÓN
    // =========================================================
    if (mostrarDialogoActualizacion) {
        AlertDialog(
            onDismissRequest = { /* Vacío para evitar que lo cierren tocando afuera */ },
            containerColor = Color(0xFF15191E),
            title = {
                Text(
                    text = "🚀 NUEVA VERSIÓN DISPONIBLE",
                    color = FireRedBright,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "Se requiere actualizar la aplicación a la versión 2.1 para continuar recibiendo las alertas y avisos del cuartel.",
                    color = FireWhite,
                    fontSize = 14.sp,
                    lineHeight = 21.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (urlDescargaApk.isNotEmpty()) {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(urlDescargaApk))
                            context.startActivity(intent)
                        } else {
                            Toast.makeText(context, "URL de descarga no disponible", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = FireRedBright)
                ) {
                    Text(
                        text = "ACTUALIZAR AHORA",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        )
    }
}