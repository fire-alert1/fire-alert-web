package com.example.sab

import android.util.Log

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore


@Composable
fun LoginScreen(
    onLoginSuccess: (List<String>) -> Unit
) {

    // =========================================================
    // MODO DE PANTALLA
    // =========================================================

    var modoRegistro by remember {
        mutableStateOf(false)
    }


    // =========================================================
    // CAMPOS LOGIN
    // =========================================================

    var email by remember {
        mutableStateOf("")
    }

    var password by remember {
        mutableStateOf("")
    }


    // =========================================================
    // CAMPOS REGISTRO CUARTEL
    // =========================================================

    var nombreCuartel by remember {
        mutableStateOf("")
    }

    var localidadCuartel by remember {
        mutableStateOf("")
    }

    var provinciaCuartel by remember {
        mutableStateOf("")
    }

    var paisCuartel by remember {
        mutableStateOf("")
    }

    var nombreAdministrador by remember {
        mutableStateOf("")
    }

    var telefonoAdministrador by remember {
        mutableStateOf("")
    }

    var emailAdministrador by remember {
        mutableStateOf("")
    }

    var passwordAdministrador by remember {
        mutableStateOf("")
    }


    // =========================================================
    // ESTADOS
    // =========================================================

    var mensaje by remember {
        mutableStateOf("")
    }

    var mostrarPassword by remember {
        mutableStateOf(false)
    }

    var mostrarPasswordRegistro by remember {
        mutableStateOf(false)
    }

    var enviandoRecuperacion by remember {
        mutableStateOf(false)
    }

    var registrandoCuartel by remember {
        mutableStateOf(false)
    }


    // =========================================================
    // FIREBASE
    // =========================================================

    val auth = remember {
        FirebaseAuth.getInstance()
    }

    val db = remember {
        FirebaseFirestore.getInstance()
    }


    // =========================================================
    // COLORES
    // =========================================================

    val fondoPrincipal =
        Color(0xFF030507)

    val fondoMedio =
        Color(0xFF080B0F)

    val fondoInferior =
        Color(0xFF010203)

    val rojoFire =
        Color(0xFFFF1717)

    val rojoOscuro =
        Color(0xFF9F0000)

    val rojoSuave =
        Color(0xFFFF4A45)

    val blanco =
        Color(0xFFF4F4F4)

    val grisClaro =
        Color(0xFFC2C4C7)

    val grisTexto =
        Color(0xFF94979C)

    val grisPlaceholder =
        Color(0xFF666A70)

    val grisBorde =
        Color(0xFF30343A)

    val verde =
        Color(0xFF39C26B)


    // =========================================================
    // SCROLL
    // =========================================================

    val scrollState =
        rememberScrollState()


    // =========================================================
    // COLORES CAMPOS
    // =========================================================

    val coloresCampos =
        OutlinedTextFieldDefaults.colors(

            focusedTextColor =
                blanco,

            unfocusedTextColor =
                blanco,

            focusedLabelColor =
                rojoFire,

            unfocusedLabelColor =
                grisTexto,

            focusedPlaceholderColor =
                grisPlaceholder,

            unfocusedPlaceholderColor =
                grisPlaceholder,

            cursorColor =
                rojoFire,

            focusedBorderColor =
                rojoFire,

            unfocusedBorderColor =
                grisBorde,

            focusedContainerColor =
                Color(0x14000000),

            unfocusedContainerColor =
                Color(0x0C000000)
        )


    // =========================================================
    // FONDO
    // =========================================================

    Box(

        modifier =
            Modifier
                .fillMaxSize()
                .background(
                    brush =
                        Brush.verticalGradient(
                            colors =
                                listOf(
                                    fondoPrincipal,
                                    fondoMedio,
                                    fondoInferior
                                )
                        )
                )
    ) {


        // =====================================================
        // RESPLANDOR ROJO
        // =====================================================

        Box(

            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(330.dp)
                    .align(
                        Alignment.BottomCenter
                    )
                    .background(
                        brush =
                            Brush.verticalGradient(
                                colors =
                                    listOf(
                                        Color.Transparent,
                                        Color(0x18FF0000),
                                        Color(0x35A00000)
                                    )
                            )
                    )
        )


        // =====================================================
        // CONTENIDO
        // =====================================================

        Column(

            modifier =
                Modifier
                    .fillMaxSize()
                    .verticalScroll(
                        scrollState
                    )
                    .padding(
                        horizontal = 24.dp,
                        vertical = 18.dp
                    ),

            horizontalAlignment =
                Alignment.CenterHorizontally,

            verticalArrangement =
                Arrangement.Center
        ) {


            // =================================================
            // LOGO
            // =================================================

            Image(

                painter =
                    painterResource(
                        id =
                            R.drawable.fire_alert_logo
                    ),

                contentDescription =
                    "Fire Alert",

                modifier =
                    Modifier.size(
                        if (modoRegistro) {
                            145.dp
                        } else {
                            205.dp
                        }
                    )
            )


            Spacer(
                modifier =
                    Modifier.height(5.dp)
            )


            // =================================================
            // TITULO
            // =================================================

            Text(

                text =
                    if (modoRegistro) {
                        "REGISTRAR CUARTEL"
                    } else {
                        "BIENVENIDO A"
                    },

                color =
                    grisClaro,

                fontSize =
                    14.sp,

                fontWeight =
                    FontWeight.Medium,

                letterSpacing =
                    2.2.sp
            )


            Spacer(
                modifier =
                    Modifier.height(3.dp)
            )


            Text(

                text =
                    "FIRE ALERT",

                color =
                    rojoFire,

                fontSize =
                    if (modoRegistro) {
                        28.sp
                    } else {
                        31.sp
                    },

                fontWeight =
                    FontWeight.Bold,

                letterSpacing =
                    4.sp
            )


            Spacer(
                modifier =
                    Modifier.height(4.dp)
            )


            Text(

                text =
                    if (modoRegistro) {
                        "CREÁ LA CUENTA DE TU CUARTEL"
                    } else {
                        "SISTEMA DE ALERTA Y NOTIFICACIÓN"
                    },

                color =
                    grisTexto,

                fontSize =
                    10.sp,

                letterSpacing =
                    1.1.sp,

                textAlign =
                    TextAlign.Center
            )


            Spacer(
                modifier =
                    Modifier.height(
                        if (modoRegistro) {
                            18.dp
                        } else {
                            27.dp
                        }
                    )
            )


            // =================================================
            // LOGIN
            // =================================================

            if (!modoRegistro) {


                // =================================================
                // CORREO
                // =================================================

                OutlinedTextField(

                    value =
                        email,

                    onValueChange = {
                        email = it
                    },

                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .height(60.dp),

                    singleLine =
                        true,

                    label = {
                        Text("Correo")
                    },

                    placeholder = {
                        Text("Ingresá tu correo")
                    },

                    keyboardOptions =
                        KeyboardOptions(
                            keyboardType =
                                KeyboardType.Email
                        ),

                    shape =
                        RoundedCornerShape(15.dp),

                    colors =
                        coloresCampos
                )


                Spacer(
                    modifier =
                        Modifier.height(13.dp)
                )


                // =================================================
                // CONTRASEÑA
                // =================================================

                OutlinedTextField(

                    value =
                        password,

                    onValueChange = {
                        password = it
                    },

                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .height(60.dp),

                    singleLine =
                        true,

                    label = {
                        Text("Contraseña")
                    },

                    placeholder = {
                        Text("Ingresá tu contraseña")
                    },

                    visualTransformation =
                        if (mostrarPassword) {
                            VisualTransformation.None
                        } else {
                            PasswordVisualTransformation()
                        },

                    trailingIcon = {

                        Box(

                            modifier =
                                Modifier
                                    .padding(
                                        end = 10.dp
                                    )
                                    .border(
                                        width = 1.dp,
                                        color =
                                            Color(0x445A5E63),
                                        shape =
                                            RoundedCornerShape(8.dp)
                                    )
                                    .clickable {

                                        mostrarPassword =
                                            !mostrarPassword
                                    }
                                    .padding(
                                        horizontal = 8.dp,
                                        vertical = 5.dp
                                    )
                        ) {

                            Text(

                                text =
                                    if (mostrarPassword) {
                                        "OCULTAR"
                                    } else {
                                        "VER"
                                    },

                                color =
                                    if (mostrarPassword) {
                                        rojoFire
                                    } else {
                                        grisClaro
                                    },

                                fontSize =
                                    9.sp,

                                fontWeight =
                                    FontWeight.Bold
                            )
                        }
                    },

                    keyboardOptions =
                        KeyboardOptions(
                            keyboardType =
                                KeyboardType.Password
                        ),

                    shape =
                        RoundedCornerShape(15.dp),

                    colors =
                        coloresCampos
                )


                Spacer(
                    modifier =
                        Modifier.height(25.dp)
                )


                // =================================================
                // INGRESAR
                // =================================================

                Button(

                    onClick = {

                        if (
                            email.trim().isEmpty() ||
                            password.isEmpty()
                        ) {

                            mensaje =
                                "Ingresá correo y contraseña"

                            return@Button
                        }


                        mensaje =
                            "Ingresando..."


                        auth
                            .signInWithEmailAndPassword(
                                email.trim(),
                                password
                            )
                            .addOnSuccessListener {

                                val usuario =
                                    auth.currentUser


                                if (usuario == null) {

                                    mensaje =
                                        "No se pudo obtener el usuario"

                                    return@addOnSuccessListener
                                }


                                val uid =
                                    usuario.uid


                                // =========================================
                                // BUSCAR USUARIO
                                // =========================================

                                db
                                    .collection("usuarios")
                                    .document(uid)
                                    .get()
                                    .addOnSuccessListener { documento ->


                                        if (!documento.exists()) {

                                            mensaje =
                                                "Usuario no registrado en FireAlert"

                                            auth.signOut()

                                            return@addOnSuccessListener
                                        }


                                        // =================================
                                        // ACTIVO
                                        // =================================

                                        val activo =
                                            documento.getBoolean(
                                                "activo"
                                            ) ?: false


                                        if (!activo) {

                                            mensaje =
                                                "Usuario desactivado"

                                            auth.signOut()

                                            return@addOnSuccessListener
                                        }


                                        // =================================
                                        // ROLES
                                        // =================================

                                        val rolesFirestore =
                                            documento.get("roles")


                                        val roles =
                                            if (
                                                rolesFirestore is List<*>
                                            ) {

                                                rolesFirestore
                                                    .filterIsInstance<String>()
                                                    .map { rol ->

                                                        normalizarRolLogin(
                                                            rol
                                                        )
                                                    }
                                                    .distinct()

                                            } else {

                                                emptyList()
                                            }


                                        // =================================
                                        // ROL ANTIGUO
                                        // =================================

                                        val rolesFinales =

                                            if (
                                                roles.isNotEmpty()
                                            ) {

                                                roles

                                            } else {

                                                val rolAntiguo =
                                                    documento
                                                        .getString("rol")


                                                if (
                                                    !rolAntiguo.isNullOrBlank()
                                                ) {

                                                    listOf(
                                                        normalizarRolLogin(
                                                            rolAntiguo
                                                        )
                                                    )

                                                } else {

                                                    emptyList()
                                                }
                                            }


                                        // =================================
                                        // SIN ROLES
                                        // =================================

                                        if (
                                            rolesFinales.isEmpty()
                                        ) {

                                            mensaje =
                                                "El usuario no tiene un rol asignado"

                                            auth.signOut()

                                            return@addOnSuccessListener
                                        }


                                        // =================================
                                        // ROLES PERMITIDOS
                                        // =================================

                                        val rolesPermitidos =
                                            setOf(

                                                "bombero",

                                                "cuartelero",

                                                "administrador",

                                                "administrador_general"
                                            )


                                        if (
                                            !rolesFinales.all {
                                                it in rolesPermitidos
                                            }
                                        ) {

                                            mensaje =
                                                "Rol no reconocido"

                                            auth.signOut()

                                            return@addOnSuccessListener
                                        }


                                        // =================================
                                        // CUARTEL
                                        // =================================

                                        val cuartelId =
                                            documento.getString(
                                                "cuartelId"
                                            )


                                        val esAdministradorGeneral =
                                            rolesFinales.contains(
                                                "administrador_general"
                                            )


                                        // =========================================
                                        // ADMINISTRADOR GENERAL
                                        // =========================================

                                        if (
                                            esAdministradorGeneral
                                        ) {

                                            Log.d(
                                                "FIREALERT",
                                                "Administrador general ingresando"
                                            )

                                            mensaje = ""

                                            onLoginSuccess(
                                                rolesFinales
                                            )

                                            return@addOnSuccessListener
                                        }


                                        // =========================================
                                        // USUARIO DE CUARTEL
                                        // =========================================

                                        if (
                                            cuartelId.isNullOrBlank()
                                        ) {

                                            mensaje =
                                                "Este usuario todavía no está asociado a un cuartel"

                                            auth.signOut()

                                            return@addOnSuccessListener
                                        }


                                        // =========================================
                                        // VERIFICAR CUARTEL
                                        // =========================================

                                        db
                                            .collection("cuarteles")
                                            .document(cuartelId)
                                            .get()
                                            .addOnSuccessListener { cuartelDocumento ->


                                                if (
                                                    !cuartelDocumento.exists()
                                                ) {

                                                    mensaje =
                                                        "El cuartel asociado no existe"

                                                    auth.signOut()

                                                    return@addOnSuccessListener
                                                }


                                                // =================================
                                                // CUARTEL ACTIVO
                                                // =================================

                                                val cuartelActivo =
                                                    cuartelDocumento
                                                        .getBoolean(
                                                            "activo"
                                                        ) ?: false


                                                if (
                                                    !cuartelActivo
                                                ) {

                                                    mensaje =
                                                        "El cuartel se encuentra inactivo. Comunicate con la administración de Fire Alert."

                                                    auth.signOut()

                                                    return@addOnSuccessListener
                                                }


                                                // =================================
                                                // SUSCRIPCIÓN
                                                // =================================

                                                val estadoSuscripcion =
                                                    cuartelDocumento
                                                        .getString(
                                                            "estadoSuscripcion"
                                                        )
                                                        ?.trim()
                                                        ?.lowercase()
                                                        ?: "pendiente"


                                                // =================================
                                                // VENCIDA
                                                // =================================

                                                if (
                                                    estadoSuscripcion ==
                                                    "vencida"
                                                ) {

                                                    mensaje =
                                                        "La suscripción de tu cuartel está vencida."

                                                    auth.signOut()

                                                    return@addOnSuccessListener
                                                }


                                                // =================================
                                                // SUSPENDIDA
                                                // =================================

                                                if (
                                                    estadoSuscripcion ==
                                                    "suspendida"
                                                ) {

                                                    mensaje =
                                                        "La suscripción de tu cuartel está suspendida."

                                                    auth.signOut()

                                                    return@addOnSuccessListener
                                                }


                                                // =================================
                                                // ESTADOS PERMITIDOS
                                                // =================================

                                                val estadosPermitidos =
                                                    setOf(
                                                        "activa",
                                                        "pendiente"
                                                    )


                                                if (
                                                    estadoSuscripcion !in
                                                    estadosPermitidos
                                                ) {

                                                    mensaje =
                                                        "El estado de suscripción de tu cuartel no permite el acceso."

                                                    auth.signOut()

                                                    return@addOnSuccessListener
                                                }


                                                // =================================
                                                // LOGIN CORRECTO
                                                // =================================

                                                Log.d(
                                                    "FIREALERT",
                                                    "Usuario ingresó correctamente"
                                                )

                                                Log.d(
                                                    "FIREALERT",
                                                    "Roles: $rolesFinales"
                                                )

                                                Log.d(
                                                    "FIREALERT",
                                                    "Cuartel: $cuartelId"
                                                )


                                                mensaje = ""

                                                onLoginSuccess(
                                                    rolesFinales
                                                )
                                            }
                                            .addOnFailureListener { error ->

                                                mensaje =
                                                    "No se pudo verificar el estado del cuartel: ${
                                                        error.message
                                                            ?: "Error desconocido"
                                                    }"

                                                auth.signOut()
                                            }
                                    }
                                    .addOnFailureListener { error ->

                                        mensaje =
                                            "Error consultando usuario: ${
                                                error.message
                                                    ?: "Error desconocido"
                                            }"

                                        auth.signOut()
                                    }
                            }
                            .addOnFailureListener { error ->

                                Log.e(
                                    "FIREALERT",
                                    "Error de inicio de sesión",
                                    error
                                )

                                mensaje =
                                    "Usuario o contraseña incorrectos"
                            }
                    },

                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .height(59.dp)
                            .shadow(
                                elevation = 13.dp,
                                shape =
                                    RoundedCornerShape(30.dp),
                                ambientColor =
                                    rojoOscuro,
                                spotColor =
                                    rojoFire
                            ),

                    shape =
                        RoundedCornerShape(30.dp),

                    colors =
                        ButtonDefaults.buttonColors(
                            containerColor =
                                rojoFire,

                            contentColor =
                                Color.White
                        )
                ) {

                    Text(

                        text =
                            "INGRESAR",

                        fontSize =
                            17.sp,

                        fontWeight =
                            FontWeight.Bold,

                        letterSpacing =
                            1.7.sp
                    )
                }


                Spacer(
                    modifier =
                        Modifier.height(17.dp)
                )


                // =================================================
                // RECUPERAR CONTRASEÑA
                // =================================================

                Text(

                    text =
                        if (enviandoRecuperacion) {
                            "ENVIANDO CORREO..."
                        } else {
                            "¿Olvidaste tu contraseña?"
                        },

                    color =
                        if (enviandoRecuperacion) {
                            Color(0xFF666A70)
                        } else {
                            Color(0xFFA6A9AD)
                        },

                    fontSize =
                        13.sp,

                    fontWeight =
                        FontWeight.Medium,

                    modifier =
                        Modifier.clickable(
                            enabled =
                                !enviandoRecuperacion
                        ) {

                            val correo =
                                email.trim()


                            if (correo.isEmpty()) {

                                mensaje =
                                    "Ingresá tu correo para recuperar la contraseña"

                                return@clickable
                            }


                            if (
                                !android.util.Patterns
                                    .EMAIL_ADDRESS
                                    .matcher(correo)
                                    .matches()
                            ) {

                                mensaje =
                                    "Ingresá un correo electrónico válido"

                                return@clickable
                            }


                            enviandoRecuperacion =
                                true

                            mensaje =
                                "Enviando correo de recuperación..."


                            auth
                                .sendPasswordResetEmail(
                                    correo
                                )
                                .addOnSuccessListener {

                                    enviandoRecuperacion =
                                        false

                                    mensaje =
                                        "Correo de recuperación enviado. Revisá tu bandeja de entrada."
                                }
                                .addOnFailureListener {

                                    enviandoRecuperacion =
                                        false

                                    mensaje =
                                        "No se pudo enviar el correo de recuperación"
                                }
                        }
                )


                Spacer(
                    modifier =
                        Modifier.height(20.dp)
                )


                // =================================================
                // REGISTRAR CUARTEL
                // =================================================

                Text(

                    text =
                        "¿Tu cuartel todavía no tiene Fire Alert?",

                    color =
                        grisTexto,

                    fontSize =
                        12.sp,

                    textAlign =
                        TextAlign.Center
                )


                Spacer(
                    modifier =
                        Modifier.height(7.dp)
                )


                Text(

                    text =
                        "REGISTRAR CUARTEL",

                    color =
                        rojoFire,

                    fontSize =
                        13.sp,

                    fontWeight =
                        FontWeight.Bold,

                    letterSpacing =
                        1.sp,

                    modifier =
                        Modifier.clickable {

                            mensaje = ""

                            modoRegistro =
                                true
                        }
                )
            }


            // =================================================
            // REGISTRO CUARTEL
            // =================================================

            else {


                // =================================================
                // NOMBRE CUARTEL
                // =================================================

                OutlinedTextField(

                    value =
                        nombreCuartel,

                    onValueChange = {
                        nombreCuartel = it
                    },

                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .height(58.dp),

                    singleLine =
                        true,

                    label = {
                        Text("🏢 Nombre del cuartel")
                    },

                    placeholder = {
                        Text("Ej: Bomberos Voluntarios Concordia")
                    },

                    shape =
                        RoundedCornerShape(14.dp),

                    colors =
                        coloresCampos
                )


                Spacer(
                    Modifier.height(9.dp)
                )


                // =================================================
                // LOCALIDAD
                // =================================================

                OutlinedTextField(

                    value =
                        localidadCuartel,

                    onValueChange = {
                        localidadCuartel = it
                    },

                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .height(58.dp),

                    singleLine =
                        true,

                    label = {
                        Text("📍 Localidad")
                    },

                    placeholder = {
                        Text("Ej: Concordia")
                    },

                    shape =
                        RoundedCornerShape(14.dp),

                    colors =
                        coloresCampos
                )


                Spacer(
                    Modifier.height(9.dp)
                )


                // =================================================
                // PROVINCIA
                // =================================================

                OutlinedTextField(

                    value =
                        provinciaCuartel,

                    onValueChange = {
                        provinciaCuartel = it
                    },

                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .height(58.dp),

                    singleLine =
                        true,

                    label = {
                        Text("🗺️ Provincia / Estado")
                    },

                    shape =
                        RoundedCornerShape(14.dp),

                    colors =
                        coloresCampos
                )


                Spacer(
                    Modifier.height(9.dp)
                )


                // =================================================
                // PAIS
                // =================================================

                OutlinedTextField(

                    value =
                        paisCuartel,

                    onValueChange = {
                        paisCuartel = it
                    },

                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .height(58.dp),

                    singleLine =
                        true,

                    label = {
                        Text("🌎 País")
                    },

                    placeholder = {
                        Text("Ej: Argentina")
                    },

                    shape =
                        RoundedCornerShape(14.dp),

                    colors =
                        coloresCampos
                )


                Spacer(
                    Modifier.height(9.dp)
                )


                // =================================================
                // ADMINISTRADOR
                // =================================================

                OutlinedTextField(

                    value =
                        nombreAdministrador,

                    onValueChange = {
                        nombreAdministrador = it
                    },

                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .height(58.dp),

                    singleLine =
                        true,

                    label = {
                        Text("👤 Nombre del administrador")
                    },

                    shape =
                        RoundedCornerShape(14.dp),

                    colors =
                        coloresCampos
                )


                Spacer(
                    Modifier.height(9.dp)
                )


                // =================================================
                // TELEFONO
                // =================================================

                OutlinedTextField(

                    value =
                        telefonoAdministrador,

                    onValueChange = {
                        telefonoAdministrador = it
                    },

                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .height(58.dp),

                    singleLine =
                        true,

                    label = {
                        Text("📱 Teléfono")
                    },

                    keyboardOptions =
                        KeyboardOptions(
                            keyboardType =
                                KeyboardType.Phone
                        ),

                    shape =
                        RoundedCornerShape(14.dp),

                    colors =
                        coloresCampos
                )


                Spacer(
                    Modifier.height(9.dp)
                )


                // =================================================
                // EMAIL
                // =================================================

                OutlinedTextField(

                    value =
                        emailAdministrador,

                    onValueChange = {
                        emailAdministrador = it
                    },

                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .height(58.dp),

                    singleLine =
                        true,

                    label = {
                        Text("📧 Correo del administrador")
                    },

                    keyboardOptions =
                        KeyboardOptions(
                            keyboardType =
                                KeyboardType.Email
                        ),

                    shape =
                        RoundedCornerShape(14.dp),

                    colors =
                        coloresCampos
                )


                Spacer(
                    Modifier.height(9.dp)
                )


                // =================================================
                // PASSWORD
                // =================================================

                OutlinedTextField(

                    value =
                        passwordAdministrador,

                    onValueChange = {
                        passwordAdministrador = it
                    },

                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .height(58.dp),

                    singleLine =
                        true,

                    label = {
                        Text("🔐 Contraseña")
                    },

                    visualTransformation =
                        if (mostrarPasswordRegistro) {
                            VisualTransformation.None
                        } else {
                            PasswordVisualTransformation()
                        },

                    trailingIcon = {

                        Text(

                            text =
                                if (mostrarPasswordRegistro) {
                                    "OCULTAR"
                                } else {
                                    "VER"
                                },

                            color =
                                if (mostrarPasswordRegistro) {
                                    rojoFire
                                } else {
                                    grisClaro
                                },

                            fontSize =
                                9.sp,

                            fontWeight =
                                FontWeight.Bold,

                            modifier =
                                Modifier
                                    .clickable {

                                        mostrarPasswordRegistro =
                                            !mostrarPasswordRegistro
                                    }
                                    .padding(10.dp)
                        )
                    },

                    keyboardOptions =
                        KeyboardOptions(
                            keyboardType =
                                KeyboardType.Password
                        ),

                    shape =
                        RoundedCornerShape(14.dp),

                    colors =
                        coloresCampos
                )


                Spacer(
                    Modifier.height(14.dp)
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

                        modifier =
                            Modifier.fillMaxWidth(),

                        color =
                            when {

                                mensaje.startsWith(
                                    "Cuartel creado"
                                ) -> {
                                    verde
                                }

                                mensaje.startsWith(
                                    "Creando"
                                ) -> {
                                    grisClaro
                                }

                                else -> {
                                    rojoSuave
                                }
                            },

                        fontSize =
                            12.sp,

                        textAlign =
                            TextAlign.Center
                    )


                    Spacer(
                        Modifier.height(10.dp)
                    )
                }


                // =================================================
                // BOTONES
                // =================================================

                Row(

                    modifier =
                        Modifier.fillMaxWidth()
                ) {


                    // =============================================
                    // VOLVER
                    // =============================================

                    Button(

                        onClick = {

                            if (
                                !registrandoCuartel
                            ) {

                                modoRegistro =
                                    false

                                mensaje =
                                    ""
                            }
                        },

                        enabled =
                            !registrandoCuartel,

                        modifier =
                            Modifier
                                .weight(1f)
                                .height(52.dp),

                        colors =
                            ButtonDefaults.buttonColors(
                                containerColor =
                                    Color.White.copy(
                                        alpha = 0.10f
                                    )
                            ),

                        shape =
                            RoundedCornerShape(15.dp)
                    ) {

                        Text("VOLVER")
                    }


                    Spacer(
                        Modifier.width(10.dp)
                    )


                    // =============================================
                    // CREAR CUARTEL
                    // =============================================

                    Button(

                        onClick = {

                            // =================================
                            // VALIDACIONES
                            // =================================

                            if (
                                nombreCuartel
                                    .trim()
                                    .isEmpty()
                            ) {

                                mensaje =
                                    "Ingresá el nombre del cuartel"

                                return@Button
                            }


                            if (
                                localidadCuartel
                                    .trim()
                                    .isEmpty()
                            ) {

                                mensaje =
                                    "Ingresá la localidad"

                                return@Button
                            }


                            if (
                                provinciaCuartel
                                    .trim()
                                    .isEmpty()
                            ) {

                                mensaje =
                                    "Ingresá la provincia o estado"

                                return@Button
                            }


                            if (
                                paisCuartel
                                    .trim()
                                    .isEmpty()
                            ) {

                                mensaje =
                                    "Ingresá el país"

                                return@Button
                            }


                            if (
                                nombreAdministrador
                                    .trim()
                                    .isEmpty()
                            ) {

                                mensaje =
                                    "Ingresá el nombre del administrador"

                                return@Button
                            }


                            if (
                                telefonoAdministrador
                                    .trim()
                                    .isEmpty()
                            ) {

                                mensaje =
                                    "Ingresá el teléfono"

                                return@Button
                            }


                            if (
                                emailAdministrador
                                    .trim()
                                    .isEmpty()
                            ) {

                                mensaje =
                                    "Ingresá el correo del administrador"

                                return@Button
                            }


                            if (
                                !android.util.Patterns
                                    .EMAIL_ADDRESS
                                    .matcher(
                                        emailAdministrador.trim()
                                    )
                                    .matches()
                            ) {

                                mensaje =
                                    "Ingresá un correo válido"

                                return@Button
                            }


                            if (
                                passwordAdministrador.length < 6
                            ) {

                                mensaje =
                                    "La contraseña debe tener mínimo 6 caracteres"

                                return@Button
                            }


                            registrandoCuartel =
                                true

                            mensaje =
                                "Creando cuartel..."


                            // =================================
                            // CREAR AUTH
                            // =================================

                            auth
                                .createUserWithEmailAndPassword(
                                    emailAdministrador.trim(),
                                    passwordAdministrador
                                )
                                .addOnSuccessListener { resultado ->

                                    val usuario =
                                        resultado.user


                                    if (
                                        usuario == null
                                    ) {

                                        registrandoCuartel =
                                            false

                                        mensaje =
                                            "No se pudo crear el administrador"

                                        return@addOnSuccessListener
                                    }


                                    val uid =
                                        usuario.uid


                                    // =================================
                                    // REFERENCIA CUARTEL
                                    // =================================

                                    val referenciaCuartel =
                                        db
                                            .collection("cuarteles")
                                            .document()


                                    val cuartelId =
                                        referenciaCuartel.id


                                    // =================================
                                    // DATOS CUARTEL
                                    // =================================

                                    val datosCuartel =
                                        hashMapOf<String, Any>(

                                            "cuartelId" to
                                                    cuartelId,

                                            "nombre" to
                                                    nombreCuartel.trim(),

                                            "localidad" to
                                                    localidadCuartel.trim(),

                                            "provincia" to
                                                    provinciaCuartel.trim(),

                                            "pais" to
                                                    paisCuartel.trim(),

                                            "creadoPor" to
                                                    uid,

                                            "administradorUid" to
                                                    uid,

                                            "activo" to
                                                    true,

                                            "plan" to
                                                    "prueba",

                                            "estadoSuscripcion" to
                                                    "pendiente"
                                        )


                                    // =================================
                                    // DATOS ADMINISTRADOR
                                    // =================================
                                    //
                                    // ESTE ES EL ADMINISTRADOR DEL CUARTEL.
                                    //
                                    // Tiene acceso a:
                                    //
                                    // - Administración del cuartel
                                    // - Funciones de administrador
                                    //
                                    // NO se le asigna automáticamente
                                    // administrador_general.
                                    //
                                    // =================================

                                    val datosAdministrador =
                                        hashMapOf<String, Any>(

                                            "nombre" to
                                                    nombreAdministrador.trim(),

                                            "email" to
                                                    emailAdministrador.trim(),

                                            "telefono" to
                                                    telefonoAdministrador.trim(),

                                            "rol" to
                                                    "administrador",

                                            "roles" to
                                                    listOf(
                                                        "administrador"
                                                    ),

                                            "activo" to
                                                    true,

                                            "cuartelId" to
                                                    cuartelId
                                        )


                                    // =================================
                                    // CREAR CUARTEL
                                    // =================================

                                    referenciaCuartel
                                        .set(datosCuartel)
                                        .addOnSuccessListener {


                                            // =================================
                                            // CREAR USUARIO
                                            // =================================

                                            db
                                                .collection("usuarios")
                                                .document(uid)
                                                .set(
                                                    datosAdministrador
                                                )
                                                .addOnSuccessListener {

                                                    registrandoCuartel =
                                                        false

                                                    mensaje =
                                                        "Cuartel creado correctamente"


                                                    // =========================
                                                    // LIMPIAR
                                                    // =========================

                                                    nombreCuartel =
                                                        ""

                                                    localidadCuartel =
                                                        ""

                                                    provinciaCuartel =
                                                        ""

                                                    paisCuartel =
                                                        ""

                                                    nombreAdministrador =
                                                        ""

                                                    telefonoAdministrador =
                                                        ""

                                                    emailAdministrador =
                                                        ""

                                                    passwordAdministrador =
                                                        ""


                                                    // =========================
                                                    // ENTRAR AUTOMÁTICAMENTE
                                                    // =========================

                                                    onLoginSuccess(
                                                        listOf(
                                                            "administrador"
                                                        )
                                                    )
                                                }
                                                .addOnFailureListener { error ->

                                                    registrandoCuartel =
                                                        false

                                                    mensaje =
                                                        "Cuartel creado, pero no se pudo guardar el administrador: ${
                                                            error.message
                                                        }"
                                                }
                                        }
                                        .addOnFailureListener { error ->

                                            registrandoCuartel =
                                                false

                                            mensaje =
                                                "No se pudo crear el cuartel: ${
                                                    error.message
                                                }"
                                        }
                                }
                                .addOnFailureListener { error ->

                                    registrandoCuartel =
                                        false

                                    mensaje =
                                        "No se pudo crear la cuenta: ${
                                            error.message
                                                ?: "Error desconocido"
                                        }"
                                }
                        },

                        enabled =
                            !registrandoCuartel,

                        modifier =
                            Modifier
                                .weight(1f)
                                .height(52.dp),

                        colors =
                            ButtonDefaults.buttonColors(
                                containerColor =
                                    rojoFire
                            ),

                        shape =
                            RoundedCornerShape(15.dp)
                    ) {

                        Text(

                            text =
                                if (registrandoCuartel) {
                                    "CREANDO..."
                                } else {
                                    "CREAR CUARTEL"
                                },

                            fontWeight =
                                FontWeight.Bold,

                            fontSize =
                                12.sp
                        )
                    }
                }


                Spacer(
                    modifier =
                        Modifier.height(25.dp)
                )
            }


            // =================================================
            // MENSAJE LOGIN
            // =================================================

            if (
                !modoRegistro &&
                mensaje.isNotEmpty()
            ) {

                Spacer(
                    modifier =
                        Modifier.height(13.dp)
                )


                Text(

                    text =
                        mensaje,

                    color =
                        when {

                            mensaje.startsWith(
                                "Correo de recuperación enviado"
                            ) -> {
                                verde
                            }

                            mensaje ==
                                    "Ingresando..." ||
                                    mensaje ==
                                    "Enviando correo de recuperación..." -> {
                                Color(0xFFBFC2C6)
                            }

                            else -> {
                                rojoSuave
                            }
                        },

                    fontSize =
                        13.sp,

                    textAlign =
                        TextAlign.Center
                )
            }
        }
    }
}


// =============================================================
// NORMALIZAR ROL
// =============================================================

private fun normalizarRolLogin(
    rol: String
): String {

    return rol
        .trim()
        .lowercase()
        .replace(
            "administrador general",
            "administrador_general"
        )
}
