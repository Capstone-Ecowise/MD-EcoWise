package com.example.capstone.view.fragment.home

import android.util.Log
import androidx.activity.result.launch
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.capstone.data.api.ApiService
import com.example.capstone.data.api.response.DataItem
import com.example.capstone.data.pref.UserData
import com.example.capstone.data.pref.UserPreference
import com.example.capstone.data.repository.Repository
import kotlinx.coroutines.launch

class HomeViewModel(private val repository: Repository) : ViewModel() {

    private val _points = MutableLiveData<Int>()
    val points: LiveData<Int> get() = _points

    fun fetchPoints(token: String) {
        viewModelScope.launch {
            try {
                val points = repository.getPoint()
                _points.value = points
            } catch (e: Exception) {
                Log.e("HomeViewModel", "Error fetching points", e)
            }
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
