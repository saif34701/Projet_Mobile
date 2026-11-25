package com.covoitall.mobile.data.remote

import com.covoitall.mobile.BuildConfig
import com.covoitall.mobile.data.local.SessionDao
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Construit le client Retrofit.
 * Le jeton JWT est lu dans la table Room de session et ajouté à chaque requête ;
 * une réponse 401 sur une requête authentifiée vide la session (jeton expiré), ce qui
 * renvoie automatiquement l'utilisateur vers l'écran d'accueil via LiveData.
 */
object ApiClient {
    fun create(sessionDao: SessionDao): ApiService {
        val authInterceptor = Interceptor { chain ->
            val token = sessionDao.tokenBlocking()
            val request = chain.request().newBuilder().apply {
                if (token != null) addHeader("Authorization", "Bearer $token")
            }.build()
            val response = chain.proceed(request)
            if (response.code() == 401 && token != null) sessionDao.clearBlocking()
            response
        }
        val client = OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .build()
        return Retrofit.Builder()
            .baseUrl(BuildConfig.API_BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}
