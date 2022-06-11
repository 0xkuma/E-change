package com.example.e_change

import android.content.ContentValues.TAG
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ListView
import com.example.e_change.databinding.ActivityMainBinding
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class ItemActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var itemArrayList: ArrayList<Item>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)

        setContentView(R.layout.activity_item)

        val db = Firebase.firestore

        itemArrayList = ArrayList()
        var imageIdList = ArrayList<String>()
        var nameList = ArrayList<String>()
        var lastMsgTimeList = ArrayList<com.google.firebase.Timestamp>()
        var lastMessageList = ArrayList<String>()
        var imageUrlList = ArrayList<String>()
        var latitudeList = ArrayList<Double>()
        var longitudeList = ArrayList<Double>()

        db.collection("items").whereEqualTo("status","waiting").get().addOnSuccessListener { result ->
            for(document in result) {
                Log.d(TAG, "${document.id} => ${document.data}")
                var imageId = document.id
                imageIdList.add(imageId)
                var name = document.data?.get("name") as String
                nameList.add(name)
                var lastMsgTime = document.data?.get("post_time") as com.google.firebase.Timestamp
                lastMsgTimeList.add(lastMsgTime)
                var lastMessage = document.data?.get("status") as String
                lastMessageList.add(lastMessage)
                var imageUrl = document.data?.get("image_url") as String
                imageUrlList.add(imageUrl)
                var latitude = document.data?.get("latitude") as Double
                latitudeList.add(latitude)
                var longitude = document.data?.get("longitude") as Double
                longitudeList.add(longitude)
                var item = Item(name, lastMessage, lastMsgTime, imageId, imageUrl)
                itemArrayList.add(item)
            }
            Log.d(TAG, imageUrlList.toString())
            val listview = findViewById<ListView>(R.id.listview)
            listview.isClickable = true
            listview.adapter = MyAdapter(this, itemArrayList)
            listview.setOnItemClickListener { parent, view, position, id ->
                var intent = Intent(this, ItemDetailActivity::class.java)

                intent.putExtra("imageId", imageIdList[position])
                intent.putExtra("name",nameList[position])
                intent.putExtra("lastMsgTime",lastMsgTimeList[position])
                intent.putExtra("lastMessage",lastMessageList[position])
                intent.putExtra("imageUrl",imageUrlList[position])
                intent.putExtra("latitude", latitudeList[position])
                intent.putExtra("longitude", longitudeList[position])

                startActivity(intent)
            }

            val fab = findViewById<FloatingActionButton>(R.id.fab)
            fab.setOnClickListener {
                var intent = Intent(this, CreateActivity::class.java)
                startActivity(intent)
            }
        }
    }
}