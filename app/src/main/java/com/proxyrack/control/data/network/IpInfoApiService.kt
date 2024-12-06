package com.proxyrack.control.data.network
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.GET

interface IpInfoApiService {
    @GET("/ip")
    fun getIpInfo(): Call<ResponseBody>
}
