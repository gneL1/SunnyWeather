package com.example.sunnyweather.logic

import androidx.lifecycle.liveData
import com.example.sunnyweather.logic.model.Place
import com.example.sunnyweather.logic.network.SunnyWeatherNetwork
import kotlinx.coroutines.Dispatchers
import java.lang.RuntimeException

/**
 * 仓库层，判断调用方请求的数据应该是从本地数据源获取还是从网络数据源获取
 * 并将获得的数据返回给调用方
 */
object Repository {
    /**
     * liveData()是lifecycle-livedata-ktx库提供的功能，可以自动构建并返回一个LiveData对象
     * 在它的代码块中提供一个挂起函数的上下文，这样就能在liveData()函数的代码块中调用任意的挂起函数
     * 这里调用SunnyWeatherNetwork的searchPlaces()搜索城市数据，然后判断如果服务器响应状态是ok
     * 就是要Kotlin内置的Result.success()来包装获取的城市数据列表
     * 否则使用Result.failure()来包装一个异常信息
     * 最后使用一个emit()方法将包装的结果发射出去
     *
     * 将liveData()的线程参数指定为Dispatchers.IO，这样代码块中的所有代码就都运行在子线程中
     * Android不允许在主线程中进行网络请求，读写数据库之类的本地数据操作也不建议在主线程中进行
     */
    fun searchPlaces(query : String) = liveData(Dispatchers.IO){
        val result = try {
            val placeResponse = SunnyWeatherNetwork.searchPlaces(query)
            if (placeResponse.status == "ok"){
                val places = placeResponse.places
                Result.success(places)
            }
            else{
                Result.failure(RuntimeException("response status is ${placeResponse.status}"))
            }
        }catch (e : Exception){
            Result.failure<List<Place>>(e)
        }
        /**
         * emit()类似于调用LiveData的setValue()来通知数据变化
         * 只不过这里无法直接取得返回的LiveData对象，所以lifecycle-livedata-ktx提供了这样一个替代方法
         */
        emit(result)
    }
}