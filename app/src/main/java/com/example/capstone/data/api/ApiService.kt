package com.example.capstone.data.api

import com.example.capstone.data.api.response.LoginResponse
import com.example.capstone.data.api.response.PredictionResponse
import com.example.capstone.data.api.response.ProfileResponse
import com.example.capstone.data.api.response.RankResponse
import com.example.capstone.data.api.response.RegistrationResponse
import com.example.capstone.data.api.response.UserProfileResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part

interface ApiService {
    @FormUrlEncoded
    @POST("auth/register")
    suspend fun register(
        @Field("username") username : String,
        @Field("name") name: String,
        @Field("email") email: String,
        @Field("password") password: String
    ): RegistrationResponse

    @POST("auth/login")
    suspend fun login(
        @Body requestBody: Map<String, String>
    ): LoginResponse

    @GET("user/leaderboard")
    suspend fun getLeaderboard(
    ): RankResponse

    @GET("user/profile")
    suspend fun getPoint(): UserProfileResponse

    @Multipart
    @POST("predict/model")
    fun predictImage(
        @Part image: MultipartBody.Part
    ): Call<PredictionResponse>

    @Multipart
    @PUT("user/profile")
    suspend fun updateUserProfile(
        @Part("username") username: RequestBody? = null,
        @Part("password") password: RequestBody? = null,
        @Part profil: MultipartBody.Part? = null
    ): Response<ProfileResponse>
}