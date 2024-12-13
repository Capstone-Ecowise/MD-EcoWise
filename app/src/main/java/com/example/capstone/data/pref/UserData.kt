package com.example.capstone.data.pref

data class UserData(
    val username: String,
    val email: String,
    val profil: String?, // Assuming 'profil' can be null
    val points: Int,
)

