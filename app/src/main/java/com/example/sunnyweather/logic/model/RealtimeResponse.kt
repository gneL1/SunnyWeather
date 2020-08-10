package com.example.sunnyweather.logic.model

import com.google.gson.annotations.SerializedName


/**
 * 获取实时天气信息
 * 将所有数据模型类定义在RealtimeResponse的内部，可以防止出现和其他接口的数据模型类有同名冲突的情况
 */
data class RealtimeResponse(val status : String,val result: Result){

    data class Result(val realtime : RealTime)

    data class RealTime(val temperature : Float,val skycon : String,@SerializedName("air_quality") val airQuality: AirQuality)

    data class AirQuality(val aqi : AQI)

    data class AQI(val chn : Float)

}