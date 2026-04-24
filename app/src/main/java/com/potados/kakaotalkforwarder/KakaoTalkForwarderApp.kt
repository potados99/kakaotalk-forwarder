package com.potados.kakaotalkforwarder

import android.app.Application
import com.potados.kakaotalkforwarder.data.ForwardingRepository
import com.potados.kakaotalkforwarder.data.db.AppDatabase
import com.potados.kakaotalkforwarder.data.prefs.SettingsRepository
import com.potados.kakaotalkforwarder.data.remote.MenuPostsApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

class KakaoTalkForwarderApp : Application() {

    val database: AppDatabase by lazy { AppDatabase.build(this) }

    val settingsRepository: SettingsRepository by lazy { SettingsRepository(this) }

    val httpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)
            .build()
    }

    val menuPostsApi: MenuPostsApi by lazy { MenuPostsApi(httpClient) }

    val forwardingRepository: ForwardingRepository by lazy {
        ForwardingRepository(
            dao = database.forwardLogDao(),
            api = menuPostsApi,
            settings = settingsRepository,
        )
    }

    val appScope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        appScope.launch { forwardingRepository.failOrphanedPending() }
    }
}

val android.content.Context.app: KakaoTalkForwarderApp
    get() = applicationContext as KakaoTalkForwarderApp
