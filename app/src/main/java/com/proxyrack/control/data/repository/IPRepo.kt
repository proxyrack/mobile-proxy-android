package com.proxyrack.control.data.repository

import com.proxyrack.control.data.network.IPInfoRetrofitInstance
import com.proxyrack.control.data.network.IpInfoApiService
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class IpInfoRepository {
    private val apiService: IpInfoApiService = IPInfoRetrofitInstance.retrofit.create(IpInfoApiService::class.java)

    fun getIpInfo(callback: (String?, Throwable?) -> Unit) {
        val call = apiService.getIpInfo()
        call.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    val responseBody = response.body()
                    if (responseBody != null) {
                        callback(responseBody.string(), null)
                    } else {
                        callback(null, Throwable("Error: Empty response body"))
                    }
                } else {
                    callback(null, Throwable("Error: ${response.code()}"))
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                callback(null, t)
            }
        })
    }
}
