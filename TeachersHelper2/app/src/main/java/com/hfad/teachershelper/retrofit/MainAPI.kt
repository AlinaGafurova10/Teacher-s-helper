package com.hfad.teachershelper.retrofit

import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface MainAPI {
//

    @FormUrlEncoded
    @POST("signup")
    fun signup(
        @Field("phone") phone: String,
        @Field("email") email: String,
        @Field("full_name") fullName: String,
        @Field("password") password: String
    ): Call<AuthResponse>

    @FormUrlEncoded
    @POST("login/step1")
    fun login(
        @Field("phone") phone: String
    ): Call<StepOneResponse>

    @FormUrlEncoded
    @POST("login/step2")
    fun parol(
        @Field("password") password: String
    ):Call<AuthResponse>

    @POST("/chat")
    suspend fun sendMessage(
        @Query("auth_token") token: String,
        @Body messageRequest: MessageRequest
    ): MessageResponse

    companion object {
        const val BASE_URL = "http://10.0.2.2:8000/"
//        const val BASE_URL = "http://127.0.0.1:8000/"
    }

    @GET("subjects/json")
    suspend fun getSubjectsJson(
        @Query("auth_token") token: String
    ): Response<CreateSubjectsWrapper>


//    @FormUrlEncoded
//    @POST("token/init")
//    fun sendPhone(@Field("phone") phone: String): Call<AuthResponse>
//
//    @FormUrlEncoded
//    @POST("token/complete")
//    fun sendPasswordAndToken(
//        @Field("password") password: String,
//        @Field("temp_token") tempToken: String
//    ): Call<AuthResponse>

    @GET("users/me")
    fun getUser(@Header("Authorization") token: String): Call<User>

//    @GET("subjects/")
//    suspend fun getSubjects(@Query("auth_token") token: String): Response<List<SubjectRequest>>

//    @GET("subjects/")
//    fun getSubjects(@Query("auth_token") token: String): Call<List<Subject>>

//    @GET("subjects/")
//    fun getSubjects(@Header("Authorization") token: String): Call<List<Subject>>

//    @FormUrlEncoded
//    @POST("token/init")
//    suspend fun getToken(
//        @Field("phone") phone: String
//    ): Response<TokenReponse>
//
//    @FormUrlEncoded
//    @POST("token/complete")
//    suspend fun get(
//        @Field("password") password: String,
//        @Field("temp_token") tempToken: String
//    ): Response<TokenReponse>
}
//    @GET("subjects/{id}")
//    suspend fun getSubjectById(@Path("id") id: Int): Subject
//
//    @GET("subjects/")
//    suspend fun getAllItems(): List<Subject>
//
//    @POST("/signup")
//    suspend fun sendLoginPaorol(@Body loginParol: Login_parol): ResponseData
//
//    @POST("/token/init")
//    suspend fun getToken(@Body authRequestFullName: AuthRequestFullName): Response<TokenReponse>
//
//    @POST("/token/complete")
//    suspend fun get(@Body authRequestHashedPassword: AuthRequestHashedPassword): Response<TokenReponse>

//    @GET("subjects/")
////короче тут должна быть ссылка на предмет
//    //типо как понять какой предмет каким по счету
//    suspend fun getSubjectById(@Path("id") id: Int): List<Subject>
//    //@Path("id") id: Int
//    @GET("subjects/")
//    suspend fun getAllItems(): List<Subject>
//
////    @POST("/token/init") // сменить url
////    suspend fun auth(@Body authRequestFullName: AuthRequestFullName): User
////    @POST("/token/complete")// сменить url
////    suspend fun autH(@Body authRequestHashedPassword: AuthRequestHashedPassword): User
//
//
//    @POST("/signup")
//    suspend fun sendLoginPaorol(@Body loginParol: Login_parol): ResponseData
//
//    @POST("/token/init")
//    suspend fun getToken(authRequestFullName: AuthRequestFullName
//    ): Response<TokenReponse>
////    @Field("username") username: String,
//    @POST("/token/complete")
//    suspend fun get(@Body authRequestHashedPassword: AuthRequestHashedPassword
//    ): Response<TokenReponse>

