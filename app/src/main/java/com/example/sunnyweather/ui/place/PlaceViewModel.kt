package com.example.sunnyweather.ui.place

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.sunnyweather.logic.Repository
import com.example.sunnyweather.logic.model.Place

/**
 * ViewModel层，ViewModel相当于逻辑层和UI层之间的一个桥梁
 * searchPlaces()没有直接调用仓库的searchPlaces()方法，
 * 而是将传入的搜索参数赋值给了一个searchLiveData对象，并使用Transformations的switchMap()方法观察这个对象
 * 否则仓库层返回的LiveData对象将无法观察
 * 每当searchPlaces()被调用时，switchMap()所对应的转换函数就会执行，
 * 在转换函数中，只需要调用仓库层定义的searchPlaces()就可以发起网络请求，
 * 同时将仓库层返回的LiveData对象转换成一个可供Activity观察的LiveData对象
 * placeList用于对界面上显示的城市数据进行缓存
 * 原则上与界面相关的数据都应该放到ViewModel中，这样可以保证它们在手机屏幕发生旋转时不会丢失
 *
 */
class PlaceViewModel : ViewModel() {
    private val searchLiveData = MutableLiveData<String>()

    val placeList = ArrayList<Place>()

    val placeLiveData = Transformations.switchMap(searchLiveData){
        Repository.searchPlaces(it)
    }

    fun searchPlaces(query : String){
        searchLiveData.value = query
    }

    fun savePlace(place: Place) = Repository.savePlace(place)

    fun getSavedPlace() = Repository.getSavedPlace()

    fun isPlaceSaved() = Repository.isPlaceSaved()
}