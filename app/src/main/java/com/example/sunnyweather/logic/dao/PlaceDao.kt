package com.example.sunnyweather.logic.dao

import android.content.Context
import androidx.core.content.edit
import com.example.sunnyweather.SunnyWeatherApplication
import com.example.sunnyweather.logic.model.Place
import com.google.gson.Gson

/**
 * savePlace()用于将Place对象存储到SharedPreferences文件中，
 * 先通过GSON将Place对象转成一个JSON字符串，然后就可以用字符串存储的方式来保存数据了
 * 在getSavePlace()中先将JSON字符串从SharedPreferences文件中读取出来，再通过GSON将JSON字符串解析成Place对象并返回
 * isPlaceSaved()用于判断是否有数据已被存储
 */
object PlaceDao {
    fun savePlace(place : Place){
        sharedPreferences().edit {
            putString("place",Gson().toJson(place))
        }
    }

    fun getSavePlace() : Place{
        val placeJson = sharedPreferences().getString("place","")
        return Gson().fromJson(placeJson,Place::class.java)
    }

    fun isPlaceSaved() = sharedPreferences().contains("place")

    private fun sharedPreferences() = SunnyWeatherApplication.context.getSharedPreferences("sunny_weather",Context.MODE_PRIVATE)
}