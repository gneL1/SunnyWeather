package com.example.sunnyweather.logic.model

import com.google.gson.annotations.SerializedName


data class PlaceResponse(val status : String,val places : List<Place>)

//使用@SerializedName注解，来让JSON字段和Kotlin字段之间建立映射
data class Place(val name : String,val location:Location,
                 @SerializedName("formatted_address")val address : String)

data class Location(val lng : String,val lat : String)
