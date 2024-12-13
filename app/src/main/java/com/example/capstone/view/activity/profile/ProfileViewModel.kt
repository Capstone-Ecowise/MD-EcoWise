package com.example.capstone.view.activity.profile

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.capstone.data.repository.Repository
import kotlinx.coroutines.launch

class ProfileViewModel(private val repository: Repository) : ViewModel() {

    private val _username = MutableLiveData<String>()
    private val _profileImageUrl = MutableLiveData<String>()
    private val _points = MutableLiveData<Int>()

    val username: LiveData<String> get() = _username
    val profileImageUrl: LiveData<String> get() = _profileImageUrl
    val points: LiveData<Int> get() = _points

    fun fetchUserProfile(token: String) {
        viewModelScope.launch {
            try {
                // Fetch user profile data
                val userProfile = repository.getUserProfile()
                _username.value = userProfile.username
                _profileImageUrl.value = userProfile.profil
            } catch (e: Exception) {
                Log.e("ProfileViewModel", "Error fetching user profile", e)
            }
        }
    }
}