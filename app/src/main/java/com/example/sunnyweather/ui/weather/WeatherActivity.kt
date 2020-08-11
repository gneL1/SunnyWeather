package com.example.sunnyweather.ui.weather

import android.content.Context
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.sunnyweather.R
import com.example.sunnyweather.logic.model.Weather
import com.example.sunnyweather.logic.model.getSky
import com.example.sunnyweather.utils.toast
import kotlinx.android.synthetic.main.activity_weather.*
import kotlinx.android.synthetic.main.forecast.*
import kotlinx.android.synthetic.main.life_index.*
import kotlinx.android.synthetic.main.now.*
import java.text.SimpleDateFormat
import java.util.*

class WeatherActivity : AppCompatActivity() {

    val viewModel by lazy {
        ViewModelProvider(this).get(WeatherViewModel::class.java)
    }

    /**
     * 首先从Intent中取出经纬度坐标和地区名称，并赋值到WeatherViewModel的相应变量中
     * 对weatherLiveData对象进行观察，当获取到服务器返回的天气数据时，就调用showWeatherInfo()方法进行解析与展示
     * 最后调用WeatherViewModel的refreshWeather()方法来执行一次刷新天气的请求
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        /**
         * 调用getWindows().getDecorView()方法拿到当前Activity的DecorView，
         * 再调用它的setSystemUiVisibility()方法来改变系统UI的显示，
         * 传入View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN和View.SYSTEM_UI_FLAG_LAYOUT_STABLE就表示Activity的布局会显示在状态栏上面
         * 最后调用以一下setStatusBarColor()方法将状态栏设置成透明色
         *
         * 由于系统状态栏已经成为布局的一部分，会导致天气界面的布局整体向上偏移了一些，头部布局会显得有些太靠上了
         * 给now.xml界面的头部布局增加android:fitsSystemWindows属性，设置成true表示会为系统状态栏留出空间
         */
        val decorView = window.decorView
        decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        window.statusBarColor = Color.TRANSPARENT

        setContentView(R.layout.activity_weather)
        if (viewModel.locationLng.isEmpty()){
            viewModel.locationLng = intent.getStringExtra("location_lng") ?: ""
        }

        if (viewModel.locationLat.isEmpty()){
            viewModel.locationLat = intent.getStringExtra("location_lat") ?: ""
        }

        if (viewModel.placeName.isEmpty()){
            viewModel.placeName = intent.getStringExtra("place_name") ?: ""
        }

        viewModel.weatherLiveData.observe(this, Observer {
            val weather = it.getOrNull()
            if (weather != null){
                showWeatherInfo(weather)
            }
            else{
                "无法成功获取天气信息".toast()
                it.exceptionOrNull()?.printStackTrace()
            }
            swipeRefresh.isRefreshing = false
        })

        /**
         * 将之前用于刷新天气信息的代码提取到了refreshWeather()方法中，在refreshWeather()中调用WeatherViewModel的refreshWeather()方法，
         * 并将SwipeRefreshLayout的isRefreshing属性设置成true，从而让下拉刷新进度条显示出来。
         * 在onCreate()方法中调用了SwipeRefreshLayout的setColorSchemeResources()方法来设置下拉刷新进度条的颜色
         * 调用setOnRefreshListener()方法给SwipeRefreshLayout设置一个下拉刷新的监听器
         * 当初发了下拉刷新操作时，就在监听器的回调中调用refreshWeather()方法来刷新天气信息
         * 当请求结束后，将SwipeRefreshLayout的isRefreshing属性设置成false，用于表示刷新事件结束，并隐藏刷新进度条
         */
        swipeRefresh.setColorSchemeResources(R.color.colorPrimary)
        refreshWeather()
        swipeRefresh.setOnRefreshListener {
            refreshWeather()
        }
//        viewModel.refreshWeather(viewModel.locationLng,viewModel.locationLat)

        /**
         * 在切换城市按钮的点击事件中调用DrawerLayout的openDrawer()方法来打开滑动菜单
         * 监听DrawerLayout的状态，当滑动菜单被隐藏的时候，同时也要隐藏输入法。
         * 在滑动菜单中搜索城市时会弹出输入法，而如果滑动菜单隐藏后输入法却还显示在界面上，会很奇怪
         */
        navBtn.setOnClickListener {
            drawerlayout.openDrawer(GravityCompat.START)
        }
        drawerlayout.addDrawerListener(object : DrawerLayout.DrawerListener {
            override fun onDrawerStateChanged(newState: Int) {
            }

            override fun onDrawerSlide(drawerView: View, slideOffset: Float) {
            }

            override fun onDrawerClosed(drawerView: View) {
                val manager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                manager.hideSoftInputFromWindow(drawerView.windowToken,InputMethodManager.HIDE_NOT_ALWAYS)

            }

            override fun onDrawerOpened(drawerView: View) {
            }

        })
    }

    /**
     * 从Weather对象中获取数据，显示到相应的控件上
     * 在未来几天天气预报的部分，使用一个for-in循环来处理每天的天气信息，
     * 在循环中动态加载forecast_item.xml布局来设置相应的数据，然后添加到父布局中
     * 生活指数方面虽然服务器会返回很多天的数据，但界面上只需要当前的数据，所以对所有生活指数都取了下标为0的数据
     */
    private fun showWeatherInfo(weather : Weather){
        placeName.text = viewModel.placeName
        val realtime =  weather.realtime
        val daily = weather.daily

        //填充now.xml布局中的数据
        val currentTempText = "${realtime.temperature.toInt()} ℃"
        currentTemp.text = currentTempText
        currentSky.text = getSky(realtime.skycon).info
        val currentPM25Text = "空气指数 ${realtime.airQuality.aqi.chn.toInt()}"
        currentAQI.text = currentPM25Text
        nowLayout.setBackgroundResource(getSky(realtime.skycon).bg)

        //填充forecast.xml布局中的数据
        forecastLayout.removeAllViews()
        val days = daily.skycon.size
        for (i in 0 until days){
            val skycon = daily.skycon[i]
            val temperature = daily.temperature[i]
            val view = LayoutInflater.from(this).inflate(R.layout.forecast_item,forecastLayout,false)
            val dateInfo = view.findViewById(R.id.dateInfo) as TextView
            val skyIcon = view.findViewById(R.id.skyIcon) as ImageView
            val skyInfo = view.findViewById(R.id.skyInfo) as TextView
            val temperatureInfo = view.findViewById(R.id.temperatureInfo) as TextView
            val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            dateInfo.text = simpleDateFormat.format(skycon.date)
            val sky = getSky(skycon.value)
            skyIcon.setImageResource(sky.icon)
            skyInfo.text = sky.info
            val tempText = "${temperature.min.toInt()} ~ ${temperature.max.toInt()} ℃"
            temperatureInfo.text = tempText
            forecastLayout.addView(view)
        }

        //填充life_index.xml布局中的数据
        val lifeIndex = daily.lifeIndex
        coldRiskText.text = lifeIndex.coldRisk[0].desc
        dressingText.text = lifeIndex.dressing[0].desc
        ultravioletText.text = lifeIndex.ultraviolet[0].desc
        carWashingText.text = lifeIndex.carWashing[0].desc
        weatherLayout.visibility = View.VISIBLE

    }


    fun refreshWeather(){
        viewModel.refreshWeather(viewModel.locationLng,viewModel.locationLat)
        swipeRefresh.isRefreshing = true
    }

}
