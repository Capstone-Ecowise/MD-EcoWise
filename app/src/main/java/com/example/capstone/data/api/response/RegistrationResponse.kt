package com.example.capstone.data.api.response

import com.google.gson.annotations.SerializedName

data class RegistrationResponse(

	@field:SerializedName("data")
	val data: dataRegistration,

	@field:SerializedName("message")
	val message: String,

	@field:SerializedName("status")
	val status: String
)

data class dataRegistration(

	@field:SerializedName("createdAt")
	val createdAt: String,

	@field:SerializedName("profil")
	val profil: Any,

	@field:SerializedName("name")
	val name: String,

	@field:SerializedName("id")
	val id: String,

	@field:SerializedName("email")
	val email: String,

	@field:SerializedName("username")
	val username: String,

	@field:SerializedName("points")
	val points: Int,

	@field:SerializedName("status")
	val status: String,

	@field:SerializedName("updatedAt")
	val updatedAt: String
)
