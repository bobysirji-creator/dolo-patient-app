package com.dolo.patient

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.dolo.patient.auth.FakeAuthRepository
import com.dolo.patient.data.LocalPatientRepository
import com.dolo.patient.platform.HttpPlatformApi
import com.dolo.patient.ui.DoloPatientApp
import com.dolo.patient.ui.theme.DoloTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val storage=getSharedPreferences("dolo_session",MODE_PRIVATE)
        val authRepository=FakeAuthRepository(storage)
        val patientRepository=LocalPatientRepository(storage)
        val platformApi=HttpPlatformApi(BuildConfig.DOLO_API_BASE_URL)
        setContent { DoloTheme { DoloPatientApp(authRepository,patientRepository,platformApi) } }
    }
}
