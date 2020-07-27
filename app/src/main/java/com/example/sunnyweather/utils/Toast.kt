package com.example.sunnyweather.utils

import android.content.Context
import android.widget.Toast
import com.example.sunnyweather.SunnyWeatherApplication
import java.time.Duration

fun String.toast(duration: Int = Toast.LENGTH_SHORT){
    Toast.makeText(SunnyWeatherApplication.context,this,duration).show()
}

fun Int.toast(duration: Int = Toast.LENGTH_SHORT){
    Toast.makeText(SunnyWeatherApplication.context,this,duration).show()
}