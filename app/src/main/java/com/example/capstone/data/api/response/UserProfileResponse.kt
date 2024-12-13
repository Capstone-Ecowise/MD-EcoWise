package com.example.capstone.data.api.response

import com.example.capstone.data.pref.UserData

data class UserProfileResponse(
    val status: String,
    val data: UserData
)
