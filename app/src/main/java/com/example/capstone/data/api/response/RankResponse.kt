package com.example.capstone.data.api.response

import com.google.gson.annotations.SerializedName

data class RankResponse(

	@field:SerializedName("data")
	val data: List<DataItem>,

	@field:SerializedName("status")
	val status: String
)

data class DataItem(

	@field:SerializedName("profil")
	val profil: String?,

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
	val status: String
)
