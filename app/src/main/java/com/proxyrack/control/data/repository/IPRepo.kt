package com.proxyrack.control.data.repository

import com.proxyrack.control.data.model.IpInfo
import com.proxyrack.control.data.network.IPInfoRetrofitInstance
import com.proxyrack.control.data.network.IpInfoApiService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class IpInfoRepository {
    private val apiService: IpInfoApiService = IPInfoRetrofitInstance.retrofit.create(IpInfoApiService::class.java)

    fun getIpInfo(callback: (IpInfo?, Throwable?) -> Unit) {
        val call = apiService.getIpInfo()
        call.enqueue(object : Callback<IpInfo> {
            override fun onResponse(call: Call<IpInfo>, response: Response<IpInfo>) {
                if (response.isSuccessful) {
                    callback(response.body(), null)
                } else {
                    callback(null, Throwable("Error: ${response.code()}"))
                }
            }

            override fun onFailure(call: Call<IpInfo>, t: Throwable) {
                callback(null, t)
            }
        })
    }
}
