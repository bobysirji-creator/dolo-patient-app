package com.dolo.patient

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.dolo.patient.auth.AndroidKeystoreTokenStore
import com.dolo.patient.auth.HttpPrototypeAuthApi
import com.dolo.patient.auth.PrototypeAuthRepository
import com.dolo.patient.auth.PrototypeSessionManager
import com.dolo.patient.data.LocalPatientRepository
import com.dolo.patient.data.HttpHostedPatientSyncApi
import com.dolo.patient.platform.HttpPlatformApi
import com.dolo.patient.push.DoloPushNotifications
import com.dolo.patient.push.PushNotificationPolicy
import com.dolo.patient.ui.DoloPatientApp
import com.dolo.patient.ui.theme.DoloTheme

class MainActivity : ComponentActivity() {
    private val notificationDestination = mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        notificationDestination.value = intent.notificationDestination()
        DoloPushNotifications.createChannel(this)
        val storage=getSharedPreferences("dolo_session",MODE_PRIVATE)
        val tokenStore=AndroidKeystoreTokenStore(storage)
        val authApi=HttpPrototypeAuthApi(BuildConfig.DOLO_API_BASE_URL)
        val sessionManager=PrototypeSessionManager(tokenStore,authApi)
        val authRepository=PrototypeAuthRepository(storage,tokenStore,authApi,sessionManager)
        val hostedSyncApi=HttpHostedPatientSyncApi(BuildConfig.DOLO_API_BASE_URL,sessionManager,storage)
        val patientRepository=LocalPatientRepository(storage)
        val platformApi=HttpPlatformApi(BuildConfig.DOLO_API_BASE_URL)
        setContent {
            var darkModeEnabled by remember { mutableStateOf(storage.getBoolean("patient_dark_mode", false)) }
            DoloTheme(darkTheme = darkModeEnabled) {
                DoloPatientApp(
                    authRepository = authRepository,
                    patientRepository = patientRepository,
                    platformApi = platformApi,
                    hostedSyncApi = hostedSyncApi,
                    darkModeEnabled = darkModeEnabled,
                    initialNotificationDestination = notificationDestination.value,
                    onNotificationDestinationHandled = { notificationDestination.value = null },
                    onDarkModeChange = { enabled ->
                        darkModeEnabled = enabled
                        storage.edit().putBoolean("patient_dark_mode", enabled).apply()
                    }
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        notificationDestination.value = intent.notificationDestination()
    }
}

private fun Intent.notificationDestination(): String? =
    getStringExtra(PushNotificationPolicy.EXTRA_DESTINATION)
        ?.takeIf { it.matches(Regex("^queue/[A-Za-z0-9_-]{1,80}$")) }
