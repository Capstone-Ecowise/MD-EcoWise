package com.example.capstone.di

import android.content.Context
import com.example.capstone.data.api.ApiConfig
import com.example.capstone.data.pref.SettingPreferences
import com.example.capstone.data.pref.UserPreference
import com.example.capstone.data.pref.dataStore
import com.example.capstone.data.pref.settingdataStore
import com.example.capstone.data.repository.Repository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

object Injection {
    fun provideRepository(context: Context): Repository {
        val pref = UserPreference.getInstance(context.dataStore)
        val user = runBlocking { pref.getSession().first() }
        val apiService = ApiConfig.getApiService(user.token)
        val setPref = SettingPreferences.getInstance(context.settingdataStore)
        return Repository.getInstance(pref, apiService, setPref)
    }
}