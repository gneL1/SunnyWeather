package com.example.sunnyweather.ui.place

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.sunnyweather.MainActivity
import com.example.sunnyweather.R
import com.example.sunnyweather.ui.weather.WeatherActivity
import com.example.sunnyweather.utils.toast
import kotlinx.android.synthetic.main.fragment_place.*

class PlaceFragment : Fragment() {

    /**
     * 使用懒加载来获取PlaceViewModel的实例
     */
    val viewModel by lazy {
        ViewModelProvider(this).get(PlaceViewModel::class.java)
    }

    private lateinit var adapter : PlaceAdapter

    /**
     * 重写onCreateView()方法，通过LayoutInflater的inflate()方法将布局动态加载进来
     */
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_place,container,false)
    }

    /**
     * 先给RecyclerView设置LayoutManager和适配器，并使用PlaceViewModel中的placeList集合作为数据源
     * 调用EditText的addTextChangeListener()来监听搜索框输入内容的变化情况
     * 每当搜索框中的内容发生变化，就获取新的数据，然后传递给PlaceViewModel的searchPlaces(),发起搜索城市数据的网络请求
     * 输入内容为空时，将RecyclerView隐藏起来，同时将背景图显示出来
     */
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        /**
         * 在PlaceFragment中进行判断，如果当前已有存储的城市数据，就获取已存储的数据并解析成Place对象
         * 然后使用它的经纬度坐标和城市名直接跳转并传递给WeatherActivity，这样就不用每次都重新搜索并选择城市了
         *
         * 假若已经右选中的城市保存在SharedPreferences文件中，就直接跳转到WeatherActivity
         * 将PlaceFragment嵌入WeatherActivity中之后，如果还执行这段逻辑会造成无限循环跳转的情况
         * 设置activity is MainActivity  只有当PlaceFragment被嵌入MainActivity，
         * 并且之前已经存在选中的城市，此时才会直接跳转到WeatherActivity
         */
        if (viewModel.isPlaceSaved() && activity is MainActivity){
            print("被创建并进行跳转");
            val place = viewModel.getSavedPlace()
            val intent = Intent(context,WeatherActivity::class.java).apply {
                putExtra("location_lng",place.location.lng)
                putExtra("location_lat",place.location.lat)
                putExtra("place_name",place.name)
            }
            startActivity(intent)
            activity?.finish()
            return
        }

        val layoutManager = LinearLayoutManager(activity)
        recyclerView.layoutManager = layoutManager
        adapter = PlaceAdapter(this,viewModel.placeList)
        recyclerView.adapter = adapter
        searchPlaceEdit.addTextChangedListener {
            editable ->
            val content = editable.toString()
            if (content.isNotEmpty()){
                viewModel.searchPlaces(content)
            }
            else{
                recyclerView.visibility = View.GONE
                bgImageView.visibility = View.VISIBLE
                viewModel.placeList.clear()
                adapter.notifyDataSetChanged()
            }
        }

        /**
         * 对PlaceViewModel中的placeLiveData对象进行观察，
         * 当有数据变化时，就会回调到Observer接口实现中，然后对回调的数据进行判断
         * 如果数据不为空，就将数据添加到PlaceViewModel的placeList集合中，并通知PlaceAdapter刷新界面
         * 如果数据为空，则说明发生了异常，弹出Toast提示，并将具体的异常原因打印出来
         */
        viewModel.placeLiveData.observe(viewLifecycleOwner, Observer {
            result ->
            val places = result.getOrNull()
            if (places != null){
                recyclerView.visibility = View.VISIBLE
                bgImageView.visibility = View.GONE
                viewModel.placeList.clear()
                viewModel.placeList.addAll(places)
                adapter.notifyDataSetChanged()
            }
            else{
                "未能查询到任何地点".toast()
                result.exceptionOrNull()?.printStackTrace()
            }
        })
    }

}