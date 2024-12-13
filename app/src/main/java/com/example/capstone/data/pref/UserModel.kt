package com.example.capstone.data.pref

data class UserModel(
    val userName: String,
    val name: String,
    val token: String,
    val isLogin: Boolean = false
)