package com.example.data.remote

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.security.SecureRandom
import java.security.cert.X509Certificate
import java.util.concurrent.TimeUnit
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

object ApiClient {
    var baseUrl: String = SecureConfig.baseUrl
        set(value) {
            field = if (value.endsWith("/")) value else "$value/"
            _apiService = createRetrofit(field).create(FlikataApiService::class.java)
        }

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.NONE
    }

    private val trustAllCerts = arrayOf<TrustManager>(
        object : X509TrustManager {
            override fun checkClientTrusted(chain: Array<out X509Certificate>?, authType: String?) {}
            override fun checkServerTrusted(chain: Array<out X509Certificate>?, authType: String?) {}
            override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
        }
    )

    @Volatile
    var currentAuthToken: String? = null

    @Volatile
    var currentUserId: String? = null

    private val authHeaderInterceptor = okhttp3.Interceptor { chain ->
        val original = chain.request()
        val builder = original.newBuilder()
        val token = currentAuthToken
        if (!token.isNullOrBlank()) {
            builder.header("Authorization", "Bearer $token")
        }
        val uid = currentUserId
        if (!uid.isNullOrBlank()) {
            builder.header("X-Sender-Id", uid)
        }
        chain.proceed(builder.build())
    }

    val BASE_URL: String get() = baseUrl

    val okHttpClient: OkHttpClient by lazy {
        val builder = OkHttpClient.Builder()
            .addInterceptor(authHeaderInterceptor)
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)

        try {
            val sslContext = SSLContext.getInstance("TLS")
            sslContext.init(null, trustAllCerts, SecureRandom())
            val trustManager = trustAllCerts[0] as X509TrustManager
            builder.sslSocketFactory(sslContext.socketFactory, trustManager)
            builder.hostnameVerifier { _, _ -> true }
        } catch (e: Throwable) {
            android.util.Log.w("ApiClient", "Custom TLS initialization fallback on older device: ${e.message}")
        }

        builder.build()
    }

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private fun createRetrofit(url: String): Retrofit {
        return Retrofit.Builder()
            .baseUrl(url)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
    }

    @Volatile
    private var _apiService: FlikataApiService? = null

    val apiService: FlikataApiService
        get() {
            return _apiService ?: synchronized(this) {
                _apiService ?: createRetrofit(baseUrl).create(FlikataApiService::class.java).also {
                    _apiService = it
                }
            }
        }
}
