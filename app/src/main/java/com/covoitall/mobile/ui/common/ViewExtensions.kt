package com.covoitall.mobile.ui.common

import android.app.Activity
import android.content.res.ColorStateList
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.covoitall.mobile.R
import com.google.android.material.snackbar.Snackbar

fun Activity.showMessage(message: String) {
    Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_LONG).show()
}

/** Affiche un message d'erreur dans un TextView dédié, ou le masque si null. */
fun TextView.showError(message: String?) {
    text = message
    isVisible = !message.isNullOrBlank()
}

fun EditText.onTextChanged(action: (String) -> Unit) {
    addTextChangedListener(object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = Unit
        override fun afterTextChanged(s: Editable?) = action(s?.toString().orEmpty())
    })
}

fun EditText.value(): String = text?.toString().orEmpty()

/** Colore une pastille de statut selon sa signification. */
fun TextView.statusStyle(kind: StatusKind) {
    val (background, foreground) = when (kind) {
        StatusKind.SUCCESS -> R.color.green_light to R.color.green_dark
        StatusKind.WARNING -> R.color.warning_light to R.color.warning
        StatusKind.ERROR -> R.color.error_light to R.color.error
    }
    backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(context, background))
    setTextColor(ContextCompat.getColor(context, foreground))
}

enum class StatusKind { SUCCESS, WARNING, ERROR }

fun statusKind(status: String): StatusKind = when (status) {
    "confirmed", "paid", "scheduled", "completed" -> StatusKind.SUCCESS
    "pending", "arrived" -> StatusKind.WARNING
    else -> StatusKind.ERROR
}
