package com.example.sunnyweather

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context

/**
 * 在任意地方通过调用SunnyWeatherApplication.context来获取Context对象
 */
class SunnyWeatherApplication : Application() {

    companion object{
        @SuppressLint("StaticFieldLeak")
        lateinit var context: Context

        //彩云天气申请的令牌
        const val TOKEN = "dokrTleAG8peBeyw"
    }

    override fun onCreate() {
        super.onCreate()
        context = applicationContext
    }
}