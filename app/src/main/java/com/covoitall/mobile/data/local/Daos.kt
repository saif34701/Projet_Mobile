package com.covoitall.mobile.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface SessionDao {
    @Query("SELECT * FROM session_user LIMIT 1")
    fun observe(): Flow<UserEntity?>

    @Query("SELECT * FROM session_user LIMIT 1")
    suspend fun current(): UserEntity?

    /** Lecture synchrone utilisée par l'intercepteur OkHttp (exécuté hors du thread principal). */
    @Query("SELECT token FROM session_user LIMIT 1")
    fun tokenBlocking(): String?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(user: UserEntity)

    @Query("DELETE FROM session_user")
    suspend fun clear()

    @Query("DELETE FROM session_user")
    fun clearBlocking()

    @Transaction
    suspend fun replace(user: UserEntity) {
        clear()
        insert(user)
    }
}

@Dao
interface NetworkDao {
    @Query("SELECT * FROM lines ORDER BY code")
    fun observeLines(): Flow<List<LineEntity>>

    @Query("SELECT * FROM lines WHERE id = :lineId")
    fun observeLine(lineId: String): Flow<LineEntity?>

    @Query("SELECT * FROM stops")
    fun observeStops(): Flow<List<StopEntity>>

    @Query("SELECT * FROM schedules ORDER BY departureTime")
    fun observeSchedules(): Flow<List<ScheduleEntity>>

    @Query("SELECT * FROM schedules WHERE lineId = :lineId ORDER BY departureTime")
    fun observeSchedulesForLine(lineId: String): Flow<List<ScheduleEntity>>

    @Query("DELETE FROM lines")
    suspend fun clearLines()

    @Query("DELETE FROM stops")
    suspend fun clearStops()

    @Query("DELETE FROM schedules")
    suspend fun clearSchedules()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLines(lines: List<LineEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStops(stops: List<StopEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSchedules(schedules: List<ScheduleEntity>)

    /** Synchronisation atomique : le cache reflète exactement l'état du serveur. */
    @Transaction
    suspend fun replaceNetwork(lines: List<LineEntity>, stops: List<StopEntity>, schedules: List<ScheduleEntity>) {
        clearLines(); clearStops(); clearSchedules()
        insertLines(lines); insertStops(stops); insertSchedules(schedules)
    }
}

@Dao
interface RideDao {
    @Query("SELECT * FROM my_rides ORDER BY departureAt DESC")
    fun observeAll(): Flow<List<RideEntity>>

    @Query("SELECT * FROM my_rides WHERE id = :rideId")
    suspend fun byId(rideId: String): RideEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(ride: RideEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(rides: List<RideEntity>)

    @Query("DELETE FROM my_rides WHERE id = :rideId")
    suspend fun delete(rideId: String)

    @Query("DELETE FROM my_rides")
    suspend fun clear()

    @Transaction
    suspend fun replaceAll(rides: List<RideEntity>) {
        clear()
        insertAll(rides)
    }
}

@Dao
interface ReservationDao {
    @Query("SELECT * FROM my_reservations ORDER BY departureAt DESC")
    fun observeAll(): Flow<List<ReservationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(reservation: ReservationEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(reservations: List<ReservationEntity>)

    @Query("DELETE FROM my_reservations WHERE id = :reservationId")
    suspend fun delete(reservationId: String)

    @Query("DELETE FROM my_reservations")
    suspend fun clear()

    @Transaction
    suspend fun replaceAll(reservations: List<ReservationEntity>) {
        clear()
        insertAll(reservations)
    }
}
