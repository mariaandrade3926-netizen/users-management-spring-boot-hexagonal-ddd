package com.jcaa.usersmanagement.mobile.data

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface UserApi {
    @POST("api/auth/login")
    suspend fun login(@Body body: LoginRequest): LoginResponse

    @GET("api/users")
    suspend fun list(): List<UserResponse>

    @POST("api/users")
    suspend fun create(@Body body: CreateUserRequest): UserResponse

    @PUT("api/users/{id}")
    suspend fun update(@Path("id") id: String, @Body body: UpdateUserRequest): UserResponse

    @DELETE("api/users/{id}")
    suspend fun delete(@Path("id") id: String): Response<Unit>
}
