package com.example.capstone.data.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import com.example.capstone.data.api.ApiService
import com.example.capstone.data.api.response.DataItem
import com.example.capstone.data.api.response.LoginResponse
import com.example.capstone.data.api.response.RankResponse
import com.example.capstone.data.api.response.RegistrationResponse
import com.example.capstone.data.api.response.UserProfileResponse
import com.example.capstone.data.pref.SettingPreferences
import com.example.capstone.data.pref.UserData
import com.example.capstone.data.pref.UserModel
import com.example.capstone.data.pref.UserPreference
import kotlinx.coroutines.flow.Flow
import java.security.PrivateKey

class Repository private constructor(
    private val userPreference: UserPreference,
    private val apiService: ApiService,
    private val settingPreferences: SettingPreferences
) {

    private suspend fun saveSession(user: UserModel) {
        userPreference.saveSession(user)
    }

    fun getSession(): Flow<UserModel> {
        return userPreference.getSession()
    }

    suspend fun logout() {
        userPreference.logout()
    }

    suspend fun registerUser(username: String, name: String, email: String, password: String): RegistrationResponse {
        return apiService.register(username, name, email, password)
    }

    suspend fun login(userName: String, password: String): LoginResponse {
        val requestBody = mapOf(
            "identifier" to userName, // Gunakan 'identifier' sesuai API
            "password" to password
        )

        val response = apiService.login(requestBody)

        if (response.status == "success") {
            val user = UserModel(
                userName = response.data?.username ?: "",
                name = response.data?.name ?: "",
                token = response.data?.token ?: "token_auth_ecowise",
                isLogin = true
            )
            saveSession(user)
        } else {
            throw Exception("Login failed: ${response.message}")
        }
        return response
    }

    suspend fun getLeaderboard(): List<DataItem> {
        val response = apiService.getLeaderboard()
        if (response.status == "success") {
            return response.data
        } else {
            throw Exception("Failed to fetch leaderboard")
        }
    }

    suspend fun getPoint(): Int {
        val response = apiService.getPoint()
        if (response.status == "success") {
            return response.data.points
        } else {
            throw Exception("Failed to fetch Point")
        }
    }
    suspend fun getUserProfile(): UserData {
        val response = apiService.getPoint()
        if (response.status == "success") {
            return UserData(
                username = response.data.username,
                email = response.data.email,
                profil = response.data.profil,
                points = response.data.points,
            )
        } else {
            throw Exception("Failed to fetch user profile")
        }
    }

    fun getThemeSettings(): LiveData<Boolean> {
        return settingPreferences.getThemeSetting().asLiveData()
    }

    suspend fun saveThemeSetting(isDarkModeActive: Boolean) {
        settingPreferences.saveThemeSetting(isDarkModeActive)
    }

    companion object {
        fun getInstance(preference: UserPreference,  apiService: ApiService, setPref: SettingPreferences) =
            Repository(preference, apiService, setPref)
    }
}