package com.dolo.patient.data

import com.dolo.patient.data.model.Doctor
import com.dolo.patient.data.model.DoctorCategory

object DummyData {
    val categories = listOf(
        DoctorCategory("general", "General Physician", "✚"),
        DoctorCategory("cardio", "Cardiologist", "♥"),
        DoctorCategory("dental", "Dentist", "◆"),
        DoctorCategory("child", "Pediatrician", "●"),
        DoctorCategory("skin", "Dermatologist", "✦"),
        DoctorCategory("eye", "Ophthalmologist", "◉"),
    )

    val doctors = listOf(
        Doctor("1", "Dr. Aisha Mehta", "General Physician", "Care Point Clinic", 11, 4.8, 500),
        Doctor("2", "Dr. Rohan Kapoor", "General Physician", "City Health Centre", 8, 4.7, 450),
        Doctor("3", "Dr. Neha Sharma", "Cardiologist", "Heartline Clinic", 14, 4.9, 800),
    )
}

