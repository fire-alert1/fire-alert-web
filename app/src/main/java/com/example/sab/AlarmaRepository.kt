package com.example.sab

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore


class AlarmaRepository {

    private val db =
        FirebaseFirestore.getInstance()

    private val auth =
        FirebaseAuth.getInstance()


    // =========================================================
    // REGISTRAR ALARMA
    // =========================================================

    fun registrarAlarma(
        motivo: String,
        cuartelId: String,
        onSuccess: (String) -> Unit,
        onError: (Exception) -> Unit
    ) {

        val usuarioActual =
            auth.currentUser


        // =====================================================
        // COMPROBAR USUARIO
        // =====================================================

        if (usuarioActual == null) {

            println(
                "FIREALERT ERROR: No hay usuario conectado"
            )

            onError(
                Exception(
                    "No hay ningún usuario conectado"
                )
            )

            return
        }


        val uid =
            usuarioActual.uid


        println(
            "FIREALERT: Usuario conectado UID = $uid"
        )


        // =====================================================
        // COMPROBAR CUARTEL
        // =====================================================

        if (
            cuartelId.isBlank()
        ) {

            println(
                "FIREALERT ERROR: cuartelId vacío"
            )

            onError(
                Exception(
                    "No se encontró el cuartel del usuario"
                )
            )

            return
        }


        println(
            "FIREALERT: Cuartel ID = $cuartelId"
        )


        // =====================================================
        // BUSCAR USUARIO
        // =====================================================

        db.collection("usuarios")
            .document(uid)
            .get()

            .addOnSuccessListener { documento ->

                println(
                    "FIREALERT: Documento usuario encontrado = ${documento.exists()}"
                )


                // =================================================
                // NOMBRE DEL USUARIO
                // =================================================

                val nombre =
                    documento.getString("nombre")
                        ?: usuarioActual.email
                        ?: "Usuario desconocido"


                println(
                    "FIREALERT: Nombre de alarma = $nombre"
                )


                // =================================================
                // MOTIVO
                // =================================================

                val motivoFinal =
                    motivo.trim().ifEmpty {
                        "Sin especificar"
                    }


                println(
                    "FIREALERT: Motivo = $motivoFinal"
                )


                // =================================================
                // DATOS DE LA ALARMA
                // =================================================

                val datosAlarma =
                    hashMapOf(

                        "uid" to uid,

                        "usuario" to nombre,

                        "motivo" to motivoFinal,

                        "estado" to "ACTIVA",

                        "cuartelId" to cuartelId,

                        "fechaHora" to
                                FieldValue.serverTimestamp()
                    )


                println(
                    "FIREALERT: Intentando guardar alarma..."
                )


                println(
                    "FIREALERT: Guardando cuartelId = $cuartelId"
                )


                // =================================================
                // GUARDAR EN FIRESTORE
                // =================================================

                db.collection("alarmas")
                    .add(datosAlarma)

                    .addOnSuccessListener { documentoAlarma ->

                        println(
                            "FIREALERT OK: Alarma guardada. ID = ${documentoAlarma.id}"
                        )


                        // =========================================
                        // DEVOLVER ID
                        // =========================================

                        onSuccess(
                            documentoAlarma.id
                        )
                    }

                    .addOnFailureListener { exception ->

                        println(
                            "FIREALERT ERROR AL GUARDAR: ${exception.message}"
                        )

                        onError(
                            exception
                        )
                    }
            }

            .addOnFailureListener { exception ->

                println(
                    "FIREALERT ERROR AL BUSCAR USUARIO: ${exception.message}"
                )

                onError(
                    exception
                )
            }
    }


    // =========================================================
    // DESACTIVAR ALARMA
    // =========================================================

    fun desactivarAlarma(
        alarmaId: String,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {

        db.collection("alarmas")
            .document(alarmaId)
            .update(
                "estado",
                "DESACTIVADA"
            )

            .addOnSuccessListener {

                println(
                    "FIREALERT: Alarma desactivada correctamente"
                )

                onSuccess()
            }

            .addOnFailureListener { exception ->

                println(
                    "FIREALERT ERROR AL DESACTIVAR: ${exception.message}"
                )

                onError(
                    exception
                )
            }
    }


    // =========================================================
    // ELIMINAR TODO EL HISTORIAL DEL CUARTEL
    //
    // IMPORTANTE:
    // Solamente elimina las alarmas pertenecientes al
    // cuartel indicado.
    // =========================================================

    fun eliminarTodoElHistorial(
        cuartelId: String,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {

        if (
            cuartelId.isBlank()
        ) {

            onError(
                Exception(
                    "No se encontró el cuartel"
                )
            )

            return
        }


        db.collection("alarmas")
            .whereEqualTo(
                "cuartelId",
                cuartelId
            )
            .get()

            .addOnSuccessListener { snapshot ->

                if (
                    snapshot.isEmpty
                ) {

                    onSuccess()

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
                // EJECUTAR BORRADO
                // =================================================

                batch.commit()

                    .addOnSuccessListener {

                        println(
                            "FIREALERT: Historial del cuartel eliminado correctamente"
                        )

                        onSuccess()
                    }

                    .addOnFailureListener { exception ->

                        println(
                            "FIREALERT ERROR AL ELIMINAR HISTORIAL: ${exception.message}"
                        )

                        onError(
                            exception
                        )
                    }
            }

            .addOnFailureListener { exception ->

                println(
                    "FIREALERT ERROR AL OBTENER HISTORIAL: ${exception.message}"
                )

                onError(
                    exception
                )
            }
    }
}