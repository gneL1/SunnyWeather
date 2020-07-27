package com.example.sunnyweather.logic.network

import com.example.sunnyweather.SunnyWeatherApplication
import com.example.sunnyweather.logic.model.PlaceResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

//访问彩云天气城市搜索API的Retrofit接口
interface PlaceService {
    /**
     * 在searchPlaces()方法的上面声明了一个@GET注解，当调用searchPlaces()方法的时候
     * Retrofit就会自动发起一条GET请求，去访问@GET注解中配置的地址
     * 需要动态指定的参数query使用@Query注解来实现
     * 不变的参数固定写在@GET注解中
     * 返回值声明成Call<PlaceResponse>,这样Retrofit会将服务器返回的JSON数据自动解析成PlaceResponse
     */
    //https://api.caiyunapp.com/v2/place?query=湖南&token=dokrTleAG8peBeyw&lang=zh_CN
    @GET("v2/place?token=${SunnyWeatherApplication.TOKEN}&lang=zh_CN")
    fun searchPlaces(@Query("query") query : String) : Call<PlaceResponse>

}