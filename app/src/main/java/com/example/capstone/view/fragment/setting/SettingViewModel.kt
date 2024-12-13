package com.example.capstone.view.fragment.setting

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.capstone.data.pref.UserModel
import com.example.capstone.data.repository.Repository
import kotlinx.coroutines.launch

class SettingViewModel(private val repository: Repository) : ViewModel() {

        private val _message = MutableLiveData<String?>()
        val message: LiveData<String?> get() = _message

        private val _isLoading = MutableLiveData<Boolean>()
        val isLoading: LiveData<Boolean> = _isLoading

        private val _name = MutableLiveData<String?>()
        val name: LiveData<String?> get() = _name

        init {
            viewModelScope.launch {
                repository.getSession().collect { user ->
                    _name.value = user.name
                }
            }
        }

        fun getSession(): LiveData<UserModel> {
            return repository.getSession().asLiveData()
        }

        fun logout() {
            viewModelScope.launch {
                repository.logout()
            }
        }

        fun getThemeSettings(): LiveData<Boolean> {
            return repository.getThemeSettings()
        }

        fun saveThemeSetting(isDarkModeActive: Boolean) {
            viewModelScope.launch {
                repository.saveThemeSetting(isDarkModeActive)
            }
        }

    private val _profileImageUrl = MutableLiveData<String?>()
    val profileImageUrl: MutableLiveData<String?> get() = _profileImageUrl

    fun fetchUserProfile(token: String) {
        viewModelScope.launch {
            try {
                // Fetch user profile data
                val userProfile = repository.getUserProfile()
                _profileImageUrl.value = userProfile.profil
            } catch (e: Exception) {
                Log.e("ProfileViewModel", "Error fetching user profile", e)
            }
        }
    }


    }