package com.example.sunnyweather.ui.place

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.example.sunnyweather.R
import com.example.sunnyweather.logic.model.Place
import com.example.sunnyweather.ui.weather.WeatherActivity
import kotlinx.android.synthetic.main.place_item.*
import org.w3c.dom.Text

//把PlaceAdapter主构造函数中传入的Fragment对象改成PlaceFragment对象，
//这样就可以调用PlaceFragment所对应的PlaceViewModel了
class PlaceAdapter (private val fragment: PlaceFragment,private val placeList : List<Place>) : RecyclerView.Adapter<PlaceAdapter.ViewHolder>() {

    inner class ViewHolder(view : View) : RecyclerView.ViewHolder(view) {
        val placeAddress : TextView = view.findViewById(R.id.placeAddress)
        val placeName : TextView = view.findViewById(R.id.placeName)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.place_item,parent,false)

        /**
         * 给place_item.xml的最外层布局注册一个点击事件监听器，
         * 在点击事件在获取当前点击项的经纬度坐标和地区名称，并把它们传入Intent中，
         * 最后调用Fragment的StartActivity()方法启动WeatherActivity
         */
        val holder = ViewHolder(view)
        holder.itemView.setOnClickListener {
            val position = holder.adapterPosition
            val place = placeList[position]
            val intent = Intent(parent.context,WeatherActivity::class.java).apply {
                putExtra("location_lng",place.location.lng)
                putExtra("location_lat",place.location.lat)
                putExtra("place_name",place.name)
            }
            //当点击了任何子项布局时，在跳转到WeatherActivity之前，先调用PlaceViewModel的savePlace()方法来存储选中的城市
            fragment.viewModel.savePlace(place)
            fragment.startActivity(intent)
            fragment.activity?.finish()
        }
        return holder
    }

    override fun getItemCount(): Int {
        return placeList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val place = placeList[position]
        holder.placeName.text = place.name
        holder.placeAddress.text = place.address

    }

}