package com.example.sab

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log

import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat

import com.example.sab.ui.AdminGeneralScreen
import com.example.sab.ui.AdminScreen
import com.example.sab.ui.HomeScreen
import com.example.sab.ui.HistorialScreen
import com.example.sab.ui.theme.SABTheme

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging


class MainActivity : ComponentActivity() {

    // =========================================================
    // FIREBASE
    // =========================================================

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore


    // =========================================================
    // PERMISO DE NOTIFICACIONES
    // =========================================================

    private val pedirPermisoNotificaciones =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { permitido ->

            Log.d(
                "FIREALERT",
                "Permiso de notificaciones: $permitido"
            )
        }


    // =========================================================
    // ON CREATE
    // =========================================================

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {

        super.onCreate(
            savedInstanceState
        )


        // =====================================================
        // FIREBASE
        // =====================================================

        auth =
            FirebaseAuth.getInstance()

        db =
            FirebaseFirestore.getInstance()


        // =====================================================
        // NOTIFICACIONES
        // =====================================================

        solicitarPermisoNotificaciones()


        // =====================================================
        // TOKEN FCM
        // =====================================================

        obtenerTokenFCM()


        // =====================================================
        // INTERFAZ
        // =====================================================

        setContent {

            SABTheme {

                Surface(
                    modifier =
                        Modifier.fillMaxSize()
                ) {

                    // =================================================
                    // SESIÓN
                    // =================================================

                    var usuarioLogueado by remember {

                        mutableStateOf(
                            false
                        )
                    }


                    // =================================================
                    // ROLES
                    // =================================================

                    var rolesUsuario by remember {

                        mutableStateOf<List<String>>(
                            emptyList()
                        )
                    }


                    // =================================================
                    // CUARTEL ID
                    // =================================================

                    var cuartelIdUsuario by remember {

                        mutableStateOf(
                            ""
                        )
                    }


                    // =================================================
                    // ESTADO DEL CUARTEL
                    // =================================================

                    var cuartelActivo by remember {

                        mutableStateOf(
                            false
                        )
                    }


                    var estadoSuscripcion by remember {

                        mutableStateOf(
                            "pendiente"
                        )
                    }


                    // =================================================
                    // PANTALLA ACTUAL (SIEMPRE INICIA EN INICIO)
                    // =================================================

                    var pantallaActual by remember {

                        mutableStateOf(
                            "inicio"
                        )
                    }


                    // =================================================
                    // VERIFICANDO SESIÓN
                    // =================================================

                    var verificandoSesion by remember {

                        mutableStateOf(
                            true
                        )
                    }


                    // =================================================
                    // RECUPERAR SESIÓN
                    // =================================================

                    LaunchedEffect(Unit) {

                        verificarSesion(

                            onSesionValida = {
                                    roles,
                                    activoCuartel,
                                    suscripcion,
                                    cuartelId ->

                                rolesUsuario =
                                    roles

                                cuartelActivo =
                                    activoCuartel

                                estadoSuscripcion =
                                    suscripcion

                                cuartelIdUsuario =
                                    cuartelId

                                // =====================================
                                // SUSCRIPCIÓN AUTOMÁTICA AL CUARTEL
                                // =====================================
                                suscribirAlCuartel(cuartelId)

                                usuarioLogueado =
                                    true

                                // La pantalla inicial siempre será la HomeScreen principal con la alarma
                                pantallaActual =
                                    "inicio"

                                verificandoSesion =
                                    false
                            },

                            onSesionInvalida = {

                                rolesUsuario =
                                    emptyList()

                                cuartelIdUsuario =
                                    ""

                                cuartelActivo =
                                    false

                                estadoSuscripcion =
                                    "pendiente"

                                usuarioLogueado =
                                    false

                                pantallaActual =
                                    "inicio"

                                verificandoSesion =
                                    false
                            }
                        )
                    }


                    // =================================================
                    // VERIFICANDO
                    // =================================================

                    if (
                        verificandoSesion
                    ) {

                        // Firebase verificando sesión.

                    } else {


                        // =================================================
                        // LOGIN
                        // =================================================

                        if (
                            !usuarioLogueado
                        ) {

                            LoginScreen(

                                onLoginSuccess = { roles ->

                                    val rolesNormalizados =
                                        roles
                                            .map {
                                                normalizarRol(
                                                    it
                                                )
                                            }
                                            .distinct()


                                    Log.d(
                                        "FIREALERT",
                                        "Roles después del login: $rolesNormalizados"
                                    )


                                    rolesUsuario =
                                        rolesNormalizados


                                    usuarioLogueado =
                                        true


                                    pantallaActual =
                                        "inicio"


                                    // -------------------------------------------------
                                    // ACTUALIZAR ESTADO DEL CUARTEL
                                    // -------------------------------------------------

                                    verificarEstadoCuartel(

                                        rolesNormalizados,

                                        onResultado = {
                                                activo,
                                                suscripcion,
                                                cuartelId ->

                                            cuartelActivo =
                                                activo

                                            estadoSuscripcion =
                                                suscripcion

                                            cuartelIdUsuario =
                                                cuartelId

                                            // =====================================
                                            // SUSCRIPCIÓN AUTOMÁTICA AL LOGUEARSE
                                            // =====================================
                                            suscribirAlCuartel(cuartelId)
                                        }
                                    )
                                }
                            )


                        } else {


                            // =================================================
                            // USUARIO LOGUEADO
                            // =================================================

                            when (
                                pantallaActual
                            ) {


                                // =========================================
                                // ADMINISTRACIÓN NORMAL
                                // =========================================

                                "admin" -> {

                                    if (
                                        rolesUsuario.contains(
                                            "administrador"
                                        )
                                    ) {

                                        AdminScreen(

                                            onVolver = {

                                                pantallaActual =
                                                    "inicio"
                                            }
                                        )

                                    } else {

                                        pantallaActual =
                                            "inicio"
                                    }
                                }


                                // =========================================
                                // ADMINISTRACIÓN GENERAL
                                // =========================================

                                "admin_general" -> {

                                    if (
                                        rolesUsuario.contains(
                                            "administrador_general"
                                        )
                                    ) {

                                        AdminGeneralScreen(

                                            onVolver = {

                                                pantallaActual =
                                                    "inicio"
                                            }
                                        )

                                    } else {

                                        Log.w(
                                            "FIREALERT",
                                            "Intento de acceso al Panel General sin permisos"
                                        )

                                        pantallaActual =
                                            "inicio"
                                    }
                                }


                                // =========================================
                                // HISTORIAL
                                // =========================================

                                "historial" -> {

                                    if (
                                        rolesUsuario.contains(
                                            "cuartelero"
                                        ) ||
                                        rolesUsuario.contains(
                                            "administrador"
                                        )
                                    ) {

                                        HistorialScreen(

                                            cuartelId = cuartelIdUsuario,

                                            onVolver = {
                                                pantallaActual = "inicio"
                                            }
                                        )

                                    } else {

                                        Log.w(
                                            "FIREALERT",
                                            "Intento de acceso al historial sin permisos"
                                        )

                                        pantallaActual =
                                            "inicio"
                                    }
                                }


                                // =========================================
                                // INICIO (PANTALLA PRINCIPAL CON BOTÓN DE ALARMA)
                                // =========================================

                                else -> {

                                    HomeScreen(

                                        // =====================================
                                        // ROLES
                                        // =====================================

                                        esCuartelero =
                                            rolesUsuario.contains(
                                                "cuartelero"
                                            ),

                                        esAdministrador =
                                            rolesUsuario.contains(
                                                "administrador"
                                            ),

                                        esAdministradorGeneral =
                                            rolesUsuario.contains(
                                                "administrador_general"
                                            ),


                                        // =====================================
                                        // ESTADO COMERCIAL DEL CUARTEL
                                        // =====================================

                                        cuartelActivo =
                                            cuartelActivo,

                                        estadoSuscripcion =
                                            estadoSuscripcion,


                                        // =====================================
                                        // CUARTEL ID
                                        // =====================================

                                        cuartelId =
                                            cuartelIdUsuario,


                                        // =====================================
                                        // ADMINISTRACIÓN NORMAL
                                        // =====================================

                                        onAbrirAdministracion = {

                                            if (
                                                rolesUsuario.contains(
                                                    "administrador"
                                                )
                                            ) {

                                                pantallaActual =
                                                    "admin"
                                            }
                                        },


                                        // =====================================
                                        // ADMINISTRACIÓN GENERAL
                                        // =====================================

                                        onAbrirPanelAdministradorGeneral = {

                                            if (
                                                rolesUsuario.contains(
                                                    "administrador_general"
                                                )
                                            ) {

                                                pantallaActual =
                                                    "admin_general"

                                            } else {

                                                Log.w(
                                                    "FIREALERT",
                                                    "Acceso al Panel General rechazado"
                                                )
                                            }
                                        },


                                        // =====================================
                                        // HISTORIAL
                                        // =====================================

                                        onAbrirHistorial = {

                                            if (
                                                rolesUsuario.contains(
                                                    "cuartelero"
                                                ) ||
                                                rolesUsuario.contains(
                                                    "administrador"
                                                )
                                            ) {

                                                pantallaActual =
                                                    "historial"

                                            } else {

                                                Log.w(
                                                    "FIREALERT",
                                                    "Acceso al historial rechazado"
                                                )
                                            }
                                        },


                                        // =====================================
                                        // CERRAR SESIÓN
                                        // =====================================

                                        onCerrarSesion = {

                                            Log.d(
                                                "FIREALERT",
                                                "Cerrando sesión del usuario"
                                            )

                                            desuscribirDelCuartel(cuartelIdUsuario)

                                            cerrarSesion()


                                            // =================================
                                            // LIMPIAR ESTADO LOCAL DE SESIÓN
                                            // =================================

                                            rolesUsuario =
                                                emptyList()

                                            cuartelIdUsuario =
                                                ""

                                            cuartelActivo =
                                                false

                                            estadoSuscripcion =
                                                "pendiente"

                                            usuarioLogueado =
                                                false

                                            pantallaActual =
                                                "inicio"
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }


    // =========================================================
    // SUSCRIPCIÓN Y DESUSCRIPCIÓN EXCLUSIVA POR CUARTEL
    // =========================================================

    private fun suscribirAlCuartel(cuartelId: String) {
        if (cuartelId.isNotBlank()) {
            val topicCuartel = "cuartel_$cuartelId"

            FirebaseMessaging.getInstance().unsubscribeFromTopic("bomberos")

            FirebaseMessaging.getInstance().subscribeToTopic(topicCuartel)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Log.d("FIREALERT_FCM", "Suscrito correctamente al topic: $topicCuartel")
                    } else {
                        Log.e("FIREALERT_FCM", "Error al suscribirse al topic: $topicCuartel", task.exception)
                    }
                }
        }
    }

    private fun desuscribirDelCuartel(cuartelId: String) {
        if (cuartelId.isNotBlank()) {
            val topicCuartel = "cuartel_$cuartelId"
            FirebaseMessaging.getInstance().unsubscribeFromTopic(topicCuartel)
            Log.d("FIREALERT_FCM", "Desuscrito del topic: $topicCuartel")
        }
    }


    // =========================================================
    // VERIFICAR SESIÓN
    // =========================================================

    private fun verificarSesion(

        onSesionValida: (
            List<String>,
            Boolean,
            String,
            String
        ) -> Unit,

        onSesionInvalida: () -> Unit

    ) {

        val usuario =
            auth.currentUser

        if (
            usuario == null
        ) {

            onSesionInvalida()

            return
        }


        val uid =
            usuario.uid


        db
            .collection(
                "usuarios"
            )
            .document(
                uid
            )
            .get()

            .addOnSuccessListener { documento ->

                if (
                    !documento.exists()
                ) {

                    Log.w(
                        "FIREALERT",
                        "El usuario autenticado no existe en Firestore"
                    )

                    auth.signOut()

                    onSesionInvalida()

                    return@addOnSuccessListener
                }


                val activo =
                    documento.getBoolean(
                        "activo"
                    ) ?: false


                if (
                    !activo
                ) {

                    Log.w(
                        "FIREALERT",
                        "Usuario desactivado"
                    )

                    auth.signOut()

                    onSesionInvalida()

                    return@addOnSuccessListener
                }


                val rolesFirestore =
                    documento.get(
                        "roles"
                    )


                val roles =

                    if (
                        rolesFirestore is List<*>
                    ) {

                        rolesFirestore
                            .filterIsInstance<String>()
                            .map {

                                normalizarRol(
                                    it
                                )
                            }
                            .distinct()

                    } else {

                        emptyList()
                    }


                val rolesFinales =

                    if (
                        roles.isNotEmpty()
                    ) {

                        roles

                    } else {

                        val rolAntiguo =
                            documento.getString(
                                "rol"
                            )


                        if (
                            !rolAntiguo.isNullOrBlank()
                        ) {

                            listOf(
                                normalizarRol(
                                    rolAntiguo
                                )
                            )

                        } else {

                            emptyList()
                        }
                    }


                if (
                    rolesFinales.isEmpty()
                ) {

                    Log.w(
                        "FIREALERT",
                        "Usuario sin roles"
                    )

                    auth.signOut()

                    onSesionInvalida()

                    return@addOnSuccessListener
                }


                val rolesPermitidos =
                    setOf(

                        "bombero",

                        "cuartelero",

                        "administrador",

                        "administrador_general"
                    )


                val rolesValidos =
                    rolesFinales.all { rol ->

                        rol in rolesPermitidos
                    }


                if (
                    !rolesValidos
                ) {

                    Log.w(
                        "FIREALERT",
                        "Rol no reconocido: $rolesFinales"
                    )

                    auth.signOut()

                    onSesionInvalida()

                    return@addOnSuccessListener
                }


                val cuartelId =
                    documento.getString(
                        "cuartelId"
                    )


                val esAdministradorGeneral =
                    rolesFinales.contains(
                        "administrador_general"
                    )


                if (
                    !esAdministradorGeneral &&
                    cuartelId.isNullOrBlank()
                ) {

                    Log.w(
                        "FIREALERT",
                        "Usuario sin cuartelId"
                    )

                    auth.signOut()

                    onSesionInvalida()

                    return@addOnSuccessListener
                }


                if (
                    esAdministradorGeneral
                ) {

                    Log.d(
                        "FIREALERT",
                        "Administrador general detectado"
                    )

                    Log.d(
                        "FIREALERT",
                        "================================"
                    )

                    Log.d(
                        "FIREALERT",
                        "SESIÓN RECUPERADA"
                    )

                    Log.d(
                        "FIREALERT",
                        "Usuario: ${usuario.email}"
                    )

                    Log.d(
                        "FIREALERT",
                        "Roles: $rolesFinales"
                    )

                    Log.d(
                        "FIREALERT",
                        "CuartelId: $cuartelId"
                    )

                    Log.d(
                        "FIREALERT",
                        "================================"
                    )


                    onSesionValida(
                        rolesFinales,
                        true,
                        "activa",
                        cuartelId ?: ""
                    )

                    return@addOnSuccessListener
                }


                db
                    .collection(
                        "cuarteles"
                    )
                    .document(
                        cuartelId!!
                    )
                    .get()

                    .addOnSuccessListener { cuartelDocumento ->

                        if (
                            !cuartelDocumento.exists()
                        ) {

                            Log.w(
                                "FIREALERT",
                                "El cuartel no existe: $cuartelId"
                            )

                            auth.signOut()

                            onSesionInvalida()

                            return@addOnSuccessListener
                        }


                        val activoCuartel =
                            cuartelDocumento.getBoolean(
                                "activo"
                            ) ?: false


                        val suscripcion =
                            cuartelDocumento.getString(
                                "estadoSuscripcion"
                            )
                                ?.trim()
                                ?.lowercase()
                                ?: "pendiente"


                        Log.d(
                            "FIREALERT",
                            "================================"
                        )

                        Log.d(
                            "FIREALERT",
                            "SESIÓN RECUPERADA"
                        )

                        Log.d(
                            "FIREALERT",
                            "Usuario: ${usuario.email}"
                        )

                        Log.d(
                            "FIREALERT",
                            "Roles: $rolesFinales"
                        )

                        Log.d(
                            "FIREALERT",
                            "CuartelId: $cuartelId"
                        )

                        Log.d(
                            "FIREALERT",
                            "Cuartel activo: $activoCuartel"
                        )

                        Log.d(
                            "FIREALERT",
                            "Suscripción: $suscripcion"
                        )

                        Log.d(
                            "FIREALERT",
                            "================================"
                        )


                        onSesionValida(

                            rolesFinales,

                            activoCuartel,

                            suscripcion,

                            cuartelId
                        )
                    }

                    .addOnFailureListener { error ->

                        Log.e(
                            "FIREALERT",
                            "Error obteniendo estado del cuartel",
                            error
                        )

                        onSesionInvalida()
                    }
            }

            .addOnFailureListener { error ->

                Log.e(
                    "FIREALERT",
                    "Error recuperando sesión",
                    error
                )

                onSesionInvalida()
            }
    }


    // =========================================================
    // VERIFICAR ESTADO DEL CUARTEL
    // =========================================================

    private fun verificarEstadoCuartel(

        roles: List<String>,

        onResultado: (
            Boolean,
            String,
            String
        ) -> Unit

    ) {

        if (
            roles.contains(
                "administrador_general"
            )
        ) {

            onResultado(
                true,
                "activa",
                ""
            )

            return
        }


        val usuario =
            auth.currentUser


        if (
            usuario == null
        ) {

            onResultado(
                false,
                "pendiente",
                ""
            )

            return
        }


        db
            .collection(
                "usuarios"
            )
            .document(
                usuario.uid
            )
            .get()

            .addOnSuccessListener { documento ->

                val cuartelId =
                    documento.getString(
                        "cuartelId"
                    )


                if (
                    cuartelId.isNullOrBlank()
                ) {

                    onResultado(
                        false,
                        "pendiente",
                        ""
                    )

                    return@addOnSuccessListener
                }


                db
                    .collection(
                        "cuarteles"
                    )
                    .document(
                        cuartelId
                    )
                    .get()

                    .addOnSuccessListener { cuartelDocumento ->

                        val activo =
                            cuartelDocumento.getBoolean(
                                "activo"
                            ) ?: false


                        val suscripcion =
                            cuartelDocumento.getString(
                                "estadoSuscripcion"
                            )
                                ?.trim()
                                ?.lowercase()
                                ?: "pendiente"


                        onResultado(
                            activo,
                            suscripcion,
                            cuartelId
                        )
                    }

                    .addOnFailureListener {

                        onResultado(
                            false,
                            "pendiente",
                            cuartelId
                        )
                    }
            }

            .addOnFailureListener {

                onResultado(
                    false,
                    "pendiente",
                    ""
                )
            }
    }


    // =========================================================
    // NORMALIZAR ROL
    // =========================================================

    private fun normalizarRol(
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


    // =========================================================
    // PERMISO NOTIFICACIONES
    // =========================================================

    private fun solicitarPermisoNotificaciones() {

        if (
            Build.VERSION.SDK_INT >=
            Build.VERSION_CODES.TIRAMISU
        ) {

            val permiso =
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                )


            if (
                permiso !=
                PackageManager.PERMISSION_GRANTED
            ) {

                pedirPermisoNotificaciones.launch(
                    Manifest.permission.POST_NOTIFICATIONS
                )
            }
        }
    }


    // =========================================================
    // TOKEN FCM
    // =========================================================

    private fun obtenerTokenFCM() {

        FirebaseMessaging
            .getInstance()
            .token
            .addOnCompleteListener { tarea ->

                if (
                    !tarea.isSuccessful
                ) {

                    Log.e(
                        "FIREALERT_FCM",
                        "No se pudo obtener token FCM",
                        tarea.exception
                    )

                    return@addOnCompleteListener
                }


                val token =
                    tarea.result


                Log.d(
                    "FIREALERT_FCM",
                    "TOKEN FCM: $token"
                )
            }
    }


    // =========================================================
    // CERRAR SESIÓN
    // =========================================================

    private fun cerrarSesion() {

        auth.signOut()
    }
}