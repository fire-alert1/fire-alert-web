package com.example.sab.ui

import android.widget.Toast

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import com.google.firebase.firestore.FirebaseFirestore


// =============================================================
// MODELO DE CUARTEL
// =============================================================

data class CuartelGeneral(

    val id: String = "",

    val nombre: String = "",

    val localidad: String = "",

    val provincia: String = "",

    val pais: String = "",

    val activo: Boolean = false,

    val plan: String = "",

    val estadoSuscripcion: String = "",

    val administradorUid: String = ""
)


// =============================================================
// ADMINISTRADOR GENERAL
// =============================================================

@Composable
fun AdminGeneralScreen(
    onVolver: (() -> Unit)? = null
) {

    val context =
        androidx.compose.ui.platform.LocalContext.current

    val db =
        FirebaseFirestore.getInstance()


    // =========================================================
    // ESTADO
    // =========================================================

    var cuarteles by remember {

        mutableStateOf(
            emptyList<CuartelGeneral>()
        )
    }


    var cargando by remember {

        mutableStateOf(
            true
        )
    }


    var guardandoId by remember {

        mutableStateOf<String?>(null)
    }


    // =========================================================
    // OPCIONES COMERCIALES
    // =========================================================

    val planes =
        listOf(
            "prueba",
            "basico",
            "profesional"
        )


    val estadosSuscripcion =
        listOf(
            "pendiente",
            "activa",
            "vencida",
            "suspendida"
        )


    // =========================================================
    // CARGAR CUARTELES
    // =========================================================

    fun cargarCuarteles() {

        cargando = true

        db.collection("cuarteles")
            .get()

            .addOnSuccessListener { resultado ->

                cuarteles =
                    resultado.documents.map { documento ->

                        CuartelGeneral(

                            id =
                                documento.id,

                            nombre =
                                documento.getString(
                                    "nombre"
                                ) ?: "Sin nombre",

                            localidad =
                                documento.getString(
                                    "localidad"
                                ) ?: "",

                            provincia =
                                documento.getString(
                                    "provincia"
                                ) ?: "",

                            pais =
                                documento.getString(
                                    "pais"
                                ) ?: "",

                            activo =
                                documento.getBoolean(
                                    "activo"
                                ) ?: false,

                            plan =
                                documento.getString(
                                    "plan"
                                ) ?: "prueba",

                            estadoSuscripcion =
                                documento.getString(
                                    "estadoSuscripcion"
                                ) ?: "pendiente",

                            administradorUid =
                                documento.getString(
                                    "administradorUid"
                                )
                                    ?: documento.getString(
                                        "creadoPor"
                                    )
                                    ?: ""
                        )
                    }

                cargando = false
            }

            .addOnFailureListener { error ->

                cargando = false

                Toast.makeText(

                    context,

                    "No se pudieron cargar los cuarteles: ${
                        error.message ?: "Error desconocido"
                    }",

                    Toast.LENGTH_LONG

                ).show()
            }
    }


    // =========================================================
    // CARGA INICIAL
    // =========================================================

    LaunchedEffect(Unit) {

        cargarCuarteles()
    }


    // =========================================================
    // FUNCIÓN ACTIVAR / DESACTIVAR
    // =========================================================

    fun cambiarActivo(
        cuartel: CuartelGeneral
    ) {

        if (
            guardandoId != null
        ) {
            return
        }

        guardandoId =
            cuartel.id

        val nuevoEstado =
            !cuartel.activo

        db.collection("cuarteles")
            .document(cuartel.id)
            .update(
                "activo",
                nuevoEstado
            )
            .addOnSuccessListener {

                cuarteles =
                    cuarteles.map {

                        if (
                            it.id ==
                            cuartel.id
                        ) {

                            it.copy(
                                activo =
                                    nuevoEstado
                            )

                        } else {

                            it
                        }
                    }

                Toast.makeText(

                    context,

                    if (
                        nuevoEstado
                    ) {
                        "Cuartel activado correctamente"
                    } else {
                        "Cuartel desactivado correctamente"
                    },

                    Toast.LENGTH_SHORT

                ).show()

                guardandoId =
                    null
            }
            .addOnFailureListener { error ->

                Toast.makeText(

                    context,

                    "No se pudo modificar el cuartel: ${
                        error.message ?: "Error desconocido"
                    }",

                    Toast.LENGTH_LONG

                ).show()

                guardandoId =
                    null
            }
    }


    // =========================================================
    // CAMBIAR PLAN
    // =========================================================

    fun cambiarPlan(
        cuartel: CuartelGeneral,
        nuevoPlan: String
    ) {

        if (
            guardandoId != null
        ) {
            return
        }

        guardandoId =
            cuartel.id

        db.collection("cuarteles")
            .document(cuartel.id)
            .update(
                "plan",
                nuevoPlan
            )
            .addOnSuccessListener {

                cuarteles =
                    cuarteles.map {

                        if (
                            it.id ==
                            cuartel.id
                        ) {

                            it.copy(
                                plan =
                                    nuevoPlan
                            )

                        } else {

                            it
                        }
                    }

                Toast.makeText(

                    context,

                    "Plan actualizado a ${nuevoPlan.replaceFirstChar { it.uppercase() }}",

                    Toast.LENGTH_SHORT

                ).show()

                guardandoId =
                    null
            }
            .addOnFailureListener { error ->

                Toast.makeText(

                    context,

                    "No se pudo actualizar el plan: ${
                        error.message ?: "Error desconocido"
                    }",

                    Toast.LENGTH_LONG

                ).show()

                guardandoId =
                    null
            }
    }


    // =========================================================
    // CAMBIAR ESTADO DE SUSCRIPCIÓN
    // =========================================================

    fun cambiarEstadoSuscripcion(
        cuartel: CuartelGeneral,
        nuevoEstado: String
    ) {

        if (
            guardandoId != null
        ) {
            return
        }

        guardandoId =
            cuartel.id

        db.collection("cuarteles")
            .document(cuartel.id)
            .update(
                "estadoSuscripcion",
                nuevoEstado
            )
            .addOnSuccessListener {

                cuarteles =
                    cuarteles.map {

                        if (
                            it.id ==
                            cuartel.id
                        ) {

                            it.copy(
                                estadoSuscripcion =
                                    nuevoEstado
                            )

                        } else {

                            it
                        }
                    }

                Toast.makeText(

                    context,

                    "Suscripción actualizada a ${nuevoEstado.replaceFirstChar { it.uppercase() }}",

                    Toast.LENGTH_SHORT

                ).show()

                guardandoId =
                    null
            }
            .addOnFailureListener { error ->

                Toast.makeText(

                    context,

                    "No se pudo actualizar la suscripción: ${
                        error.message ?: "Error desconocido"
                    }",

                    Toast.LENGTH_LONG

                ).show()

                guardandoId =
                    null
            }
    }


    // =========================================================
    // FONDO
    // =========================================================

    Box(

        modifier =
            Modifier
                .fillMaxSize()
                .background(
                    Color(0xFF05070A)
                )
    ) {


        // =====================================================
        // CARGANDO
        // =====================================================

        if (
            cargando
        ) {

            Column(

                modifier =
                    Modifier.fillMaxSize(),

                horizontalAlignment =
                    Alignment.CenterHorizontally,

                verticalArrangement =
                    Arrangement.Center
            ) {

                Text(

                    text =
                        "CARGANDO CUARTELES...",

                    color =
                        Color(0xFF9EA1A5),

                    fontSize =
                        13.sp
                )
            }

            return@Box
        }


        // =====================================================
        // CONTENIDO
        // =====================================================

        Column(

            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(
                        horizontal = 20.dp,
                        vertical = 24.dp
                    )
        ) {


            // =================================================
            // ENCABEZADO
            // =================================================

            Row(

                modifier =
                    Modifier.fillMaxWidth(),

                verticalAlignment =
                    Alignment.CenterVertically
            ) {

                Column(

                    modifier =
                        Modifier.weight(
                            1f
                        )
                ) {

                    Text(

                        text =
                            "ADMINISTRACIÓN GENERAL",

                        color =
                            Color.White,

                        fontSize =
                            24.sp,

                        fontWeight =
                            FontWeight.Bold,

                        letterSpacing =
                            1.5.sp
                    )

                    Spacer(
                        modifier =
                            Modifier.height(
                                5.dp
                            )
                    )

                    Text(

                        text =
                            "Gestión general de Fire Alert",

                        color =
                            Color(0xFF9EA1A5),

                        fontSize =
                            13.sp
                    )
                }
            }


            // =================================================
            // VOLVER
            // =================================================

            if (
                onVolver != null
            ) {

                Spacer(
                    modifier =
                        Modifier.height(
                            8.dp
                        )
                )

                TextButton(

                    onClick =
                        onVolver
                ) {

                    Text(

                        text =
                            "← Volver",

                        color =
                            Color(0xFFB7B9BD),

                        fontSize =
                            14.sp
                    )
                }
            }


            Spacer(
                modifier =
                    Modifier.height(
                        18.dp
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
                        18.dp
                    ),

                colors =
                    CardDefaults.cardColors(

                        containerColor =
                            Color(
                                0xFF15191E
                            )
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

                    horizontalArrangement =
                        Arrangement.SpaceBetween,

                    verticalAlignment =
                        Alignment.CenterVertically
                ) {

                    Column {

                        Text(

                            text =
                                "CUARTELES REGISTRADOS",

                            color =
                                Color(0xFF8E9297),

                            fontSize =
                                9.sp,

                            fontWeight =
                                FontWeight.Bold,

                            letterSpacing =
                                1.2.sp
                        )

                        Spacer(
                            modifier =
                                Modifier.height(
                                    4.dp
                                )
                        )

                        Text(

                            text =
                                cuarteles.size.toString(),

                            color =
                                Color.White,

                            fontSize =
                                25.sp,

                            fontWeight =
                                FontWeight.Bold
                        )
                    }


                    Column(

                        horizontalAlignment =
                            Alignment.End
                    ) {

                        val activos =
                            cuarteles.count {
                                it.activo
                            }

                        Text(

                            text =
                                "ACTIVOS",

                            color =
                                Color(0xFF8E9297),

                            fontSize =
                                9.sp,

                            fontWeight =
                                FontWeight.Bold,

                            letterSpacing =
                                1.2.sp
                        )

                        Spacer(
                            modifier =
                                Modifier.height(
                                    4.dp
                                )
                        )

                        Text(

                            text =
                                activos.toString(),

                            color =
                                Color(0xFF42C978),

                            fontSize =
                                20.sp,

                            fontWeight =
                                FontWeight.Bold
                        )
                    }
                }
            }


            Spacer(
                modifier =
                    Modifier.height(
                        18.dp
                    )
            )


            // =================================================
            // SIN CUARTELES
            // =================================================

            if (
                cuarteles.isEmpty()
            ) {

                Column(

                    modifier =
                        Modifier.fillMaxWidth(),

                    horizontalAlignment =
                        Alignment.CenterHorizontally
                ) {

                    Spacer(
                        modifier =
                            Modifier.height(
                                60.dp
                            )
                    )

                    Text(

                        text =
                            "🏠",

                        fontSize =
                            42.sp
                    )

                    Spacer(
                        modifier =
                            Modifier.height(
                                12.dp
                            )
                    )

                    Text(

                        text =
                            "No hay cuarteles registrados",

                        color =
                            Color.White,

                        fontSize =
                            16.sp,

                        fontWeight =
                            FontWeight.Medium
                    )

                    Spacer(
                        modifier =
                            Modifier.height(
                                6.dp
                            )
                    )

                    Text(

                        text =
                            "Los cuarteles registrados aparecerán aquí.",

                        color =
                            Color(0xFF8E9297),

                        fontSize =
                            13.sp
                    )
                }

            } else {


                // =================================================
                // LISTA
                // =================================================

                LazyColumn(

                    modifier =
                        Modifier.fillMaxSize(),

                    verticalArrangement =
                        Arrangement.spacedBy(
                            12.dp
                        )
                ) {

                    items(
                        cuarteles
                    ) { cuartel ->


                        val guardando =
                            guardandoId ==
                                    cuartel.id


                        // =========================================
                        // TARJETA
                        // =========================================

                        Card(

                            modifier =
                                Modifier.fillMaxWidth(),

                            shape =
                                RoundedCornerShape(
                                    18.dp
                                ),

                            colors =
                                CardDefaults.cardColors(

                                    containerColor =
                                        Color(
                                            0xFF15191E
                                        )
                                )
                        ) {

                            Column(

                                modifier =
                                    Modifier
                                        .fillMaxWidth()
                                        .padding(
                                            18.dp
                                        )
                            ) {


                                // =================================
                                // NOMBRE + ESTADO
                                // =================================

                                Row(

                                    modifier =
                                        Modifier.fillMaxWidth(),

                                    verticalAlignment =
                                        Alignment.CenterVertically
                                ) {

                                    Column(

                                        modifier =
                                            Modifier.weight(
                                                1f
                                            )
                                    ) {

                                        Text(

                                            text =
                                                "🏠 ${cuartel.nombre}",

                                            color =
                                                Color.White,

                                            fontSize =
                                                18.sp,

                                            fontWeight =
                                                FontWeight.Bold
                                        )

                                        Spacer(
                                            modifier =
                                                Modifier.height(
                                                    5.dp
                                                )
                                        )

                                        Text(

                                            text =
                                                "ID: ${cuartel.id}",

                                            color =
                                                Color(0xFF697177),

                                            fontSize =
                                                9.sp
                                        )
                                    }


                                    Box(

                                        modifier =
                                            Modifier
                                                .background(

                                                    if (
                                                        cuartel.activo
                                                    ) {
                                                        Color(
                                                            0xFF176E43
                                                        )
                                                    } else {
                                                        Color(
                                                            0xFF5A1717
                                                        )
                                                    },

                                                    RoundedCornerShape(
                                                        50.dp
                                                    )
                                                )
                                                .padding(
                                                    horizontal =
                                                        10.dp,
                                                    vertical =
                                                        6.dp
                                                )
                                    ) {

                                        Text(

                                            text =
                                                if (
                                                    cuartel.activo
                                                ) {
                                                    "ACTIVO"
                                                } else {
                                                    "INACTIVO"
                                                },

                                            color =
                                                Color.White,

                                            fontSize =
                                                9.sp,

                                            fontWeight =
                                                FontWeight.Bold,

                                            letterSpacing =
                                                0.8.sp
                                        )
                                    }
                                }


                                Spacer(
                                    modifier =
                                        Modifier.height(
                                            14.dp
                                        )
                                )


                                // =================================
                                // UBICACIÓN
                                // =================================

                                val ubicacion =
                                    listOf(

                                        cuartel.localidad,

                                        cuartel.provincia

                                    )
                                        .filter {
                                            it.isNotBlank()
                                        }
                                        .joinToString(
                                            ", "
                                        )


                                if (
                                    ubicacion.isNotBlank()
                                ) {

                                    Text(

                                        text =
                                            "📍 $ubicacion",

                                        color =
                                            Color(0xFFB7B9BD),

                                        fontSize =
                                            13.sp
                                    )

                                    Spacer(
                                        modifier =
                                            Modifier.height(
                                                5.dp
                                            )
                                    )
                                }


                                // =================================
                                // PAÍS
                                // =================================

                                if (
                                    cuartel.pais.isNotBlank()
                                ) {

                                    Text(

                                        text =
                                            "🌎 ${cuartel.pais}",

                                        color =
                                            Color(0xFFB7B9BD),

                                        fontSize =
                                            13.sp
                                    )
                                }


                                Spacer(
                                    modifier =
                                        Modifier.height(
                                            14.dp
                                        )
                                )


                                // =================================
                                // PLAN
                                // =================================

                                Text(

                                    text =
                                        "PLAN",

                                    color =
                                        Color(0xFF8E9297),

                                    fontSize =
                                        9.sp,

                                    fontWeight =
                                        FontWeight.Bold,

                                    letterSpacing =
                                        1.1.sp
                                )

                                Spacer(
                                    modifier =
                                        Modifier.height(
                                            5.dp
                                        )
                                )


                                SelectorFirestore(

                                    valor =
                                        cuartel.plan.ifBlank {
                                            "prueba"
                                        },

                                    opciones =
                                        planes,

                                    enabled =
                                        !guardando,

                                    onSeleccionar = { nuevoPlan ->

                                        cambiarPlan(
                                            cuartel,
                                            nuevoPlan
                                        )
                                    }
                                )


                                Spacer(
                                    modifier =
                                        Modifier.height(
                                            13.dp
                                        )
                                )


                                // =================================
                                // ESTADO SUSCRIPCIÓN
                                // =================================

                                Text(

                                    text =
                                        "ESTADO DE SUSCRIPCIÓN",

                                    color =
                                        Color(0xFF8E9297),

                                    fontSize =
                                        9.sp,

                                    fontWeight =
                                        FontWeight.Bold,

                                    letterSpacing =
                                        1.1.sp
                                )

                                Spacer(
                                    modifier =
                                        Modifier.height(
                                            5.dp
                                        )
                                )


                                SelectorFirestore(

                                    valor =
                                        cuartel.estadoSuscripcion.ifBlank {
                                            "pendiente"
                                        },

                                    opciones =
                                        estadosSuscripcion,

                                    enabled =
                                        !guardando,

                                    onSeleccionar = { nuevoEstado ->

                                        cambiarEstadoSuscripcion(
                                            cuartel,
                                            nuevoEstado
                                        )
                                    }
                                )


                                Spacer(
                                    modifier =
                                        Modifier.height(
                                            14.dp
                                        )
                                )


                                // =================================
                                // ADMINISTRADOR
                                // =================================

                                if (
                                    cuartel.administradorUid
                                        .isNotBlank()
                                ) {

                                    Text(

                                        text =
                                            "👤 Administrador asignado",

                                        color =
                                            Color(0xFFB7B9BD),

                                        fontSize =
                                            12.sp
                                    )

                                    Spacer(
                                        modifier =
                                            Modifier.height(
                                                12.dp
                                            )
                                    )
                                }


                                // =================================
                                // BOTÓN ACTIVAR / DESACTIVAR
                                // =================================

                                Button(

                                    onClick = {

                                        cambiarActivo(
                                            cuartel
                                        )
                                    },

                                    enabled =
                                        !guardando,

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
                                                if (
                                                    cuartel.activo
                                                ) {
                                                    Color(
                                                        0xFF7A1F1F
                                                    )
                                                } else {
                                                    Color(
                                                        0xFF176E43
                                                    )
                                                },

                                            disabledContainerColor =
                                                Color(
                                                    0xFF30343A
                                                )
                                        )
                                ) {

                                    Text(

                                        text =
                                            if (
                                                guardando
                                            ) {
                                                "GUARDANDO..."
                                            } else if (
                                                cuartel.activo
                                            ) {
                                                "DESACTIVAR CUARTEL"
                                            } else {
                                                "ACTIVAR CUARTEL"
                                            },

                                        color =
                                            Color.White,

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
                }
            }
        }
    }
}


// =============================================================
// SELECTOR FIRESTORE
// =============================================================

@Composable
private fun SelectorFirestore(

    valor: String,

    opciones: List<String>,

    enabled: Boolean,

    onSeleccionar: (String) -> Unit

) {

    var expandido by remember {

        mutableStateOf(
            false
        )
    }


    Box(

        modifier =
            Modifier.fillMaxWidth()
    ) {

        Button(

            onClick = {

                if (
                    enabled
                ) {

                    expandido =
                        true
                }
            },

            enabled =
                enabled,

            modifier =
                Modifier.fillMaxWidth(),

            shape =
                RoundedCornerShape(
                    13.dp
                ),

            colors =
                ButtonDefaults.buttonColors(

                    containerColor =
                        Color(
                            0xFF20252B
                        ),

                    contentColor =
                        Color.White,

                    disabledContainerColor =
                        Color(
                            0xFF181B1F
                        )
                )
        ) {

            Row(

                modifier =
                    Modifier.fillMaxWidth(),

                horizontalArrangement =
                    Arrangement.SpaceBetween,

                verticalAlignment =
                    Alignment.CenterVertically
            ) {

                Text(

                    text =
                        valor
                            .replace(
                                "_",
                                " "
                            )
                            .replaceFirstChar {
                                it.uppercase()
                            },

                    fontSize =
                        13.sp,

                    fontWeight =
                        FontWeight.Medium
                )


                Text(
                    text =
                        "▼",

                    color =
                        Color(0xFF9EA1A5),

                    fontSize =
                        10.sp
                )
            }
        }


        DropdownMenu(

            expanded =
                expandido,

            onDismissRequest = {

                expandido =
                    false
            }
        ) {

            opciones.forEach { opcion ->

                DropdownMenuItem(

                    text = {

                        Text(

                            text =
                                opcion
                                    .replace(
                                        "_",
                                        " "
                                    )
                                    .replaceFirstChar {
                                        it.uppercase()
                                    },

                            fontSize =
                                13.sp
                        )
                    },

                    onClick = {

                        expandido =
                            false

                        if (
                            opcion != valor
                        ) {

                            onSeleccionar(
                                opcion
                            )
                        }
                    }
                )
            }
        }
    }
}