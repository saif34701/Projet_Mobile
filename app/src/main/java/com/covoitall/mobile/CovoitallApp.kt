package com.covoitall.mobile

import android.app.Application
import android.content.Context
import com.covoitall.mobile.data.local.AppDatabase
import com.covoitall.mobile.data.remote.ApiClient
import com.covoitall.mobile.data.repository.AuthRepository
import com.covoitall.mobile.data.repository.LineRepository
import com.covoitall.mobile.data.repository.ReservationRepository
import com.covoitall.mobile.data.repository.RideRepository
import org.osmdroid.config.Configuration

/** Conteneur d'injection manuelle : une instance unique de chaque dépendance pour toute l'application. */
class AppContainer(context: Context) {
    private val database = AppDatabase.build(context)
    private val api = ApiClient.create(database.sessionDao())

    val authRepository = AuthRepository(api, database.sessionDao(), database.rideDao(), database.reservationDao())
    val lineRepository = LineRepository(api, database.networkDao())
    val rideRepository = RideRepository(api, database.rideDao())
    val reservationRepository = ReservationRepository(api, database.reservationDao())
}

class CovoitallApp : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
        // osmdroid exige un user-agent identifiant l'application auprès des serveurs de tuiles OpenStreetMap.
        Configuration.getInstance().apply {
            userAgentValue = packageName
            osmdroidBasePath = cacheDir
            osmdroidTileCache = cacheDir.resolve("tiles")
        }
    }
}
