package com.dolo.patient.data

import com.dolo.patient.R
import com.dolo.patient.data.model.Doctor
import com.dolo.patient.data.model.DoctorCategory

object DummyData {
    val categories = listOf(
        DoctorCategory("general", "General Physician", "+", R.drawable.category_general),
        DoctorCategory("cardio", "Cardiologist", "heart", R.drawable.category_cardio),
        DoctorCategory("dental", "Dentist", "tooth", R.drawable.category_dental),
        DoctorCategory("child", "Pediatrician", "child", R.drawable.category_child),
        DoctorCategory("skin", "Dermatologist", "skin", R.drawable.category_skin),
        DoctorCategory("eye", "Ophthalmologist", "eye", R.drawable.category_eye),
        DoctorCategory("ortho", "Orthopedic", "bone", R.drawable.category_ortho),
        DoctorCategory("ent", "ENT Specialist", "ear", R.drawable.category_ent),
        DoctorCategory("gyn", "Gynecologist", "women", R.drawable.category_gyn),
        DoctorCategory("neuro", "Neurologist", "brain", R.drawable.category_neuro),
        DoctorCategory("mental", "Psychiatrist", "mind", R.drawable.category_mental),
        DoctorCategory("physio", "Physiotherapist", "movement", R.drawable.category_physio),
    )

    val doctors = listOf(
        Doctor("1", "Dr. Aisha Mehta", "General Physician", "Care Point Clinic", 11, 4.8, 500),
        Doctor("2", "Dr. Rohan Kapoor", "General Physician", "City Health Centre", 8, 4.7, 450),
        Doctor("3", "Dr. Neha Sharma", "Cardiologist", "Heartline Clinic", 14, 4.9, 800),
        Doctor("4", "Dr. Arjun Malhotra", "Cardiologist", "Pulse Care Hospital", 10, 4.7, 750, eveningAvailable = false),
        Doctor("5", "Dr. Kavya Iyer", "Dentist", "Smile Studio", 9, 4.8, 600),
        Doctor("6", "Dr. Sameer Khan", "Dentist", "Pearl Dental Care", 13, 4.6, 550, morningAvailable = false),
        Doctor("7", "Dr. Meera Joshi", "Pediatrician", "Little Steps Clinic", 12, 4.9, 650),
        Doctor("8", "Dr. Nikhil Batra", "Pediatrician", "Happy Child Centre", 7, 4.7, 550),
        Doctor("9", "Dr. Sana Qureshi", "Dermatologist", "Clear Skin Clinic", 10, 4.8, 700),
        Doctor("10", "Dr. Vivek Rao", "Dermatologist", "Derma Plus", 15, 4.6, 750, eveningAvailable = false),
        Doctor("11", "Dr. Priya Menon", "Ophthalmologist", "Vision First", 14, 4.9, 650),
        Doctor("12", "Dr. Aditya Bose", "Ophthalmologist", "Bright Eye Centre", 8, 4.7, 600),
        Doctor("13", "Dr. Manav Sethi", "Orthopedic", "Joint & Bone Clinic", 16, 4.8, 800),
        Doctor("14", "Dr. Ritu Nair", "Orthopedic", "Mobility Care", 9, 4.6, 700),
        Doctor("15", "Dr. Farhan Ali", "ENT Specialist", "Clear Voice ENT", 12, 4.8, 600),
        Doctor("16", "Dr. Shreya Das", "ENT Specialist", "HearWell Clinic", 8, 4.7, 550, morningAvailable = false),
        Doctor("17", "Dr. Ananya Gupta", "Gynecologist", "Her Wellness Clinic", 15, 4.9, 800),
        Doctor("18", "Dr. Pooja Reddy", "Gynecologist", "Bloom Women's Care", 10, 4.8, 750),
        Doctor("19", "Dr. Kabir Chawla", "Neurologist", "NeuroCare Centre", 17, 4.9, 900),
        Doctor("20", "Dr. Isha Kulkarni", "Neurologist", "Mind & Nerve Clinic", 11, 4.7, 850),
        Doctor("21", "Dr. Devika Sen", "Psychiatrist", "Calm Mind Clinic", 13, 4.8, 700),
        Doctor("22", "Dr. Aman Verma", "Psychiatrist", "Wellbeing Centre", 9, 4.7, 650, eveningAvailable = false),
        Doctor("23", "Dr. Tara Singh", "Physiotherapist", "Active Life Physio", 10, 4.8, 500),
        Doctor("24", "Dr. Harsh Patel", "Physiotherapist", "Move Better Studio", 7, 4.6, 450, morningAvailable = false),
    )
}