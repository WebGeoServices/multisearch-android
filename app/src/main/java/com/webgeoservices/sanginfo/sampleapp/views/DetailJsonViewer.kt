package com.webgeoservices.sanginfo.sampleapp.views

import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.google.gson.Gson
import com.paulvarry.jsonviewer.JsonViewer
import com.webgeoservices.multisearch.searchdatamodels.DetailsResponseItem
import com.webgeoservices.sanginfo.sampleapp.R
import org.json.JSONObject

class DetailJsonViewer : AppCompatActivity() {
    private val TAG = "DetailJsonViewer"
    private lateinit var detailsJson: String
    private lateinit var jasonViewer: JsonViewer
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail_json_viewer)
        initializeSupportBar()
        getIntentData()
        initializeActivityControl()
    }

    private fun initializeSupportBar() {
        val back: ImageView
        val header: TextView
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
            header = view.findViewById(R.id.header_text)
            header.text = getString(R.string.open_details)
        } catch (ex: Exception) {
            Log.e(TAG, ex.toString())
        }
    }

    private fun getIntentData() {
        try {
            if (intent.hasExtra("details")) {
                val data = intent.getParcelableExtra<DetailsResponseItem>("details")
                if (data != null) {
                    val json=Gson().toJson(data);
                    val jsonObject= JSONObject(json)
                    if(jsonObject.has("item")){
                        jsonObject.remove("item")
                    }
                    jsonObject.put("item",data.item)
                    detailsJson = jsonObject.toString();
                }
            }
        } catch (ex: Exception) {
            Log.e(TAG, ex.toString())
        }

    }

    private fun initializeActivityControl() {
        try {
            jasonViewer = findViewById(R.id.jsonViewer)
            jasonViewer.setJson(JSONObject(detailsJson))
        } catch (ex: Exception) {
            Log.e(TAG, ex.toString())
        }


    }

}