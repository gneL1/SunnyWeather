package com.example.sunnyweather.logic

import androidx.lifecycle.liveData
import com.example.sunnyweather.logic.model.Place
import com.example.sunnyweather.logic.model.Weather
import com.example.sunnyweather.logic.network.SunnyWeatherNetwork
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlin.RuntimeException
import kotlin.coroutines.CoroutineContext

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
//    fun searchPlaces(query : String) = liveData(Dispatchers.IO){
//        val result = try {
//            val placeResponse = SunnyWeatherNetwork.searchPlaces(query)
//            if (placeResponse.status == "ok"){
//                val places = placeResponse.places
//                Result.success(places)
//            }
//            else{
//                Result.failure(RuntimeException("response status is ${placeResponse.status}"))
//            }
//        }catch (e : Exception){
//            Result.failure<List<Place>>(e)
//        }
//        /**
//         * emit()类似于调用LiveData的setValue()来通知数据变化
//         * 只不过这里无法直接取得返回的LiveData对象，所以lifecycle-livedata-ktx提供了这样一个替代方法
//         */
//        emit(result)
//    }

    fun searchPlaces(query : String) = fire(Dispatchers.IO) {
        val placeResponse = SunnyWeatherNetwork.searchPlaces(query)
        if (placeResponse.status == "ok"){
            val places = placeResponse.places
            Result.success(places)
        }
        else{
            Result.failure(RuntimeException("response status is ${placeResponse.status}"))
        }
    }


//    fun refreshWeather(lng : String,lat : String) = liveData(Dispatchers.IO) {
//        val result = try {
//            /**
//             * 分别在2个async函数中发起网络请求，然后再分别调用它们的await()方法
//             * 可以保证只有在两个网络请求都成功响应之后，才会进一步执行程序
//             * 由于async函数必须在协程作用域内才能调用，所以这里又使用了coroutineScope函数创建了一个协程作用域
//             */
//            coroutineScope {
//                val deferredRealTime = async {
//                    SunnyWeatherNetwork.getRealtimeWeather(lng, lat)
//                }
//
//                val deferredDaily = async {
//                    SunnyWeatherNetwork.getDailyWeather(lng, lat)
//                }
//
//                val realtimeResponse = deferredRealTime.await()
//                val dailyResponse = deferredDaily.await()
//
//                /**
//                 * 在同时获取到RealtimeResponse和DailyResponse之后，如果它们的响应状态都是ok，
//                 * 就将Realtime和Daily对象取出并封装到一个Weather对象中，使用Result.success()方法包装这个Weather对象，
//                 * 否则使用Result.failure()包装一个异常信息，
//                 * 最后调用emit()将包装的结果发射出去
//                 */
//                if (realtimeResponse.status == "ok" && dailyResponse.status == "ok"){
//                    val weather = Weather(realtimeResponse.result.realTime,dailyResponse.result.daily)
//                    Result.success(weather)
//                }
//                else{
//                    Result.failure(
//                        RuntimeException("realtime response status is ${realtimeResponse.status}" +
//                                         "daily response status is ${dailyResponse.status}")
//                    )
//                }
//            }
//        }catch (e:Exception){
//            Result.failure<Weather>(e)
//        }
//
//        emit(result)
//    }

    fun refreshWeather(lng : String,lat : String) = fire(Dispatchers.IO) {
        coroutineScope {
            val deferredRealTime = async {
                SunnyWeatherNetwork.getRealtimeWeather(lng, lat)
            }
            val deferredDaily = async {
                SunnyWeatherNetwork.getDailyWeather(lng, lat)
            }
            val realtimeResponse = deferredRealTime.await()
            val dailyResponse = deferredDaily.await()
            /**
             * 在同时获取到RealtimeResponse和DailyResponse之后，如果它们的响应状态都是ok，
             * 就将Realtime和Daily对象取出并封装到一个Weather对象中，使用Result.success()方法包装这个Weather对象，
             * 否则使用Result.failure()包装一个异常信息，
             * 最后调用emit()将包装的结果发射出去
             */
            if (realtimeResponse.status == "ok" && dailyResponse.status == "ok"){
                val weather = Weather(realtimeResponse.result.realTime,dailyResponse.result.daily)
                Result.success(weather)
            }
            else{
                Result.failure(
                    RuntimeException("realtime response status is ${realtimeResponse.status}" +
                            "daily response status is ${dailyResponse.status}")
                )
            }
        }
    }

    /**
     * fire()函数是按照liveData()函数的参数接收标准定义的一个高阶函数
     * 在fire函数内部会先调用一下liveData函数，然后在liveData函数的代码块中统一进行了try catch处理，
     * 并在try语句中调用传入的Lambda表达式中的代码，最终获取Lambda表达式的执行结果并调用emit()方法发射出去
     * 在liveData()函数的代码块中是拥有挂起函数上下文的，可是回到Lambda表达式中，代码就没有挂起函数上下文了，
     * 但实际上Lambda表达式中的代码一定也是在挂起函数中运行的。
     * 所以需要在函数类型前声明一个suspend关键字，以表示所有传入的Lambda表达式中的代码也是拥有挂起函数上下文的
     */
    private fun <T> fire(context : CoroutineContext,block : suspend () -> Result<T>) =
        liveData<Result<T>>(context) {
            val result = try {
                block();
            }catch (e : Exception){
                Result.failure<T>(e)
            }
            emit(result)
        }

}