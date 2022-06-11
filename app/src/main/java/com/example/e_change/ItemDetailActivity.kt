package com.example.e_change

import android.Manifest
import android.content.ContentValues.TAG
import android.content.Intent
import android.location.Geocoder
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.squareup.picasso.Picasso
import java.text.SimpleDateFormat
import java.util.*


class ItemDetailActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    protected var mLastLocation: Location? = null
    protected var mLocationRequest: LocationRequest? = null
    protected var mLocationProvider: FusedLocationProviderClient? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_item_detail)

        var name = findViewById<TextView>(R.id.detail_name)
        var lastMessage = findViewById<TextView>(R.id.detail_lastMessage)
        var imageView = findViewById<ImageView>(R.id.detail_imageView)
        val storage = Firebase.storage
        val storageRef = storage.reference
        intent.getStringExtra("imageUrl")?.let {
            storageRef.child(it).downloadUrl.addOnSuccessListener { img ->
                Log.d(TAG, img.toString())
                // Data for "images/island.jpg" is returned, use this as needed
                Picasso.get().load(img).into(imageView);

            }.addOnFailureListener { error ->
                Log.e(TAG, error.toString())
                // Handle any errors
            }
        }

        Log.d(TAG, intent.getStringExtra("lastMsgTime").toString())

        name.text = intent.getStringExtra("name")
        lastMessage.text = intent.getStringExtra("lastMessage")

        val locationPermissionRequest = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions(),
            ActivityResultCallback<Map<String?, Boolean?>> { result: Map<String?, Boolean?> ->
                val fineLocationGranted = result.getOrDefault(
                    Manifest.permission.ACCESS_FINE_LOCATION, false
                )
                val coarseLocationGranted = result.getOrDefault(
                    Manifest.permission.ACCESS_COARSE_LOCATION, false
                )
                if (fineLocationGranted != null && fineLocationGranted) {
                    // Precise location access granted.
                    // permissionOk = true;
                    Log.d(TAG, "permission granted")
                } else if (coarseLocationGranted != null && coarseLocationGranted) {
                    // Only approximate location access granted.
                    Log.d(TAG, "permission granted")
                } else {
                    // permissionOk = false;
                    // No location access granted.
                    Log.d(TAG, "permission not granted")
                }
            }
        )
        locationPermissionRequest.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
        mLocationRequest = LocationRequest()
        mLocationRequest!!.interval = 100
        mLocationRequest!!.fastestInterval = 5
        mLocationRequest!!.priority = LocationRequest.PRIORITY_HIGH_ACCURACY

        mLocationProvider = LocationServices.getFusedLocationProviderClient(this)

        val map = supportFragmentManager.findFragmentById(R.id.detail_map) as SupportMapFragment
        map.getMapAsync(this)

        mLocationProvider!!.requestLocationUpdates(
            mLocationRequest!!,
            mLocationCallBack, Looper.getMainLooper()
        )
        var currentLatLng: LatLng? = null
        mLocationProvider!!.lastLocation.addOnSuccessListener{ location ->
            // Got last known location. In some rare situations this can be null.
            if (location != null) {
                var latitude = intent.getDoubleExtra("latitude", 0.0)
                var longitude = intent.getDoubleExtra("longitude", 0.0)
                currentLatLng = LatLng(latitude, longitude)
                Log.d(TAG, currentLatLng.toString())
                mMap.addMarker(
                    MarkerOptions()
                        .position(currentLatLng!!)
                        .title("Target location"))
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng!!, 12f))
                val removeTask = mLocationProvider!!.removeLocationUpdates(mLocationCallBack)
                removeTask.addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Log.d(TAG, "Location Callback removed.")
                    } else {
                        Log.d(TAG, "Failed to remove Location Callback.")
                    }
                }
            }
        }

        val holdBtn: Button = findViewById(R.id.holdBtn)
        holdBtn.setOnClickListener {
            var id = intent.getStringExtra("imageId").toString()
            val itemRef = Firebase.firestore.collection("items")
            itemRef.document(id)
                .update(mapOf(
                    "status" to "hold"
                ))
            Toast.makeText(baseContext, "Item success to hold.",
                Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, ItemActivity::class.java))
        }

    }

    var mLocationCallBack: LocationCallback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            mLastLocation = result.lastLocation
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        // Add a marker in Sydney and move the camera
        val sydney = LatLng(40.73, -73.99)
        mMap.addMarker(
            MarkerOptions()
                .position(sydney)
                .title("Marker in Sydney"))
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney))
    }

}