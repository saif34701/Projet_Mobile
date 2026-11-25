package com.covoitall.mobile.ui.common

import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

/** Mise en forme partagée des dates, prix et statuts. */
object Formatters {
    private val locale = Locale.FRANCE
    private val dateTime = DateTimeFormatter.ofPattern("EEE d MMM 'à' HH:mm", locale)
    val apiDateTime: DateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME

    /** Les départs sont stockés en heure locale (sans fuseau) ; les dates système en UTC avec fuseau. */
    fun parse(value: String): LocalDateTime? = runCatching { LocalDateTime.parse(value) }.getOrNull()
        ?: runCatching { OffsetDateTime.parse(value).atZoneSameInstant(ZoneId.systemDefault()).toLocalDateTime() }.getOrNull()
        ?: runCatching { LocalDateTime.parse(value + "T00:00:00") }.getOrNull()

    fun dateTime(value: String): String = parse(value)?.format(dateTime)?.replaceFirstChar { it.uppercase() } ?: value

    /** Horodatages système (création, validation) : enregistrés en UTC par le serveur, affichés en heure locale. */
    fun utcDateTime(value: String): String {
        val local = runCatching { OffsetDateTime.parse(value).atZoneSameInstant(ZoneId.systemDefault()).toLocalDateTime() }.getOrNull()
            ?: runCatching { LocalDateTime.parse(value).atZone(ZoneId.of("UTC")).withZoneSameInstant(ZoneId.systemDefault()).toLocalDateTime() }.getOrNull()
        return local?.format(dateTime)?.replaceFirstChar { it.uppercase() } ?: value
    }

    /** Nombre sans décimales inutiles (5.0 -> 5, 4.5 -> 4.5) pour pré-remplir un champ. */
    fun plainNumber(value: Double): String = if (value % 1.0 == 0.0) value.toLong().toString() else value.toString()

    fun price(value: Double): String = String.format(locale, "%.2f TND", value)

    fun rideStatus(status: String): String = when (status) {
        "scheduled" -> "Planifié"
        "arrived" -> "Au point de rendez-vous"
        "completed" -> "Terminé"
        "cancelled" -> "Annulé"
        else -> status
    }

    fun reservationStatus(status: String): String = when (status) {
        "pending" -> "En attente du conducteur"
        "confirmed" -> "Confirmée"
        "paid" -> "Payée"
        "rejected" -> "Refusée"
        "cancelled" -> "Annulée"
        else -> status
    }

    fun minutes(value: Int): String = when {
        value <= 1 -> "Imminent"
        value < 60 -> "$value min"
        else -> "${value / 60} h ${String.format(locale, "%02d", value % 60)}"
    }
}
