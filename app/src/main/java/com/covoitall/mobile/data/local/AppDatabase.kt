package com.covoitall.mobile.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [UserEntity::class, LineEntity::class, StopEntity::class, ScheduleEntity::class, RideEntity::class, ReservationEntity::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun sessionDao(): SessionDao
    abstract fun networkDao(): NetworkDao
    abstract fun rideDao(): RideDao
    abstract fun reservationDao(): ReservationDao

    companion object {
        fun build(context: Context): AppDatabase =
            Room.databaseBuilder(context, AppDatabase::class.java, "covoitall.db")
                .fallbackToDestructiveMigration()
                .build()
    }
}
