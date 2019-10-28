package com.android.example.cameraxbasic.remote

import com.android.example.cameraxbasic.model.VisionRequest
import com.android.example.cameraxbasic.model.VisionResponse
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query
import retrofit2.Call
import retrofit2.http.Headers

interface GoogleVisionService {

    @Headers("Content-type: application/json")
    @POST("v1/images:annotate")
    fun getAnnotations(@Query("key") apiKey: String, @Body request: VisionRequest): Call<VisionResponse>


    object ApiUtils {
        val BASE_URL = "https://vision.googleapis.com"

        val gvService:GoogleVisionService
        get() = RetrofitClient.getClient(BASE_URL)!!.create(GoogleVisionService::class.java)
    }
}