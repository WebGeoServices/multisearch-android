package com.webgeoservices.sanginfo.sampleapp.utils

import android.content.Context
import android.graphics.Color
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.text.Spannable
import android.text.SpannableString
import android.text.style.BackgroundColorSpan
import android.util.Log
import android.util.TypedValue
import org.json.JSONArray
import org.json.JSONObject

class Util {
    companion object {
        fun isNetworkAvailable(context: Context?): Boolean {
            if (context == null) return false
            val connectivityManager =
                context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val nw = connectivityManager.activeNetwork ?: return false
            val actNw = connectivityManager.getNetworkCapabilities(nw) ?: return false
            return when {
                actNw.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
                actNw.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
                //for other device how are able to connect with Ethernet
                actNw.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
                //for check internet over Bluetooth
                actNw.hasTransport(NetworkCapabilities.TRANSPORT_BLUETOOTH) -> true
                else -> false
            }
        }

        fun dpToPx(context: Context, dip: Float): Int {
            val px = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                dip,
                context.resources.displayMetrics
            )
            return px.toInt()
        }

        fun highlightMatchSubString(
            description: String,
            subStringData: JSONArray?
        ): SpannableString {
            val spannableString = SpannableString(description)
            try {
                if (subStringData != null && subStringData.length() > 0) {
                    for (item in 0 until subStringData.length()) {
                        val data = subStringData[item] as JSONObject
                        val length = data.getInt("length")
                        val offset = data.getInt("offset")
                        spannableString.setSpan(
                            BackgroundColorSpan(Color.YELLOW),
                            offset,
                            offset + length,
                            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                        )
                    }
                }

            } catch (ex: Exception) {
                Log.e("highlightMatchSubString", ex.toString())
            }
            return spannableString
        }


    }
}