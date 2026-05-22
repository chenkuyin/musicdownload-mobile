package com.example.musicdownloader.data.remote

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {
    
    // 默认服务器地址，可在设置中修改
    var BASE_URL = "http://192.168.100.196:8000"  // 真机访问本机
    
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }
    
    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(120, TimeUnit.SECONDS)  // 搜索可能需要较长时间
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()
    
    private var retrofit: Retrofit? = null
    
    fun getRetrofit(): Retrofit {
        if (retrofit == null || retrofit?.baseUrl()?.toString() != BASE_URL) {
            retrofit = Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
        }
        return retrofit!!
    }
    
    fun getApiService(): MusicApiService {
        return getRetrofit().create(MusicApiService::class.java)
    }
    
    fun updateBaseUrl(newUrl: String) {
        BASE_URL = newUrl
        retrofit = null
    }
}
