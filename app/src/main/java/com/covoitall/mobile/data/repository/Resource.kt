package com.covoitall.mobile.data.repository

import com.google.gson.JsonParser
import com.google.gson.JsonSyntaxException
import retrofit2.Response
import java.io.IOException
import java.net.SocketTimeoutException

/** Résultat d'une opération de données, consommé par les ViewModels. */
sealed class Resource<out T> {
    data class Success<T>(val data: T) : Resource<T>()
    data class Error(val message: String, val code: Int? = null) : Resource<Nothing>()
}

/**
 * Encapsule chaque appel Retrofit : erreurs réseau (try/catch) et erreurs HTTP 4xx/5xx
 * (vérification de response.isSuccessful), avec le message renvoyé par FastAPI.
 */
suspend fun <T> safeApiCall(call: suspend () -> Response<T>): Resource<T> {
    return try {
        val response = call()
        if (response.isSuccessful) {
            val body = response.body()
            @Suppress("UNCHECKED_CAST")
            when {
                body != null -> Resource.Success(body)
                response.code() == 204 -> Resource.Success(Unit as T)
                else -> Resource.Error("Réponse vide du serveur", response.code())
            }
        } else {
            Resource.Error(httpErrorMessage(response), response.code())
        }
    } catch (_: SocketTimeoutException) {
        Resource.Error("Le serveur met trop de temps à répondre. Réessayez.")
    } catch (_: IOException) {
        Resource.Error("Impossible de joindre le serveur. Vérifiez votre connexion internet.")
    } catch (_: JsonSyntaxException) {
        Resource.Error("Réponse inattendue du serveur.")
    } catch (exception: Exception) {
        Resource.Error(exception.message ?: "Une erreur inattendue est survenue.")
    }
}

/** Extrait le champ « detail » de FastAPI (texte ou liste d'erreurs de validation). */
private fun httpErrorMessage(response: Response<*>): String {
    val fallback = when (response.code()) {
        401 -> "Session expirée ou identifiants invalides."
        403 -> "Action non autorisée."
        404 -> "Élément introuvable."
        409 -> "Conflit avec l'état actuel des données."
        in 500..599 -> "Le serveur rencontre un problème (${response.code()}). Réessayez plus tard."
        else -> "Erreur ${response.code()}"
    }
    val raw = try { response.errorBody()?.string() } catch (_: IOException) { null } ?: return fallback
    return try {
        val detail = JsonParser.parseString(raw).asJsonObject.get("detail") ?: return fallback
        when {
            detail.isJsonPrimitive -> detail.asString
            detail.isJsonArray -> detail.asJsonArray.joinToString("\n") { item ->
                val obj = item.asJsonObject
                val field = obj.getAsJsonArray("loc")?.lastOrNull()?.asString
                val message = obj.get("msg")?.asString ?: "valeur invalide"
                if (field != null) "$field : $message" else message
            }
            else -> fallback
        }
    } catch (_: Exception) {
        fallback
    }
}

/** Transforme un succès sans modifier une erreur. */
inline fun <T, R> Resource<T>.map(transform: (T) -> R): Resource<R> = when (this) {
    is Resource.Success -> Resource.Success(transform(data))
    is Resource.Error -> this
}
