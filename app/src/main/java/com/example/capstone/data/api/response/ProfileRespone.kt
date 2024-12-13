package com.example.capstone.data.api.response

data class ProfileResponse(
    val status: String,
    val message: String,
    val data: ProfileData
)

data class ProfileData(
    val id: String,
    val username: String,
    val password: String,
    val email: String,
    val name: String,
    val profile: String?,
    val points: Int,
    val status: String,
    val createdAt: String,
    val updatedAt: String
)