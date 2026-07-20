package com.dolo.patient

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.dolo.patient.auth.AndroidKeystoreTokenStore
import com.dolo.patient.auth.HttpPrototypeAuthApi
import com.dolo.patient.auth.PrototypeAuthRepository
import com.dolo.patient.auth.PrototypeSessionManager
import com.dolo.patient.data.LocalPatientRepository
import com.dolo.patient.data.HttpHostedPatientSyncApi
import com.dolo.patient.platform.HttpPlatformApi
import com.dolo.patient.ui.DoloPatientApp
import com.dolo.patient.ui.theme.DoloTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val storage=getSharedPreferences("dolo_session",MODE_PRIVATE)
        val tokenStore=AndroidKeystoreTokenStore(storage)
        val authApi=HttpPrototypeAuthApi(BuildConfig.DOLO_API_BASE_URL)
        val authRepository=PrototypeAuthRepository(storage,tokenStore,authApi)
        val hostedSyncApi=HttpHostedPatientSyncApi(BuildConfig.DOLO_API_BASE_URL,PrototypeSessionManager(tokenStore,authApi),storage)
        val patientRepository=LocalPatientRepository(storage)
        val platformApi=HttpPlatformApi(BuildConfig.DOLO_API_BASE_URL)
        setContent { DoloTheme { DoloPatientApp(authRepository,patientRepository,platformApi,hostedSyncApi) } }
    }
}
