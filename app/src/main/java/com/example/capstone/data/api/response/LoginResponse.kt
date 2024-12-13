package com.example.capstone.data.api.response

import com.google.gson.annotations.SerializedName

data class LoginResponse(

	@field:SerializedName("data")
	val data: dataLogin,

	@field:SerializedName("message")
	val message: String,

	@field:SerializedName("status")
	val status: String
)

data class dataLogin(

	@field:SerializedName("name")
	val name: String,

	@field:SerializedName("token")
	val token: String,

	@field:SerializedName("username")
	val username: String,

	@field:SerializedName("status")
	val status: String,

	@field:SerializedName("points")
	val points: Int
)

data class ErrorResponse(
	@field:SerializedName("message")
	val message: String? = null
)
