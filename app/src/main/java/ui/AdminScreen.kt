package com.example.sab.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import com.example.sab.R
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore


// =============================================================
// COLORES FIRE ALERT
// =============================================================

private val FireRed = Color(0xFFE53935)
private val FireDarkRed = Color(0xFF8B0000)
private val FireGreen = Color(0xFF39C26B)
private val FireDark = Color(0xFF101214)
private val FireWhite = Color(0xFFF5F5F5)
private val FireGray = Color(0xFFB7BDC2)
private val FireCard = Color(0xD9111417)
private val FireBlue = Color(0xFF3D7EFF)


// =============================================================
// COLORES CAMPOS
// =============================================================

private val FieldText = Color.White
private val FieldLabel = Color(0xFFD7DCE0)
private val FieldBorder = Color(0xFF7E878E)
private val FieldBorderFocused = FireRed
private val FieldCursor = FireRed


// =============================================================
// MODELO USUARIO
// =============================================================

data class UsuarioFireAlert(
    val id: String,
    val nombre: String,
    val email: String,
    val telefono: String,
    val rol: String,
    val roles: List<String>,
    val activo: Boolean,
    val cuartelId: String
)


// =============================================================
// COLORES TEXTFIELD
// =============================================================

@Composable
private fun fireAlertTextFieldColors() =
    OutlinedTextFieldDefaults.colors(
        focusedTextColor = FieldText,
        unfocusedTextColor = FieldText,
        disabledTextColor = FieldText.copy(alpha = 0.5f),

        focusedLabelColor = FieldBorderFocused,
        unfocusedLabelColor = FieldLabel,
        disabledLabelColor = FieldLabel.copy(alpha = 0.5f),

        focusedBorderColor = FieldBorderFocused,
        unfocusedBorderColor = FieldBorder,
        disabledBorderColor = FieldBorder.copy(alpha = 0.4f),

        cursorColor = FieldCursor,

        focusedLeadingIconColor = FieldLabel,
        unfocusedLeadingIconColor = FieldLabel,

        focusedTrailingIconColor = FieldLabel,
        unfocusedTrailingIconColor = FieldLabel
    )


// =============================================================
// ADMIN SCREEN
// =============================================================

@Composable
fun AdminScreen(
    onVolver: () -> Unit
) {

    val context = LocalContext.current

    val auth = remember {
        FirebaseAuth.getInstance()
    }

    val db = remember {
        FirebaseFirestore.getInstance()
    }


    // =========================================================
    // ESTADOS
    // =========================================================

    var usuarios by remember {
        mutableStateOf<List<UsuarioFireAlert>>(emptyList())
    }

    var cuartelId by remember {
        mutableStateOf<String?>(null)
    }

    var cargandoCuartel by remember {
        mutableStateOf(true)
    }

    var mostrarFormulario by remember {
        mutableStateOf(false)
    }

    var mostrarEdicion by remember {
        mutableStateOf(false)
    }

    var usuarioEditando by remember {
        mutableStateOf<UsuarioFireAlert?>(null)
    }

    var telefonoEditado by remember {
        mutableStateOf("")
    }

    var editarBombero by remember {
        mutableStateOf(false)
    }

    var editarCuartelero by remember {
        mutableStateOf(false)
    }

    var editarAdministrador by remember {
        mutableStateOf(false)
    }

    var nombreNuevo by remember {
        mutableStateOf("")
    }

    var emailNuevo by remember {
        mutableStateOf("")
    }

    var telefonoNuevo by remember {
        mutableStateOf("")
    }

    var contrasenaNueva by remember {
        mutableStateOf("")
    }

    var rolBombero by remember {
        mutableStateOf(true)
    }

    var rolCuartelero by remember {
        mutableStateOf(false)
    }

    var rolAdministrador by remember {
        mutableStateOf(false)
    }

    var mensaje by remember {
        mutableStateOf("")
    }

    var creandoUsuario by remember {
        mutableStateOf(false)
    }

    var guardandoEdicion by remember {
        mutableStateOf(false)
    }


    // =========================================================
    // OBTENER CUARTEL DEL ADMINISTRADOR
    // =========================================================

    DisposableEffect(Unit) {

        val usuarioActual = auth.currentUser

        if (usuarioActual == null) {

            cargandoCuartel = false
            mensaje = "No hay un usuario autenticado"

            onDispose { }

        } else {

            db.collection("usuarios")
                .document(usuarioActual.uid)
                .get()
                .addOnSuccessListener { documento ->

                    if (!documento.exists()) {

                        cargandoCuartel = false
                        cuartelId = null
                        mensaje =
                            "No se encontró el usuario administrador"

                        return@addOnSuccessListener
                    }

                    val id =
                        documento
                            .getString("cuartelId")
                            ?.trim()

                    if (id.isNullOrEmpty()) {

                        cargandoCuartel = false
                        cuartelId = null

                        mensaje =
                            "Este administrador todavía no tiene un cuartel asignado"

                    } else {

                        cuartelId = id
                        cargandoCuartel = false
                        mensaje = ""
                    }
                }
                .addOnFailureListener { error ->

                    cargandoCuartel = false
                    cuartelId = null

                    mensaje =
                        "Error obteniendo el cuartel: ${error.message}"
                }

            onDispose { }
        }
    }


    // =========================================================
    // ESCUCHAR USUARIOS DEL CUARTEL
    // =========================================================

    DisposableEffect(cuartelId) {

        val idCuartel = cuartelId

        if (idCuartel.isNullOrEmpty()) {

            usuarios = emptyList()

            onDispose { }

        } else {

            val listener =
                db.collection("usuarios")
                    .whereEqualTo(
                        "cuartelId",
                        idCuartel
                    )
                    .addSnapshotListener { snapshot, error ->

                        if (error != null) {

                            mensaje =
                                "Error leyendo usuarios: ${error.message}"

                            return@addSnapshotListener
                        }

                        if (snapshot == null) {
                            return@addSnapshotListener
                        }

                        usuarios =
                            snapshot.documents.map { documento ->

                                val nombreFirestore =
                                    documento
                                        .getString("nombre")
                                        ?.trim()

                                val nombreBombero =
                                    documento
                                        .getString("bombero")
                                        ?.trim()

                                val nombreFinal =
                                    when {

                                        !nombreFirestore
                                            .isNullOrEmpty() ->
                                            nombreFirestore

                                        !nombreBombero
                                            .isNullOrEmpty() ->
                                            nombreBombero

                                        else ->
                                            "Sin nombre"
                                    }


                                val rolesFirestore =
                                    documento.get("roles")

                                val rolesFinales =
                                    if (
                                        rolesFirestore is List<*>
                                    ) {

                                        rolesFirestore
                                            .filterIsInstance<String>()
                                            .map {
                                                it.trim().lowercase()
                                            }
                                            .filter {
                                                it.isNotEmpty()
                                            }

                                    } else {

                                        emptyList()
                                    }


                                val rolAntiguo =
                                    documento
                                        .getString("rol")
                                        ?.trim()
                                        ?.lowercase()
                                        ?: "sin rol"


                                UsuarioFireAlert(

                                    id =
                                        documento.id,

                                    nombre =
                                        nombreFinal,

                                    email =
                                        documento
                                            .getString("email")
                                            ?: "Sin email",

                                    telefono =
                                        documento
                                            .getString("telefono")
                                            ?: "Sin teléfono",

                                    rol =
                                        rolAntiguo,

                                    roles =
                                        rolesFinales,

                                    activo =
                                        documento
                                            .getBoolean("activo")
                                            ?: false,

                                    cuartelId =
                                        documento
                                            .getString("cuartelId")
                                            ?: ""
                                )
                            }


                        if (!mensaje.startsWith("Error")) {
                            mensaje = ""
                        }
                    }


            onDispose {
                listener.remove()
            }
        }
    }


    // =============================================================
    // FONDO
    // =============================================================

    Box(
        modifier = Modifier.fillMaxSize()
    ) {

        Image(
            painter =
                painterResource(
                    R.drawable.fondo_bomberos
                ),

            contentDescription =
                "Fondo FireAlert",

            modifier =
                Modifier.fillMaxSize(),

            contentScale =
                ContentScale.Crop
        )


        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .background(
                        Color.Black.copy(
                            alpha = 0.56f
                        )
                    )
        )


        // =========================================================
        // CARGANDO
        // =========================================================

        if (cargandoCuartel) {

            Box(
                modifier =
                    Modifier.fillMaxSize(),

                contentAlignment =
                    Alignment.Center
            ) {

                Text(
                    text =
                        "CARGANDO INFORMACIÓN DEL CUARTEL...",

                    color =
                        FireWhite,

                    fontSize =
                        13.sp,

                    fontWeight =
                        FontWeight.Bold
                )
            }


        } else if (cuartelId.isNullOrEmpty()) {

            // =====================================================
            // SIN CUARTEL
            // =====================================================

            Box(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .padding(30.dp),

                contentAlignment =
                    Alignment.Center
            ) {

                Card(
                    modifier =
                        Modifier.fillMaxWidth(),

                    shape =
                        RoundedCornerShape(22.dp),

                    colors =
                        CardDefaults.cardColors(
                            containerColor =
                                FireCard
                        )
                ) {

                    Column(
                        modifier =
                            Modifier.padding(24.dp),

                        horizontalAlignment =
                            Alignment.CenterHorizontally
                    ) {

                        Text(
                            text =
                                "⚠️",

                            fontSize =
                                38.sp
                        )

                        Spacer(
                            Modifier.height(12.dp)
                        )

                        Text(
                            text =
                                "CUARTEL NO ASIGNADO",

                            color =
                                FireWhite,

                            fontSize =
                                19.sp,

                            fontWeight =
                                FontWeight.Bold,

                            textAlign =
                                TextAlign.Center
                        )

                        Spacer(
                            Modifier.height(8.dp)
                        )

                        Text(
                            text =
                                mensaje,

                            color =
                                FireGray,

                            fontSize =
                                13.sp,

                            textAlign =
                                TextAlign.Center
                        )
                    }
                }
            }


        } else if (
            mostrarEdicion &&
            usuarioEditando != null
        ) {

            // =====================================================
            // EDITAR PERSONAL
            // =====================================================

            val usuario =
                usuarioEditando!!


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
                // HEADER EDITAR
                // =================================================

                Row(
                    modifier =
                        Modifier.fillMaxWidth(),

                    verticalAlignment =
                        Alignment.CenterVertically
                ) {

                    Button(
                        onClick = {

                            if (!guardandoEdicion) {

                                mostrarEdicion =
                                    false

                                usuarioEditando =
                                    null

                                mensaje =
                                    ""
                            }
                        },

                        enabled =
                            !guardandoEdicion,

                        modifier =
                            Modifier.size(44.dp),

                        contentPadding =
                            PaddingValues(0.dp),

                        colors =
                            ButtonDefaults.buttonColors(
                                containerColor =
                                    Color.White.copy(
                                        alpha = 0.08f
                                    )
                            ),

                        shape =
                            CircleShape
                    ) {

                        Text(
                            text =
                                "←",

                            color =
                                FireWhite,

                            fontSize =
                                25.sp,

                            fontWeight =
                                FontWeight.Light
                        )
                    }

                    Spacer(
                        Modifier.width(12.dp)
                    )

                    Column {

                        Text(
                            text =
                                "FIRE ALERT",

                            color =
                                FireWhite,

                            fontSize =
                                26.sp,

                            fontWeight =
                                FontWeight.Bold,

                            letterSpacing =
                                1.5.sp
                        )

                        Text(
                            text =
                                "EDITAR PERSONAL",

                            color =
                                FireGray,

                            fontSize =
                                12.sp,

                            letterSpacing =
                                1.2.sp
                        )
                    }
                }


                Spacer(
                    Modifier.height(22.dp)
                )


                Card(
                    modifier =
                        Modifier.fillMaxWidth(),

                    shape =
                        RoundedCornerShape(22.dp),

                    colors =
                        CardDefaults.cardColors(
                            containerColor =
                                FireCard
                        )
                ) {

                    Column(
                        modifier =
                            Modifier.padding(20.dp)
                    ) {

                        Row(
                            verticalAlignment =
                                Alignment.CenterVertically
                        ) {

                            Box(
                                modifier =
                                    Modifier
                                        .size(52.dp)
                                        .background(
                                            FireBlue.copy(
                                                alpha = 0.18f
                                            ),
                                            CircleShape
                                        )
                                        .border(
                                            1.dp,
                                            FireBlue.copy(
                                                alpha = 0.5f
                                            ),
                                            CircleShape
                                        ),

                                contentAlignment =
                                    Alignment.Center
                            ) {

                                Text(
                                    text =
                                        "👤",

                                    fontSize =
                                        23.sp
                                )
                            }


                            Spacer(
                                Modifier.width(14.dp)
                            )


                            Column {

                                Text(
                                    text =
                                        usuario.nombre,

                                    color =
                                        FireWhite,

                                    fontSize =
                                        20.sp,

                                    fontWeight =
                                        FontWeight.Bold
                                )

                                Spacer(
                                    Modifier.height(3.dp)
                                )

                                Text(
                                    text =
                                        usuario.email,

                                    color =
                                        FireGray,

                                    fontSize =
                                        12.sp
                                )
                            }
                        }


                        Spacer(
                            Modifier.height(22.dp)
                        )


                        OutlinedTextField(
                            value =
                                telefonoEditado,

                            onValueChange = {
                                telefonoEditado = it
                            },

                            label = {
                                Text("📱 Teléfono")
                            },

                            modifier =
                                Modifier.fillMaxWidth(),

                            singleLine =
                                true,

                            colors =
                                fireAlertTextFieldColors()
                        )


                        Spacer(
                            Modifier.height(20.dp)
                        )


                        Text(
                            text =
                                "ROLES DEL PERSONAL",

                            color =
                                FireGray,

                            fontSize =
                                11.sp,

                            fontWeight =
                                FontWeight.Bold,

                            letterSpacing =
                                1.2.sp
                        )

                        Spacer(
                            Modifier.height(8.dp)
                        )


                        RoleCheck(
                            texto =
                                "👨‍🚒  Bombero",

                            checked =
                                editarBombero,

                            onCheckedChange = {
                                editarBombero = it
                            }
                        )


                        RoleCheck(
                            texto =
                                "📢  Cuartelero",

                            checked =
                                editarCuartelero,

                            onCheckedChange = {
                                editarCuartelero = it
                            }
                        )


                        RoleCheck(
                            texto =
                                "👑  Administrador",

                            checked =
                                editarAdministrador,

                            onCheckedChange = {
                                editarAdministrador = it
                            }
                        )


                        if (mensaje.isNotEmpty()) {

                            Spacer(
                                Modifier.height(10.dp)
                            )

                            Text(
                                text =
                                    mensaje,

                                color =
                                    if (
                                        mensaje.startsWith(
                                            "Cambios guardados"
                                        )
                                    ) {
                                        FireGreen
                                    } else {
                                        Color.Yellow
                                    },

                                fontSize =
                                    13.sp
                            )
                        }


                        Spacer(
                            Modifier.height(20.dp)
                        )


                        Row(
                            modifier =
                                Modifier.fillMaxWidth()
                        ) {

                            Button(
                                onClick = {

                                    mostrarEdicion =
                                        false

                                    usuarioEditando =
                                        null

                                    mensaje =
                                        ""
                                },

                                enabled =
                                    !guardandoEdicion,

                                modifier =
                                    Modifier.weight(1f),

                                colors =
                                    ButtonDefaults.buttonColors(
                                        containerColor =
                                            Color.White.copy(
                                                alpha = 0.12f
                                            )
                                    ),

                                shape =
                                    RoundedCornerShape(14.dp)
                            ) {

                                Text(
                                    text =
                                        "CANCELAR"
                                )
                            }


                            Spacer(
                                Modifier.width(10.dp)
                            )


                            Button(
                                enabled =
                                    !guardandoEdicion,

                                onClick = {

                                    if (
                                        telefonoEditado
                                            .trim()
                                            .isEmpty()
                                    ) {

                                        mensaje =
                                            "Ingresá el teléfono"

                                        return@Button
                                    }


                                    if (
                                        !editarBombero &&
                                        !editarCuartelero &&
                                        !editarAdministrador
                                    ) {

                                        mensaje =
                                            "Seleccioná al menos un rol"

                                        return@Button
                                    }


                                    guardandoEdicion =
                                        true

                                    mensaje =
                                        "Guardando cambios..."


                                    val rolesEditados =
                                        mutableListOf<String>()


                                    if (editarBombero) {
                                        rolesEditados.add(
                                            "bombero"
                                        )
                                    }

                                    if (editarCuartelero) {
                                        rolesEditados.add(
                                            "cuartelero"
                                        )
                                    }

                                    if (editarAdministrador) {
                                        rolesEditados.add(
                                            "administrador"
                                        )
                                    }


                                    val rolPrincipal =
                                        rolesEditados.first()


                                    val datosActualizados =
                                        hashMapOf<String, Any>(

                                            "telefono" to
                                                    telefonoEditado
                                                        .trim(),

                                            "rol" to
                                                    rolPrincipal,

                                            "roles" to
                                                    rolesEditados,

                                            "cuartelId" to
                                                    (cuartelId ?: "")
                                        )


                                    db.collection("usuarios")
                                        .document(usuario.id)
                                        .update(
                                            datosActualizados
                                        )
                                        .addOnSuccessListener {

                                            guardandoEdicion =
                                                false

                                            mensaje =
                                                "Cambios guardados correctamente"

                                            mostrarEdicion =
                                                false

                                            usuarioEditando =
                                                null
                                        }
                                        .addOnFailureListener { error ->

                                            guardandoEdicion =
                                                false

                                            mensaje =
                                                "Error guardando cambios: ${error.message}"
                                        }
                                },

                                modifier =
                                    Modifier.weight(1f),

                                colors =
                                    ButtonDefaults.buttonColors(
                                        containerColor =
                                            FireRed
                                    ),

                                shape =
                                    RoundedCornerShape(14.dp)
                            ) {

                                Text(
                                    text =
                                        if (
                                            guardandoEdicion
                                        ) {
                                            "GUARDANDO..."
                                        } else {
                                            "GUARDAR"
                                        },

                                    fontWeight =
                                        FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }


        } else if (mostrarFormulario) {

            // =====================================================
            // NUEVO PERSONAL
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
                // HEADER NUEVO PERSONAL
                // =================================================

                Row(
                    modifier =
                        Modifier.fillMaxWidth(),

                    verticalAlignment =
                        Alignment.CenterVertically
                ) {

                    Button(
                        onClick = {

                            if (!creandoUsuario) {

                                mostrarFormulario =
                                    false

                                mensaje =
                                    ""
                            }
                        },

                        enabled =
                            !creandoUsuario,

                        modifier =
                            Modifier.size(44.dp),

                        contentPadding =
                            PaddingValues(0.dp),

                        colors =
                            ButtonDefaults.buttonColors(
                                containerColor =
                                    Color.White.copy(
                                        alpha = 0.08f
                                    )
                            ),

                        shape =
                            CircleShape
                    ) {

                        Text(
                            text =
                                "←",

                            color =
                                FireWhite,

                            fontSize =
                                25.sp,

                            fontWeight =
                                FontWeight.Light
                        )
                    }

                    Spacer(
                        Modifier.width(12.dp)
                    )

                    Column {

                        Text(
                            text =
                                "FIRE ALERT",

                            color =
                                FireWhite,

                            fontSize =
                                26.sp,

                            fontWeight =
                                FontWeight.Bold,

                            letterSpacing =
                                1.5.sp
                        )

                        Text(
                            text =
                                "NUEVO PERSONAL",

                            color =
                                FireGray,

                            fontSize =
                                12.sp,

                            letterSpacing =
                                1.2.sp
                        )
                    }
                }


                Spacer(
                    Modifier.height(20.dp)
                )


                Card(
                    modifier =
                        Modifier.fillMaxWidth(),

                    shape =
                        RoundedCornerShape(22.dp),

                    colors =
                        CardDefaults.cardColors(
                            containerColor =
                                FireCard
                        )
                ) {

                    Column(
                        modifier =
                            Modifier.padding(20.dp)
                    ) {

                        OutlinedTextField(
                            value =
                                nombreNuevo,

                            onValueChange = {
                                nombreNuevo = it
                            },

                            label = {
                                Text(
                                    "👤 Nombre completo"
                                )
                            },

                            modifier =
                                Modifier.fillMaxWidth(),

                            singleLine =
                                true,

                            colors =
                                fireAlertTextFieldColors()
                        )


                        Spacer(
                            Modifier.height(12.dp)
                        )


                        OutlinedTextField(
                            value =
                                emailNuevo,

                            onValueChange = {
                                emailNuevo = it
                            },

                            label = {
                                Text(
                                    "📧 Correo electrónico"
                                )
                            },

                            modifier =
                                Modifier.fillMaxWidth(),

                            singleLine =
                                true,

                            colors =
                                fireAlertTextFieldColors()
                        )


                        Spacer(
                            Modifier.height(12.dp)
                        )


                        OutlinedTextField(
                            value =
                                telefonoNuevo,

                            onValueChange = {
                                telefonoNuevo = it
                            },

                            label = {
                                Text(
                                    "📱 Teléfono"
                                )
                            },

                            modifier =
                                Modifier.fillMaxWidth(),

                            singleLine =
                                true,

                            colors =
                                fireAlertTextFieldColors()
                        )


                        Spacer(
                            Modifier.height(12.dp)
                        )


                        OutlinedTextField(
                            value =
                                contrasenaNueva,

                            onValueChange = {
                                contrasenaNueva = it
                            },

                            label = {
                                Text(
                                    "🔐 Contraseña"
                                )
                            },

                            visualTransformation =
                                PasswordVisualTransformation(),

                            modifier =
                                Modifier.fillMaxWidth(),

                            singleLine =
                                true,

                            colors =
                                fireAlertTextFieldColors()
                        )


                        Spacer(
                            Modifier.height(20.dp)
                        )


                        Text(
                            text =
                                "ROLES DEL PERSONAL",

                            color =
                                FireGray,

                            fontSize =
                                11.sp,

                            fontWeight =
                                FontWeight.Bold,

                            letterSpacing =
                                1.2.sp
                        )


                        Spacer(
                            Modifier.height(8.dp)
                        )


                        RoleCheck(
                            texto =
                                "👨‍🚒  Bombero",

                            checked =
                                rolBombero,

                            onCheckedChange = {
                                rolBombero = it
                            }
                        )


                        RoleCheck(
                            texto =
                                "📢  Cuartelero",

                            checked =
                                rolCuartelero,

                            onCheckedChange = {
                                rolCuartelero = it
                            }
                        )


                        RoleCheck(
                            texto =
                                "👑  Administrador",

                            checked =
                                rolAdministrador,

                            onCheckedChange = {
                                rolAdministrador = it
                            }
                        )


                        if (mensaje.isNotEmpty()) {

                            Spacer(
                                Modifier.height(8.dp)
                            )

                            Text(
                                text =
                                    mensaje,

                                color =
                                    if (
                                        mensaje.startsWith(
                                            "Usuario creado"
                                        )
                                    ) {
                                        FireGreen
                                    } else {
                                        Color.Yellow
                                    },

                                fontSize =
                                    13.sp
                            )
                        }


                        Spacer(
                            Modifier.height(20.dp)
                        )


                        Row(
                            modifier =
                                Modifier.fillMaxWidth()
                        ) {

                            Button(
                                onClick = {

                                    mostrarFormulario =
                                        false

                                    mensaje =
                                        ""
                                },

                                enabled =
                                    !creandoUsuario,

                                modifier =
                                    Modifier.weight(1f),

                                colors =
                                    ButtonDefaults.buttonColors(
                                        containerColor =
                                            Color.White.copy(
                                                alpha = 0.12f
                                            )
                                    ),

                                shape =
                                    RoundedCornerShape(14.dp)
                            ) {

                                Text(
                                    text =
                                        "CANCELAR"
                                )
                            }


                            Spacer(
                                Modifier.width(10.dp)
                            )


                            Button(
                                enabled =
                                    !creandoUsuario,

                                onClick = {

                                    // =================================
                                    // VALIDACIONES
                                    // =================================

                                    if (
                                        cuartelId.isNullOrEmpty()
                                    ) {

                                        mensaje =
                                            "No se encontró el cuartel del administrador"

                                        return@Button
                                    }


                                    if (
                                        nombreNuevo
                                            .trim()
                                            .isEmpty()
                                    ) {

                                        mensaje =
                                            "Ingresá el nombre"

                                        return@Button
                                    }


                                    if (
                                        emailNuevo
                                            .trim()
                                            .isEmpty()
                                    ) {

                                        mensaje =
                                            "Ingresá el correo"

                                        return@Button
                                    }


                                    if (
                                        telefonoNuevo
                                            .trim()
                                            .isEmpty()
                                    ) {

                                        mensaje =
                                            "Ingresá el teléfono"

                                        return@Button
                                    }


                                    if (
                                        contrasenaNueva.length < 6
                                    ) {

                                        mensaje =
                                            "La contraseña debe tener mínimo 6 caracteres"

                                        return@Button
                                    }


                                    if (
                                        !rolBombero &&
                                        !rolCuartelero &&
                                        !rolAdministrador
                                    ) {

                                        mensaje =
                                            "Seleccioná al menos un rol"

                                        return@Button
                                    }


                                    creandoUsuario =
                                        true

                                    mensaje =
                                        "Creando usuario..."


                                    // =================================
                                    // FIREBASE SECUNDARIO
                                    // =================================

                                    val appPrincipal =
                                        FirebaseApp.getInstance()


                                    val nombreAppSecundaria =
                                        "FireAlertSecondary"


                                    val appSecundariaExistente =
                                        try {

                                            FirebaseApp.getInstance(
                                                nombreAppSecundaria
                                            )

                                        } catch (
                                            _: Exception
                                        ) {

                                            null
                                        }


                                    val appSecundaria =
                                        appSecundariaExistente
                                            ?: FirebaseApp.initializeApp(
                                                context,
                                                appPrincipal.options,
                                                nombreAppSecundaria
                                            )


                                    if (
                                        appSecundaria == null
                                    ) {

                                        creandoUsuario =
                                            false

                                        mensaje =
                                            "No se pudo iniciar Firebase secundario"

                                        return@Button
                                    }


                                    val authSecundario =
                                        FirebaseAuth.getInstance(
                                            appSecundaria
                                        )


                                    authSecundario
                                        .createUserWithEmailAndPassword(
                                            emailNuevo.trim(),
                                            contrasenaNueva
                                        )
                                        .addOnSuccessListener { resultado ->

                                            val usuarioNuevo =
                                                resultado.user


                                            if (
                                                usuarioNuevo == null
                                            ) {

                                                creandoUsuario =
                                                    false

                                                mensaje =
                                                    "No se pudo obtener el usuario creado"

                                                authSecundario
                                                    .signOut()

                                                return@addOnSuccessListener
                                            }


                                            val uidNuevo =
                                                usuarioNuevo.uid


                                            val roles =
                                                mutableListOf<String>()


                                            if (rolBombero) {
                                                roles.add(
                                                    "bombero"
                                                )
                                            }

                                            if (rolCuartelero) {
                                                roles.add(
                                                    "cuartelero"
                                                )
                                            }

                                            if (rolAdministrador) {
                                                roles.add(
                                                    "administrador"
                                                )
                                            }


                                            val rolPrincipal =
                                                roles.first()


                                            val datosUsuario =
                                                hashMapOf<String, Any>(

                                                    "nombre" to
                                                            nombreNuevo
                                                                .trim(),

                                                    "email" to
                                                            emailNuevo
                                                                .trim(),

                                                    "telefono" to
                                                            telefonoNuevo
                                                                .trim(),

                                                    "rol" to
                                                            rolPrincipal,

                                                    "roles" to
                                                            roles,

                                                    "activo" to
                                                            true,

                                                    "cuartelId" to
                                                            cuartelId!!
                                                )


                                            // =================================
                                            // GUARDAR FIRESTORE
                                            // =================================

                                            db.collection("usuarios")
                                                .document(uidNuevo)
                                                .set(
                                                    datosUsuario
                                                )
                                                .addOnSuccessListener {

                                                    authSecundario
                                                        .signOut()

                                                    creandoUsuario =
                                                        false

                                                    mensaje =
                                                        "Usuario creado correctamente"

                                                    nombreNuevo =
                                                        ""

                                                    emailNuevo =
                                                        ""

                                                    telefonoNuevo =
                                                        ""

                                                    contrasenaNueva =
                                                        ""

                                                    rolBombero =
                                                        true

                                                    rolCuartelero =
                                                        false

                                                    rolAdministrador =
                                                        false

                                                    mostrarFormulario =
                                                        false
                                                }
                                                .addOnFailureListener { error ->

                                                    authSecundario
                                                        .signOut()

                                                    creandoUsuario =
                                                        false

                                                    mensaje =
                                                        "Usuario creado, pero error guardando datos: ${error.message}"
                                                }
                                        }
                                        .addOnFailureListener { error ->

                                            authSecundario
                                                .signOut()

                                            creandoUsuario =
                                                false

                                            mensaje =
                                                "Error creando usuario: ${error.message}"
                                        }
                                },

                                modifier =
                                    Modifier.weight(1f),

                                colors =
                                    ButtonDefaults.buttonColors(
                                        containerColor =
                                            FireRed
                                    ),

                                shape =
                                    RoundedCornerShape(14.dp)
                            ) {

                                Text(
                                    text =
                                        if (
                                            creandoUsuario
                                        ) {
                                            "CREANDO..."
                                        } else {
                                            "CREAR USUARIO"
                                        },

                                    fontWeight =
                                        FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }


        } else {

            // =====================================================
            // LISTA PRINCIPAL
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
                // HEADER ADMINISTRACIÓN
                // =================================================

                Row(
                    modifier =
                        Modifier.fillMaxWidth(),

                    verticalAlignment =
                        Alignment.CenterVertically
                ) {

                    Button(
                        onClick =
                            onVolver,

                        modifier =
                            Modifier.size(44.dp),

                        contentPadding =
                            PaddingValues(0.dp),

                        colors =
                            ButtonDefaults.buttonColors(
                                containerColor =
                                    Color.White.copy(
                                        alpha = 0.08f
                                    )
                            ),

                        shape =
                            CircleShape
                    ) {

                        Text(
                            text =
                                "←",

                            color =
                                FireWhite,

                            fontSize =
                                25.sp,

                            fontWeight =
                                FontWeight.Light
                        )
                    }

                    Spacer(
                        Modifier.width(12.dp)
                    )

                    Column(
                        modifier =
                            Modifier.weight(1f)
                    ) {

                        Text(
                            text =
                                "FIRE ALERT",

                            color =
                                FireWhite,

                            fontSize =
                                27.sp,

                            fontWeight =
                                FontWeight.Bold,

                            letterSpacing =
                                1.5.sp
                        )

                        Text(
                            text =
                                "ADMINISTRACIÓN DE PERSONAL",

                            color =
                                FireGray,

                            fontSize =
                                11.sp,

                            fontWeight =
                                FontWeight.Medium,

                            letterSpacing =
                                1.sp
                        )
                    }


                    Box(
                        modifier =
                            Modifier
                                .size(44.dp)
                                .background(
                                    FireRed.copy(
                                        alpha = 0.16f
                                    ),
                                    CircleShape
                                )
                                .border(
                                    1.dp,
                                    FireRed.copy(
                                        alpha = 0.5f
                                    ),
                                    CircleShape
                                ),

                        contentAlignment =
                            Alignment.Center
                    ) {

                        Text(
                            text =
                                "👑",

                            fontSize =
                                20.sp
                        )
                    }
                }


                Spacer(
                    Modifier.height(20.dp)
                )


                Card(
                    modifier =
                        Modifier.fillMaxWidth(),

                    shape =
                        RoundedCornerShape(20.dp),

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
                                .padding(18.dp),

                        verticalAlignment =
                            Alignment.CenterVertically
                    ) {

                        Box(
                            modifier =
                                Modifier
                                    .size(48.dp)
                                    .background(
                                        FireGreen.copy(
                                            alpha = 0.12f
                                        ),
                                        CircleShape
                                    )
                                    .border(
                                        1.dp,
                                        FireGreen.copy(
                                            alpha = 0.35f
                                        ),
                                        CircleShape
                                    ),

                            contentAlignment =
                                Alignment.Center
                        ) {

                            Text(
                                text =
                                    "👥",

                                fontSize =
                                    22.sp
                            )
                        }


                        Spacer(
                            Modifier.width(14.dp)
                        )


                        Column(
                            modifier =
                                Modifier.weight(1f)
                        ) {

                            Text(
                                text =
                                    "PERSONAL REGISTRADO",

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
                                Modifier.height(3.dp)
                            )

                            Text(
                                text =
                                    "${usuarios.size} integrantes",

                                color =
                                    FireWhite,

                                fontSize =
                                    19.sp,

                                fontWeight =
                                    FontWeight.Bold
                            )
                        }


                        Button(
                            onClick = {

                                mensaje =
                                    ""

                                mostrarFormulario =
                                    true
                            },

                            modifier =
                                Modifier.height(44.dp),

                            colors =
                                ButtonDefaults.buttonColors(
                                    containerColor =
                                        FireRed
                                ),

                            shape =
                                RoundedCornerShape(13.dp)
                        ) {

                            Text(
                                text =
                                    "＋ AGREGAR",

                                fontSize =
                                    12.sp,

                                fontWeight =
                                    FontWeight.Bold
                            )
                        }
                    }
                }


                if (mensaje.isNotEmpty()) {

                    Spacer(
                        Modifier.height(10.dp)
                    )

                    Text(
                        text =
                            mensaje,

                        modifier =
                            Modifier.fillMaxWidth(),

                        color =
                            if (
                                mensaje.contains("Error") ||
                                mensaje.contains("no tiene") ||
                                mensaje.contains("no se encontró")
                            ) {
                                FireRed
                            } else {
                                FireGreen
                            },

                        fontSize =
                            12.sp,

                        textAlign =
                            TextAlign.Center
                    )
                }


                Spacer(
                    Modifier.height(8.dp)
                )


                LazyColumn(
                    modifier =
                        Modifier.fillMaxWidth()
                ) {

                    items(
                        items =
                            usuarios,

                        key = {
                            it.id
                        }
                    ) { usuario ->

                        UsuarioCard(

                            usuario =
                                usuario,

                            onEditar = {

                                usuarioEditando =
                                    usuario

                                telefonoEditado =
                                    usuario.telefono


                                val rolesUsuario =
                                    if (
                                        usuario.roles.isNotEmpty()
                                    ) {
                                        usuario.roles
                                    } else {
                                        listOf(
                                            usuario.rol
                                        )
                                    }


                                editarBombero =
                                    rolesUsuario.contains(
                                        "bombero"
                                    )

                                editarCuartelero =
                                    rolesUsuario.contains(
                                        "cuartelero"
                                    )

                                editarAdministrador =
                                    rolesUsuario.contains(
                                        "administrador"
                                    )


                                mensaje =
                                    ""

                                mostrarEdicion =
                                    true
                            },


                            onCambiarEstado = {

                                val nuevoEstado =
                                    !usuario.activo


                                db.collection("usuarios")
                                    .document(usuario.id)
                                    .update(
                                        "activo",
                                        nuevoEstado
                                    )
                                    .addOnSuccessListener {

                                        mensaje =
                                            if (
                                                nuevoEstado
                                            ) {
                                                "${usuario.nombre} fue activado"
                                            } else {
                                                "${usuario.nombre} fue desactivado"
                                            }
                                    }
                                    .addOnFailureListener { error ->

                                        mensaje =
                                            "Error cambiando estado: ${error.message}"
                                    }
                            }
                        )
                    }
                }
            }
        }
    }
}


// =============================================================
// TARJETA USUARIO
// =============================================================

@Composable
private fun UsuarioCard(
    usuario: UsuarioFireAlert,
    onEditar: () -> Unit,
    onCambiarEstado: () -> Unit
) {

    val textoRoles =

        if (usuario.roles.isNotEmpty()) {

            usuario.roles.joinToString(
                separator = "  •  "
            ) { rol ->

                when (rol.lowercase()) {

                    "bombero" ->
                        "Bombero"

                    "cuartelero" ->
                        "Cuartelero"

                    "administrador" ->
                        "Administrador"

                    else ->
                        rol.replaceFirstChar {
                            it.uppercase()
                        }
                }
            }

        } else {

            when (usuario.rol.lowercase()) {

                "bombero" ->
                    "Bombero"

                "cuartelero" ->
                    "Cuartelero"

                "administrador" ->
                    "Administrador"

                else ->
                    usuario.rol
            }
        }


    Card(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp),

        shape =
            RoundedCornerShape(20.dp),

        colors =
            CardDefaults.cardColors(
                containerColor =
                    FireCard
            )
    ) {

        Column(
            modifier =
                Modifier.padding(17.dp)
        ) {

            Row(
                modifier =
                    Modifier.fillMaxWidth(),

                verticalAlignment =
                    Alignment.CenterVertically
            ) {

                Box(
                    modifier =
                        Modifier
                            .size(48.dp)
                            .background(
                                if (usuario.activo) {
                                    FireGreen.copy(
                                        alpha = 0.13f
                                    )
                                } else {
                                    FireRed.copy(
                                        alpha = 0.13f
                                    )
                                },
                                CircleShape
                            )
                            .border(
                                1.dp,
                                if (usuario.activo) {
                                    FireGreen.copy(
                                        alpha = 0.35f
                                    )
                                } else {
                                    FireRed.copy(
                                        alpha = 0.35f
                                    )
                                },
                                CircleShape
                            ),

                    contentAlignment =
                        Alignment.Center
                ) {

                    Text(
                        text =
                            "👨‍🚒",

                        fontSize =
                            21.sp
                    )
                }


                Spacer(
                    Modifier.width(13.dp)
                )


                Column(
                    modifier =
                        Modifier.weight(1f)
                ) {

                    Text(
                        text =
                            usuario.nombre,

                        color =
                            FireWhite,

                        fontSize =
                            18.sp,

                        fontWeight =
                            FontWeight.Bold
                    )

                    Spacer(
                        Modifier.height(3.dp)
                    )

                    Text(
                        text =
                            if (
                                usuario.activo
                            ) {
                                "● ACTIVO"
                            } else {
                                "● DESACTIVADO"
                            },

                        color =
                            if (
                                usuario.activo
                            ) {
                                FireGreen
                            } else {
                                FireRed
                            },

                        fontSize =
                            10.sp,

                        fontWeight =
                            FontWeight.Bold,

                        letterSpacing =
                            0.8.sp
                    )
                }
            }


            Spacer(
                Modifier.height(15.dp)
            )


            Text(
                text =
                    "📧  ${usuario.email}",

                color =
                    FireGray,

                fontSize =
                    12.sp
            )


            Spacer(
                Modifier.height(6.dp)
            )


            Text(
                text =
                    "📱  ${usuario.telefono}",

                color =
                    FireGray,

                fontSize =
                    12.sp
            )


            Spacer(
                Modifier.height(9.dp)
            )


            Text(
                text =
                    "ROLES",

                color =
                    FireGray,

                fontSize =
                    9.sp,

                fontWeight =
                    FontWeight.Bold,

                letterSpacing =
                    1.sp
            )


            Spacer(
                Modifier.height(3.dp)
            )


            Text(
                text =
                    textoRoles,

                color =
                    FireWhite,

                fontSize =
                    13.sp,

                fontWeight =
                    FontWeight.SemiBold
            )


            Spacer(
                Modifier.height(15.dp)
            )


            Button(
                onClick =
                    onEditar,

                modifier =
                    Modifier.fillMaxWidth(),

                colors =
                    ButtonDefaults.buttonColors(
                        containerColor =
                            FireBlue.copy(
                                alpha = 0.85f
                            )
                    ),

                shape =
                    RoundedCornerShape(13.dp)
            ) {

                Text(
                    text =
                        "✏️  EDITAR PERSONAL",

                    fontWeight =
                        FontWeight.Bold
                )
            }


            Spacer(
                Modifier.height(7.dp)
            )


            Button(
                onClick =
                    onCambiarEstado,

                modifier =
                    Modifier.fillMaxWidth(),

                colors =
                    ButtonDefaults.buttonColors(
                        containerColor =
                            if (
                                usuario.activo
                            ) {
                                FireRed.copy(
                                    alpha = 0.88f
                                )
                            } else {
                                FireGreen.copy(
                                    alpha = 0.88f
                                )
                            }
                    ),

                shape =
                    RoundedCornerShape(13.dp)
            ) {

                Text(
                    text =
                        if (
                            usuario.activo
                        ) {
                            "🔴  DESACTIVAR PERSONAL"
                        } else {
                            "🟢  ACTIVAR PERSONAL"
                        },

                    fontWeight =
                        FontWeight.Bold
                )
            }
        }
    }
}


// =============================================================
// CHECKBOX DE ROL
// =============================================================

@Composable
private fun RoleCheck(
    texto: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {

    Row(
        modifier =
            Modifier.fillMaxWidth(),

        verticalAlignment =
            Alignment.CenterVertically
    ) {

        Checkbox(
            checked =
                checked,

            onCheckedChange =
                onCheckedChange
        )

        Text(
            text =
                texto,

            color =
                FireWhite,

            fontSize =
                14.sp
        )
    }
}
