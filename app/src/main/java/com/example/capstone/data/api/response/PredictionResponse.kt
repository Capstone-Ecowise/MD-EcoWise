package com.example.capstone.data.api.response


data class PredictionResponse(
    val status: String,
    val message: String,
    val data: PredictionData
)

data class PredictionData(
    val prediction: String,
    val accuracy: Double,
    val description: String,
    val tips: String,
    val user: Any?
)