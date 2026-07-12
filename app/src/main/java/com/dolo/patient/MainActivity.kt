package com.dolo.patient

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.dolo.patient.auth.FakeAuthRepository
import com.dolo.patient.ui.DoloPatientApp
import com.dolo.patient.ui.theme.DoloTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val authRepository = FakeAuthRepository(getSharedPreferences("dolo_session", MODE_PRIVATE))
        setContent { DoloTheme { DoloPatientApp(authRepository) } }
    }
}
