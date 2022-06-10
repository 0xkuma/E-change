package com.example.e_change

import android.content.ContentValues.TAG
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ListView
import com.example.e_change.databinding.ActivityMainBinding
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

        db.collection("items").whereEqualTo("status","waiting").get().addOnSuccessListener { result ->
            for(document in result) {
                Log.d(TAG, "${document.id} => ${document.data}")
                var imageId = document.id
//                Log.d(TAG, imageId.size.toString())
                var name = document.data?.get("name") as String
                var lastMsgTime = document.data?.get("post_time") as com.google.firebase.Timestamp
                var lastMessage = document.data?.get("status") as String
                var imageUrl = document.data?.get("image_url") as String
                var item = Item(name, lastMessage, lastMsgTime, imageId, imageUrl)
                itemArrayList.add(item)
            }
            Log.d(TAG, itemArrayList.toString())
            val listview = findViewById<ListView>(R.id.listview)
            listview.isClickable = true
            listview.adapter = MyAdapter(this, itemArrayList)
            listview.setOnItemClickListener { parent, view, position, id ->
                var intent = Intent(this, LoginActivity::class.java)
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