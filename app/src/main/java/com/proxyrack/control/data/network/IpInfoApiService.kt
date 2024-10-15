package com.proxyrack.control.data.network
import com.proxyrack.control.data.model.IpInfo
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Headers

interface IpInfoApiService {
    @Headers("Accept: application/json")
    @GET("/")
    fun getIpInfo(): Call<IpInfo>
}
