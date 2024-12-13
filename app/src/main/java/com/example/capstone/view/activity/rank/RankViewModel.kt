package com.example.capstone.view.activity.rank

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.capstone.data.api.response.DataItem
import com.example.capstone.data.repository.Repository
import kotlinx.coroutines.launch

class RankViewModel(private val repository: Repository) : ViewModel() {
        private val _leaderboard = MutableLiveData<List<DataItem>>()
        val leaderboard: LiveData<List<DataItem>> get() = _leaderboard

        private val _isLoading = MutableLiveData<Boolean>()
        val isLoading: LiveData<Boolean> get() = _isLoading

        private val _errorMessage = MutableLiveData<String?>()
        val errorMessage: LiveData<String?> get() = _errorMessage

        fun fetchLeaderboard() {
            _isLoading.value = true
            viewModelScope.launch {
                try {
                    val result = repository.getLeaderboard()
                    _leaderboard.value = result
                    _errorMessage.value = null
                } catch (e: Exception) {
                    _errorMessage.value = e.message
                } finally {
                    _isLoading.value = false
                }
            }
        }
    }
