package com.covoitall.mobile.ui.lines

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.covoitall.mobile.data.local.ScheduleEntity
import com.covoitall.mobile.data.repository.LineRepository
import com.covoitall.mobile.data.repository.Resource
import com.covoitall.mobile.ui.common.Event
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.text.Normalizer
import java.time.Duration
import java.time.LocalDateTime
import java.time.LocalTime

data class LineItem(
    val id: String,
    val code: String,
    val name: String,
    val route: String,
    val stopCount: Int,
    val scheduleCount: Int,
    val nextDeparture: String
)

/** Calcul du prochain départ théorique à partir des fiches horaires (jours en français). */
object DepartureCalculator {
    private val DAYS = listOf("lundi", "mardi", "mercredi", "jeudi", "vendredi", "samedi", "dimanche")

    private fun normalize(value: String) =
        Normalizer.normalize(value.trim().lowercase(), Normalizer.Form.NFD).replace(Regex("\\p{M}"), "")

    fun next(schedules: List<ScheduleEntity>, now: LocalDateTime = LocalDateTime.now()): String {
        for (offset in 0..7) {
            val day = now.plusDays(offset.toLong())
            val dayName = DAYS[day.dayOfWeek.value - 1]
            val time = schedules
                .filter { schedule -> schedule.days.any { normalize(it) == dayName } }
                .mapNotNull { runCatching { LocalTime.parse(it.departureTime) }.getOrNull() }
                .filter { offset > 0 || it.isAfter(now.toLocalTime()) }
                .minOrNull() ?: continue
            val label = "%02d:%02d".format(time.hour, time.minute)
            return when (offset) {
                0 -> {
                    val minutes = Duration.between(now.toLocalTime(), time).toMinutes()
                    if (minutes < 60) "Aujourd'hui à $label (dans $minutes min)" else "Aujourd'hui à $label"
                }
                1 -> "Demain à $label"
                else -> "${dayName.replaceFirstChar { it.uppercase() }} à $label"
            }
        }
        return "Aucun départ programmé"
    }

    fun matches(text: String, query: String) = normalize(text).contains(normalize(query))
}

class LinesViewModel(private val repository: LineRepository) : ViewModel() {
    private val query = MutableStateFlow("")

    /** Lignes filtrées dynamiquement par code, nom ou arrêt desservi, enrichies du prochain départ. */
    val lines: LiveData<List<LineItem>> = combine(repository.lines, repository.stops, repository.schedules, query) { lines, stops, schedules, text ->
        val stopNames = stops.associate { it.id to it.name }
        lines.map { line ->
            val names = line.stopIds.mapNotNull { stopNames[it] }
            val lineSchedules = schedules.filter { it.lineId == line.id }
            LineItem(
                id = line.id,
                code = line.code,
                name = line.name,
                route = names.joinToString(" → ").ifEmpty { "Arrêts non renseignés" },
                stopCount = names.size,
                scheduleCount = lineSchedules.size,
                nextDeparture = DepartureCalculator.next(lineSchedules)
            )
        }.filter { item ->
            text.isBlank() || DepartureCalculator.matches("${item.code} ${item.name} ${item.route}", text)
        }
    }.asLiveData()

    private val _loading = MutableLiveData(false)
    val loading: LiveData<Boolean> = _loading

    private val _message = MutableLiveData<Event<String>>()
    val message: LiveData<Event<String>> = _message

    init {
        refresh()
    }

    fun setQuery(text: String) {
        query.value = text
    }

    fun refresh() {
        if (_loading.value == true) return
        _loading.value = true
        viewModelScope.launch {
            val result = repository.refresh()
            _loading.value = false
            if (result is Resource.Error) _message.value = Event("${result.message}\nAffichage des dernières données enregistrées.")
        }
    }
}
