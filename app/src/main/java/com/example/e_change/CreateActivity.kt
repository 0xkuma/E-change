package com.example.e_change

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.ContentValues.TAG
import android.content.Intent
import android.graphics.Bitmap
import android.location.*
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Looper
import android.provider.MediaStore
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.gms.location.*
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import java.io.ByteArrayOutputStream
import java.util.*

class CreateActivity : AppCompatActivity(), OnMapReadyCallback {
    var storage = Firebase.storage
    var storageRef = storage.reference
    private lateinit var mMap: GoogleMap

    protected var mLastLocation: Location? = null
    protected var mLocationRequest: LocationRequest? = null
    protected var mGeocoder: Geocoder? = null
    protected var mLocationProvider: FusedLocationProviderClient? = null

    companion object {
        var REQUEST_LOCATION = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create)
        val cameraBtn:Button = findViewById(R.id.cameraBtn)
        cameraBtn.setOnClickListener {
            dispatchTakePictureIntent()
        }

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

        val map = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        map.getMapAsync(this)

        mLocationProvider!!.requestLocationUpdates(
            mLocationRequest!!,
            mLocationCallBack, Looper.getMainLooper()
        )
        mLocationProvider!!.lastLocation.addOnSuccessListener{ location ->
            // Got last known location. In some rare situations this can be null.
            // 3
            if (location != null) {
                val currentLatLng = LatLng(location.latitude, location.longitude)
                mMap.addMarker(
                    MarkerOptions()
                        .position(currentLatLng)
                        .title("Your location"))
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 12f))
                val removeTask = mLocationProvider!!.removeLocationUpdates(mLocationCallBack)
                removeTask.addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Log.d(TAG, "Location Callback removed.")
//                stopSelf()
                    } else {
                        Log.d(TAG, "Failed to remove Location Callback.")
                    }
                }
            }
        }

    }

    val REQUEST_IMAGE_CAPTURE = 1

    private fun dispatchTakePictureIntent() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        try {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
        } catch (e: ActivityNotFoundException) {
            // display error state to the user
        }
    }

    var mLocationCallBack: LocationCallback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            mLastLocation = result.lastLocation
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            val imageView: ImageView = findViewById(R.id.cameraImage)
            val imageBitmap = data?.extras?.get("data") as Bitmap
            imageView.setImageBitmap(imageBitmap)
            val imageUid = UUID.randomUUID()
            val mountainsRef = storageRef.child("${imageUid}.jpg")
            val mountainImagesRef = storageRef.child("images/mountains.jpg")

            mountainsRef.name == mountainImagesRef.name // true
            mountainsRef.path == mountainImagesRef.path // false
            val baos = ByteArrayOutputStream()
            imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
            val data = baos.toByteArray()
            var uploadTask = mountainsRef.putBytes(data)
            uploadTask.addOnFailureListener {
                // Handle unsuccessful uploads
            }.addOnSuccessListener { taskSnapshot ->
                // taskSnapshot.metadata contains file metadata such as size, content-type, etc.
                // ...

            }

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