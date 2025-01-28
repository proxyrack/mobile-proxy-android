package com.proxyrack.control.data.network

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException
import java.net.Proxy
import java.net.ProxySelector
import java.net.SocketAddress
import java.net.URI

object IPInfoRetrofitInstance {
    private const val BASE_URL = "https://check.ragpets.com/"

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val httpClient = OkHttpClient.Builder()
        // https://github.com/square/okhttp/issues/6877#issuecomment-1438554879
        // Add our own proxy selector to guard against the case that the user has invalid
        // system proxy settings. If the system settings are invalid we fallback to using
        // no proxy for the request.
        .proxySelector(object : ProxySelector() {
            override fun select(uri: URI?): List<Proxy> {
                return try {
                    getDefault().select(uri)
                } catch (e: Exception) {
                    listOf(Proxy.NO_PROXY)
                }
            }

            override fun connectFailed(uri: URI?, sa: SocketAddress?, ioe: IOException?) {
                getDefault().connectFailed(uri, sa, ioe)
            }
        })
        .addInterceptor(loggingInterceptor)
        .build()

    val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(httpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
}
