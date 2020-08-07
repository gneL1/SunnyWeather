package com.example.sunnyweather.logic.network

import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.await
import java.lang.RuntimeException
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

/**
 * 网络数据源访问路口，对所有网络请求的API进行封装
 */
object SunnyWeatherNetwork {

    /**
     * 使用ServiceCreator构建了一个动态代理对象
     * 然后定义一个searchPlaces()函数，在这里调用在PlaceService接口中定义的searchPlaces()方法
     * 发起搜索城市数据请求
     * 当外部调用SunnyWeatherNetwork的searchPlaces()函数时，Retrofit就会立刻发起网络请求，
     * 同时当前的协程也会被阻塞住。直到服务器响应请求后，await()函数会将解析出来的数据模型对象取出并返回
     * 同时恢复当前协程的执行，searchPlaces()函数在得到await()的返回之后会将该数据再返回到上一层
     */
    private val placeService = ServiceCreator.create<PlaceService>()

    suspend fun searchPlaces(query : String) = placeService.searchPlaces(query).await()

    private val weatherService = ServiceCreator.create<WeatherService>()

    suspend fun getDailyWeather(lng : String, lat : String) = weatherService.getDailyWeather(lng,lat).await()

    suspend fun getRealtimeWeather(lng: String,lat: String) = weatherService.getRealtimeWeather(lng, lat).await()


    private suspend fun <T> Call<T>.await():T{
        return suspendCoroutine {
            enqueue(object : Callback<T> {
                override fun onFailure(call: Call<T>, t: Throwable) {
                    it.resumeWithException(t)
                }

                override fun onResponse(call: Call<T>, response: Response<T>) {
                    val body = response.body()
                    if (body != null){
                        it.resume(body)
                    }
                    else{
                        it.resumeWithException(
                            RuntimeException("response body is null!")
                        )
                    }
                }

            })
        }
    }
}