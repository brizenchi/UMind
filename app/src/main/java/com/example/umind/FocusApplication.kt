package com.example.umind

import android.app.Application
import com.example.umind.data.repository.FocusRepositoryImpl
import com.example.umind.util.FocusModeCountdownManager
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class FocusApplication : Application() {

    @Inject
    lateinit var repository: FocusRepositoryImpl

    @Inject
    lateinit var focusModeCountdownManager: FocusModeCountdownManager

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override fun onCreate() {
        super.onCreate()

        // Preload installed apps in background
        applicationScope.launch {
            repository.preloadInstalledApps()
        }

        // FocusModeCountdownManager is automatically initialized via @Inject
        // It starts monitoring in its init block
    }
}
