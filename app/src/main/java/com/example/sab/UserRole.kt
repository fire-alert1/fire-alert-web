package com.example.sab

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

// ============================================================
// OBTENER TODOS LOS ROLES DEL USUARIO
// ============================================================

fun obtenerRolesUsuario(
    resultado: (List<String>) -> Unit
) {

    val usuario = FirebaseAuth.getInstance().currentUser

    if (usuario == null) {

        Log.d("SAB", "NO HAY USUARIO LOGUEADO")

        resultado(emptyList())

        return
    }

    val uid = usuario.uid

    Log.d("SAB", "UID ACTUAL: $uid")
    Log.d("SAB", "CORREO: ${usuario.email}")

    FirebaseFirestore.getInstance()
        .collection("usuarios")
        .document(uid)
        .get()
        .addOnSuccessListener { documento ->

            Log.d(
                "SAB",
                "DOCUMENTO BUSCADO: ${documento.id}"
            )

            Log.d(
                "SAB",
                "EXISTE DOCUMENTO: ${documento.exists()}"
            )

            Log.d(
                "SAB",
                "DATOS FIREBASE: ${documento.data}"
            )

            if (!documento.exists()) {

                Log.d(
                    "SAB",
                    "NO EXISTE EL DOCUMENTO"
                )

                resultado(emptyList())

                return@addOnSuccessListener
            }

            // =================================================
            // LEER ROLES NUEVOS
            // =================================================

            val rolesFirestore =
                documento.get("roles")

            val roles =
                if (rolesFirestore is List<*>) {

                    rolesFirestore
                        .filterIsInstance<String>()
                        .map {
                            it.trim().lowercase()
                        }
                        .distinct()

                } else {

                    emptyList()
                }

            // =================================================
            // COMPATIBILIDAD CON CAMPO "rol" ANTIGUO
            // =================================================

            val rolesFinales =
                if (roles.isNotEmpty()) {

                    roles

                } else {

                    val rolAntiguo =
                        documento
                            .getString("rol")
                            ?.trim()
                            ?.lowercase()

                    if (!rolAntiguo.isNullOrEmpty()) {

                        listOf(rolAntiguo)

                    } else {

                        emptyList()
                    }
                }

            Log.d(
                "SAB",
                "ROLES FINALES: $rolesFinales"
            )

            // =================================================
            // ROLES PERMITIDOS
            // =================================================

            val rolesPermitidos =
                setOf(
                    "bombero",
                    "cuartelero",
                    "administrador"
                )

            val rolesValidos =
                rolesFinales.all {
                    it in rolesPermitidos
                }

            if (!rolesValidos) {

                Log.d(
                    "SAB",
                    "ROLES NO VÁLIDOS: $rolesFinales"
                )

                resultado(emptyList())

                return@addOnSuccessListener
            }

            resultado(rolesFinales)
        }

        .addOnFailureListener { error ->

            Log.d(
                "SAB",
                "ERROR FIRESTORE: ${error.message}"
            )

            resultado(emptyList())
        }
}


// ============================================================
// OBTENER ROL PRINCIPAL
// COMPATIBILIDAD CON HOMESCREEN
// ============================================================

fun obtenerRolUsuario(
    resultado: (String) -> Unit
) {

    obtenerRolesUsuario { roles ->

        if (roles.isNotEmpty()) {

            // Si tiene varios roles, usamos el primero
            resultado(roles.first())

        } else {

            resultado("SIN_ROL")
        }
    }
}