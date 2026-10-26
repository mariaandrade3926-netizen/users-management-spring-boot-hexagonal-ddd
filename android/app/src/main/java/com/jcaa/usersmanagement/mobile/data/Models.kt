package com.jcaa.usersmanagement.mobile.data

data class LoginRequest(val email: String, val password: String)

data class LoginResponse(
    val accessToken: String,
    val tokenType: String,
    val expiresIn: Long
)

data class UserResponse(
    val id: String,
    val name: String,
    val email: String,
    val role: String,
    val status: String
)

data class CreateUserRequest(
    val id: String,
    val name: String,
    val email: String,
    val password: String,
    val role: String
)

data class UpdateUserRequest(
    val name: String,
    val email: String,
    val password: String?,
    val role: String,
    val status: String
)

data class ApiError(val status: Int, val message: String)
