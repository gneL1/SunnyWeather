package com.example.sunnyweather.ui.weather

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.example.sunnyweather.logic.Repository
import com.example.sunnyweather.logic.model.Location
import com.example.sunnyweather.logic.model.Place
import com.example.sunnyweather.logic.model.Weather
import kotlin.math.ln

class WeatherViewModel : ViewModel(){

    private val locationLiveData = MutableLiveData<Location>()

    /**
     * locationLng、locationLat、placeName是和界面相关的数据
     * 放到ViewModel中可以保证它们在手机屏幕发生旋转的时候不会丢失
     */
    val locationLng = ""

    val locationLat = ""

    val placeName = ""


    /**
     * 定义一个refreshWeather()方法来刷新天气信息h
     * 将传入的经纬度参数封装成一个Location对象后赋值给locationLiveData对象
     * 然后使用Transformations的switchMap()方法来观察这个对象
     * 并在switchMap()方法的转换函数中调用仓库层中定义的refreshWeather()方法，
     * 将仓库层返回的LiveData对象转换成一个供Activity观察的LiveData对象
     */
    val weatherLiveData = Transformations.switchMap(locationLiveData){
        Repository.refreshWeather(it.lng,it.lat)
    }


    fun refreshWeather(lng : String,lat : String){
        locationLiveData.value = Location(lng, lat)
    }

}