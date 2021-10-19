package com.webgeoservices.sanginfo.sampleapp.views

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.ActionBar
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.Toolbar
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.webgeoservices.multisearch.searchdatamodels.DetailsResponseItem
import com.webgeoservices.multisearch.searchdatamodels.Location
import com.webgeoservices.sanginfo.sampleapp.R

class MapActivity : AppCompatActivity(), OnMapReadyCallback {
    private val TAG = "MapActivity"
    private lateinit var mMap: GoogleMap
    private lateinit var details: DetailsResponseItem
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)
        initializeSupportBar()
        getIntentData()
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        var detail=findViewById<AppCompatButton>(R.id.detail)
        detail.setOnClickListener {
            it?.apply { isEnabled = false; postDelayed({ isEnabled = true }, 1000) }
            moveToDetailActivity()
        }
    }

    override fun onMapReady(map: GoogleMap?) {
        if (map != null) {
            mMap = map
        }
        showMarker()
    }

    private fun getIntentData() {
        if (intent.hasExtra("details")) {
            val data = intent.getParcelableExtra<DetailsResponseItem>("details")
            if (data != null) {
                details = data
            }
        }
    }

    private fun showMarker() {
        if (this::details.isInitialized) {
            val location: Location = details.geometry.location
            val latlng = LatLng(location.lat, location.lng)
            val markerOption = MarkerOptions().position(latlng).title(details.formattedAddress)
            val marker = mMap.addMarker(markerOption)
            marker?.showInfoWindow()
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latlng, 14f))
        }

    }

    private fun initializeSupportBar() {
        val back: ImageView
        try {
            supportActionBar!!.displayOptions = ActionBar.DISPLAY_SHOW_CUSTOM
            supportActionBar!!.setDisplayShowCustomEnabled(true)
            supportActionBar!!.setCustomView(R.layout.custom_action_bar)
            val parent = supportActionBar?.customView?.parent as Toolbar
            parent.setPadding(0, 0, 0, 0)//for tab otherwise give space in tab
            parent.setContentInsetsAbsolute(0, 0)
            val view = supportActionBar!!.customView
            back = view.findViewById(R.id.back)
            back.setOnClickListener {
                finish()
            }

        } catch (ex: Exception) {
            Log.e(TAG, ex.toString())
        }
    }

    private fun moveToDetailActivity() {
        if(this::details.isInitialized){
            val intent = Intent(this, DetailJsonViewer::class.java)
            intent.putExtra("details", details)
            startActivityForResult(intent, 151)
        }
    }

}