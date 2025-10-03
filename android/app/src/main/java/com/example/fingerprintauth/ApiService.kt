package com.example.fingerprintauth

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST

// Data classes for API requests and responses
data class RegisterRequest(
    val username: String,
    val fingerprintData: String
)

data class VerifyRequest(
    val username: String,
    val fingerprintData: String
)

data class RegisterFaceRequest(
    val username: String,
    val faceData: String
)

data class VerifyFaceRequest(
    val username: String,
    val faceData: String
)

data class ApiResponse(
    val success: Boolean,
    val message: String?,
    val userId: String? = null,
    val username: String? = null
)

// API Service interface
interface ApiService {
    @POST("api/register")
    suspend fun registerUser(@Body request: RegisterRequest): ApiResponse
    
    @POST("api/verify")
    suspend fun verifyUser(@Body request: VerifyRequest): ApiResponse
    
    @POST("api/register-face")
    suspend fun registerFace(@Body request: RegisterFaceRequest): ApiResponse
    
    @POST("api/verify-face")
    suspend fun verifyFace(@Body request: VerifyFaceRequest): ApiResponse
}

// API Client object
object ApiClient {
    private const val BASE_URL = "http://192.168.246.31:3000/"
    
    fun create(): ApiService {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        
        return retrofit.create(ApiService::class.java)
    }
}