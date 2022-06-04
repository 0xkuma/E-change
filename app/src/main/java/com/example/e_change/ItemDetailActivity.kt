package com.example.e_change

import android.content.ContentValues.TAG
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import com.squareup.picasso.Picasso
import java.text.SimpleDateFormat
import java.util.*


class ItemDetailActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_item_detail)

        var name = findViewById<TextView>(R.id.detail_name)
        var lastMessage = findViewById<TextView>(R.id.detail_lastMessage)
        var imageView = findViewById<ImageView>(R.id.detail_imageView)
        Picasso.get().load(intent.getStringExtra("imageUrl")).into(imageView);

        Log.d(TAG, intent.getStringExtra("lastMsgTime").toString())

        name.text = intent.getStringExtra("name")
        lastMessage.text = intent.getStringExtra("lastMessage")
    }
}