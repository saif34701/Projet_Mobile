package com.covoitall.mobile.data.repository

import com.covoitall.mobile.data.local.LineEntity
import com.covoitall.mobile.data.local.NetworkDao
import com.covoitall.mobile.data.local.ScheduleEntity
import com.covoitall.mobile.data.local.StopEntity
import com.covoitall.mobile.data.remote.ApiService
import com.covoitall.mobile.data.remote.LiveLineDto
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow

/**
 * Lignes, arrêts et horaires des navettes.
 * Les écrans observent le cache Room ; refresh() synchronise ce cache avec l'API,
 * ce qui permet une consultation hors ligne des dernières données connues.
 */
class LineRepository(private val api: ApiService, private val dao: NetworkDao) {
    val lines: Flow<List<LineEntity>> = dao.observeLines()
    val stops: Flow<List<StopEntity>> = dao.observeStops()
    val schedules: Flow<List<ScheduleEntity>> = dao.observeSchedules()

    fun line(lineId: String): Flow<LineEntity?> = dao.observeLine(lineId)
    fun schedulesForLine(lineId: String): Flow<List<ScheduleEntity>> = dao.observeSchedulesForLine(lineId)

    suspend fun refresh(): Resource<Unit> = coroutineScope {
        val lines = async { safeApiCall { api.lines() } }
        val stops = async { safeApiCall { api.stops() } }
        val schedules = async { safeApiCall { api.schedules() } }
        val results = listOf(lines.await(), stops.await(), schedules.await())
        val error = results.filterIsInstance<Resource.Error>().firstOrNull()
        if (error != null) return@coroutineScope error
        dao.replaceNetwork(
            (lines.await() as Resource.Success).data.map { it.toEntity() },
            (stops.await() as Resource.Success).data.map { it.toEntity() },
            (schedules.await() as Resource.Success).data.map { it.toEntity() }
        )
        Resource.Success(Unit)
    }

    /** État temps réel d'une ligne : position GPS de la navette et temps d'attente estimés. */
    suspend fun liveStatus(lineId: String): Resource<LiveLineDto> = safeApiCall { api.liveLine(lineId) }
}
