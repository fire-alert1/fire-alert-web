package com.example.sab.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import com.example.sab.AlarmaRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

import java.text.SimpleDateFormat
import java.util.Locale


// =============================================================
// COLORES FIRE ALERT
// =============================================================

private val FireRed =
    Color(0xFFE53935)

private val FireDark =
    Color(0xFF101214)

private val FireWhite =
    Color(0xFFF5F5F5)

private val FireGray =
    Color(0xFFB7BDC2)

private val FireCard =
    Color(0xD9111417)


// =============================================================
// MODELO
// =============================================================

data class RegistroAlarma(

    val id: String,

    val fechaHora: com.google.firebase.Timestamp?
)


// =============================================================
// HISTORIAL DE ALARMAS
// =============================================================

@Composable
fun HistorialScreen(

    // =========================================================
    // CUARTEL ACTUAL
    // =========================================================

    cuartelId: String?,

    // =========================================================
    // VOLVER
    // =========================================================

    onVolver: () -> Unit
) {

    // =========================================================
    // ESTADOS
    // =========================================================

    var registros by remember {

        mutableStateOf<List<RegistroAlarma>>(
            emptyList()
        )
    }


    var mensaje by remember {

        mutableStateOf("")
    }


    var esAdministrador by remember {

        mutableStateOf(false)
    }


    var mostrarConfirmacion by remember {

        mutableStateOf(false)
    }


    var eliminando by remember {

        mutableStateOf(false)
    }


    // =========================================================
    // FIREBASE
    // =========================================================

    val db =
        remember {
            FirebaseFirestore.getInstance()
        }


    val auth =
        remember {
            FirebaseAuth.getInstance()
        }


    // =========================================================
    // REPOSITORY
    // =========================================================

    val alarmaRepository =
        remember {
            AlarmaRepository()
        }


    // =========================================================
    // COMPROBAR ADMINISTRADOR
    // =========================================================

    LaunchedEffect(Unit) {

        val usuario =
            auth.currentUser


        if (usuario == null) {

            esAdministrador =
                false

            return@LaunchedEffect
        }


        db
            .collection("usuarios")
            .document(usuario.uid)
            .get()

            .addOnSuccessListener { documento ->

                if (!documento.exists()) {

                    esAdministrador =
                        false

                    return@addOnSuccessListener
                }


                // =================================================
                // LEER ARRAY DE ROLES
                // =================================================

                val rolesFirestore =
                    documento.get("roles")


                val roles =

                    if (
                        rolesFirestore is List<*>
                    ) {

                        rolesFirestore
                            .filterIsInstance<String>()
                            .map {
                                it.trim().lowercase()
                            }

                    } else {

                        emptyList()
                    }


                // =================================================
                // ROL ANTIGUO
                // =================================================

                val rolAntiguo =
                    documento
                        .getString("rol")
                        ?.trim()
                        ?.lowercase()


                // =================================================
                // ADMINISTRADOR
                // =================================================

                esAdministrador =
                    roles.contains("administrador") ||
                            rolAntiguo == "administrador"
            }
    }


    // =========================================================
    // ESCUCHAR HISTORIAL
    // =========================================================

    DisposableEffect(
        cuartelId
    ) {

        // =========================================================
        // SI NO HAY CUARTEL
        // =========================================================

        if (
            cuartelId.isNullOrBlank()
        ) {

            registros =
                emptyList()

            mensaje =
                "No se encontró el cuartel."

            onDispose { }
        }


        // =========================================================
        // ESCUCHAR SOLO EL CUARTEL ACTUAL
        // =========================================================

        val listener =

            if (
                !cuartelId.isNullOrBlank()
            ) {

                db
                    .collection("alarmas")
                    .whereEqualTo(
                        "cuartelId",
                        cuartelId
                    )
                    .addSnapshotListener {

                            snapshot,
                            error ->

                        // =========================================
                        // ERROR
                        // =========================================

                        if (
                            error != null
                        ) {

                            mensaje =
                                "ERROR FIREBASE: ${error.message}"

                            android.util.Log.e(
                                "FIREALERT_HISTORIAL",
                                "Error leyendo alarmas",
                                error
                            )

                            return@addSnapshotListener
                        }


                        // =========================================
                        // SNAPSHOT NULL
                        // =========================================

                        if (
                            snapshot == null
                        ) {

                            mensaje =
                                "Firebase devolvió datos vacíos."

                            return@addSnapshotListener
                        }


                        // =========================================
                        // LOG
                        // =========================================

                        android.util.Log.d(
                            "FIREALERT_HISTORIAL",
                            "CUARTEL: $cuartelId"
                        )


                        android.util.Log.d(
                            "FIREALERT_HISTORIAL",
                            "DOCUMENTOS RECIBIDOS: ${snapshot.documents.size}"
                        )


                        // =========================================
                        // CONVERTIR
                        // =========================================

                        registros =
                            snapshot.documents

                                .mapNotNull { documento ->

                                    val fecha =
                                        documento
                                            .getTimestamp(
                                                "fechaHora"
                                            )


                                    android.util.Log.d(
                                        "FIREALERT_HISTORIAL",
                                        "Documento ${documento.id} - fechaHora=$fecha"
                                    )


                                    if (
                                        fecha == null
                                    ) {

                                        null

                                    } else {

                                        RegistroAlarma(

                                            id =
                                                documento.id,

                                            fechaHora =
                                                fecha
                                        )
                                    }
                                }

                                .sortedByDescending {

                                    it.fechaHora
                                        ?.toDate()
                                }


                        // =========================================
                        // MENSAJE
                        // =========================================

                        mensaje =
                            if (
                                snapshot.documents.isEmpty()
                            ) {

                                ""

                            } else if (
                                registros.isEmpty()
                            ) {

                                "Las alarmas encontradas no tienen fecha."

                            } else {

                                ""
                            }
                    }

            } else {

                null
            }


        // =========================================================
        // LIBERAR LISTENER
        // =========================================================

        onDispose {

            listener?.remove()
        }
    }


    // =========================================================
    // ELIMINAR TODO EL HISTORIAL DEL CUARTEL
    // =========================================================

    fun eliminarTodoElHistorial() {

        // =========================================================
        // SEGURIDAD LOCAL
        // =========================================================

        if (
            registros.isEmpty() ||
            !esAdministrador ||
            cuartelId.isNullOrBlank()
        ) {

            mostrarConfirmacion =
                false

            return
        }


        eliminando =
            true


        mensaje =
            ""


        // =========================================================
        // BUSCAR SOLO ALARMAS DEL CUARTEL
        // =========================================================

        db
            .collection("alarmas")
            .whereEqualTo(
                "cuartelId",
                cuartelId
            )
            .get()

            .addOnSuccessListener { snapshot ->

                // =================================================
                // NADA PARA BORRAR
                // =================================================

                if (
                    snapshot.isEmpty
                ) {

                    eliminando =
                        false

                    mostrarConfirmacion =
                        false

                    registros =
                        emptyList()

                    return@addOnSuccessListener
                }


                // =================================================
                // BATCH
                // =================================================

                val batch =
                    db.batch()


                snapshot.documents.forEach { documento ->

                    batch.delete(
                        documento.reference
                    )
                }


                // =================================================
                // EJECUTAR
                // =================================================

                batch
                    .commit()

                    .addOnSuccessListener {

                        eliminando =
                            false

                        mostrarConfirmacion =
                            false

                        registros =
                            emptyList()

                        mensaje =
                            ""

                        android.util.Log.d(
                            "FIREALERT_HISTORIAL",
                            "Historial del cuartel $cuartelId eliminado correctamente"
                        )
                    }

                    .addOnFailureListener { error ->

                        eliminando =
                            false

                        mostrarConfirmacion =
                            false

                        mensaje =
                            "No se pudo limpiar el historial: ${error.message}"

                        android.util.Log.e(
                            "FIREALERT_HISTORIAL",
                            "Error eliminando historial",
                            error
                        )
                    }
            }

            .addOnFailureListener { error ->

                eliminando =
                    false

                mostrarConfirmacion =
                    false

                mensaje =
                    "No se pudo acceder al historial: ${error.message}"

                android.util.Log.e(
                    "FIREALERT_HISTORIAL",
                    "Error buscando historial",
                    error
                )
            }
    }


    // =========================================================
    // PANTALLA
    // =========================================================

    Box(

        modifier =
            Modifier
                .fillMaxSize()
                .background(
                    FireDark
                )
    ) {

        Column(

            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(
                        horizontal = 20.dp,
                        vertical = 16.dp
                    )
        ) {


            // =================================================
            // BARRA SUPERIOR
            // =================================================

            Row(

                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(48.dp),

                verticalAlignment =
                    Alignment.CenterVertically
            ) {


                // =================================================
                // FLECHA
                // =================================================

                Text(

                    text =
                        "←",

                    color =
                        FireWhite,

                    fontSize =
                        32.sp,

                    fontWeight =
                        FontWeight.Normal,

                    modifier =
                        Modifier
                            .size(44.dp)
                            .clickable {

                                onVolver()
                            }
                )


                Spacer(
                    modifier =
                        Modifier.size(4.dp)
                )


                Column {

                    Text(

                        text =
                            "FIRE ALERT",

                        color =
                            FireWhite,

                        fontSize =
                            25.sp,

                        fontWeight =
                            FontWeight.Bold,

                        letterSpacing =
                            1.5.sp
                    )


                    Text(

                        text =
                            "HISTORIAL DE ALARMAS",

                        color =
                            FireGray,

                        fontSize =
                            10.sp,

                        fontWeight =
                            FontWeight.Medium,

                        letterSpacing =
                            1.sp
                    )
                }
            }


            Spacer(
                modifier =
                    Modifier.height(
                        16.dp
                    )
            )


            // =================================================
            // RESUMEN
            // =================================================

            Card(

                modifier =
                    Modifier.fillMaxWidth(),

                shape =
                    RoundedCornerShape(
                        20.dp
                    ),

                colors =
                    CardDefaults.cardColors(
                        containerColor =
                            FireCard
                    )
            ) {

                Row(

                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(
                                18.dp
                            ),

                    verticalAlignment =
                        Alignment.CenterVertically
                ) {

                    Box(

                        modifier =
                            Modifier
                                .size(48.dp)
                                .background(
                                    FireRed.copy(
                                        alpha =
                                            0.14f
                                    ),
                                    CircleShape
                                ),

                        contentAlignment =
                            Alignment.Center
                    ) {

                        Text(

                            text =
                                "📢",

                            fontSize =
                                22.sp
                        )
                    }


                    Spacer(
                        modifier =
                            Modifier.size(
                                14.dp
                            )
                    )


                    Column {

                        Text(

                            text =
                                "REGISTRO DE ACTIVACIONES",

                            color =
                                FireGray,

                            fontSize =
                                10.sp,

                            fontWeight =
                                FontWeight.Bold,

                            letterSpacing =
                                1.sp
                        )


                        Spacer(
                            modifier =
                                Modifier.height(
                                    3.dp
                                )
                        )


                        Text(

                            text =
                                "${registros.size} alarmas registradas",

                            color =
                                FireWhite,

                            fontSize =
                                18.sp,

                            fontWeight =
                                FontWeight.Bold
                        )
                    }
                }
            }


            Spacer(
                modifier =
                    Modifier.height(
                        12.dp
                    )
            )


            // =================================================
            // MENSAJE
            // =================================================

            if (
                mensaje.isNotEmpty()
            ) {

                Text(

                    text =
                        mensaje,

                    color =
                        FireRed,

                    fontSize =
                        13.sp,

                    fontWeight =
                        FontWeight.Bold,

                    modifier =
                        Modifier.padding(
                            vertical =
                                8.dp
                        )
                )
            }


            // =================================================
            // SIN REGISTROS
            // =================================================

            if (
                mensaje.isEmpty() &&
                registros.isEmpty()
            ) {

                Box(

                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .weight(
                                1f
                            ),

                    contentAlignment =
                        Alignment.Center
                ) {

                    Text(

                        text =
                            "Todavía no hay alarmas registradas",

                        color =
                            FireGray,

                        fontSize =
                            14.sp
                    )
                }
            }


            // =================================================
            // LISTA
            // =================================================

            else if (
                registros.isNotEmpty()
            ) {

                LazyColumn(

                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .weight(
                                1f
                            ),

                    verticalArrangement =
                        Arrangement.spacedBy(
                            8.dp
                        )
                ) {

                    items(

                        items =
                            registros,

                        key = {
                            it.id
                        }

                    ) { registro ->

                        RegistroAlarmaCard(
                            registro =
                                registro
                        )
                    }
                }


                // =================================================
                // BOTÓN BORRAR
                // SOLO ADMINISTRADOR
                // =================================================

                if (
                    esAdministrador
                ) {

                    Spacer(
                        modifier =
                            Modifier.height(
                                12.dp
                            )
                    )


                    Button(

                        onClick = {

                            mostrarConfirmacion =
                                true
                        },

                        enabled =
                            !eliminando,

                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .height(
                                    50.dp
                                ),

                        shape =
                            RoundedCornerShape(
                                14.dp
                            ),

                        colors =
                            ButtonDefaults.buttonColors(

                                containerColor =
                                    Color(0xFF651717),

                                contentColor =
                                    Color.White
                            )
                    ) {

                        Text(

                            text =
                                if (
                                    eliminando
                                ) {

                                    "ELIMINANDO..."

                                } else {

                                    "🗑  LIMPIAR HISTORIAL"
                                },

                            fontSize =
                                13.sp,

                            fontWeight =
                                FontWeight.Bold,

                            letterSpacing =
                                0.8.sp
                        )
                    }
                }
            }
        }


        // =========================================================
        // CONFIRMACIÓN
        // =========================================================

        if (
            mostrarConfirmacion
        ) {

            AlertDialog(

                onDismissRequest = {

                    if (
                        !eliminando
                    ) {

                        mostrarConfirmacion =
                            false
                    }
                },

                title = {

                    Text(

                        text =
                            "¿Limpiar historial?",

                        fontWeight =
                            FontWeight.Bold
                    )
                },

                text = {

                    Text(

                        text =
                            "Se eliminarán las ${registros.size} alarmas registradas de este cuartel.\n\nEsta acción no se puede deshacer."
                    )
                },

                confirmButton = {

                    TextButton(

                        onClick = {

                            eliminarTodoElHistorial()
                        },

                        enabled =
                            !eliminando
                    ) {

                        Text(

                            text =
                                "ELIMINAR",

                            color =
                                FireRed,

                            fontWeight =
                                FontWeight.Bold
                        )
                    }
                },

                dismissButton = {

                    TextButton(

                        onClick = {

                            mostrarConfirmacion =
                                false
                        },

                        enabled =
                            !eliminando
                    ) {

                        Text(
                            text =
                                "CANCELAR"
                        )
                    }
                }
            )
        }
    }
}


// =============================================================
// TARJETA DEL HISTORIAL
// =============================================================

@Composable
private fun RegistroAlarmaCard(

    registro: RegistroAlarma

) {

    // =========================================================
    // FECHA
    // =========================================================

    val fecha =
        registro.fechaHora?.let {

            SimpleDateFormat(
                "dd/MM/yyyy",
                Locale.getDefault()
            ).format(
                it.toDate()
            )

        } ?: "--/--/----"


    // =========================================================
    // HORA
    // =========================================================

    val hora =
        registro.fechaHora?.let {

            SimpleDateFormat(
                "HH:mm",
                Locale.getDefault()
            ).format(
                it.toDate()
            )

        } ?: "--:--"


    // =========================================================
    // TARJETA
    // =========================================================

    Card(

        modifier =
            Modifier.fillMaxWidth(),

        shape =
            RoundedCornerShape(
                16.dp
            ),

        colors =
            CardDefaults.cardColors(
                containerColor =
                    FireCard
            )
    ) {

        Row(

            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = 18.dp,
                        vertical = 16.dp
                    ),

            verticalAlignment =
                Alignment.CenterVertically
        ) {


            // =================================================
            // INDICADOR
            // =================================================

            Box(

                modifier =
                    Modifier
                        .size(10.dp)
                        .background(
                            FireRed,
                            CircleShape
                        )
            )


            Spacer(
                modifier =
                    Modifier.size(
                        14.dp
                    )
            )


            Column {

                Text(

                    text =
                        "ALARMA ACTIVADA",

                    color =
                        FireGray,

                    fontSize =
                        10.sp,

                    fontWeight =
                        FontWeight.Bold,

                    letterSpacing =
                        1.sp
                )


                Spacer(
                    modifier =
                        Modifier.height(
                            4.dp
                        )
                )


                Text(

                    text =
                        fecha,

                    color =
                        FireWhite,

                    fontSize =
                        17.sp,

                    fontWeight =
                        FontWeight.SemiBold
                )


                Spacer(
                    modifier =
                        Modifier.height(
                            1.dp
                        )
                )


                Text(

                    text =
                        hora,

                    color =
                        FireRed,

                    fontSize =
                        23.sp,

                    fontWeight =
                        FontWeight.Bold
                )
            }
        }
    }
}