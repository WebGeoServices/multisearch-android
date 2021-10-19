package com.webgeoservices.sanginfo.sampleapp.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.webgeoservices.sanginfo.sampleapp.listeners.RecyclerItemClickListener
import com.webgeoservices.sanginfo.sampleapp.utils.Util
import com.webgeoservices.multisearch.searchdatamodels.AutocompleteResponseItem
import com.webgeoservices.sanginfo.sampleapp.R

class SearchAdapter(
    var searchList: ArrayList<AutocompleteResponseItem>,
    var recyclerItemClickListener: RecyclerItemClickListener
) : RecyclerView.Adapter<SearchAdapter.ItemHolder>() {

    private val TAG = "SearchAdapter";

    class ItemHolder(v: View) : RecyclerView.ViewHolder(v) {
        private val search: TextView = v.findViewById(R.id.search_text)
        private val main: ConstraintLayout = v.findViewById(R.id.main)
        fun bind(
            data: AutocompleteResponseItem,
            recyclerItemClickListener: RecyclerItemClickListener
        ) {
            search.text = Util.highlightMatchSubString(data.description, data.matchedSubStrings)
            main.tag = data
            main.setOnClickListener {
                recyclerItemClickListener.onItemClickListener(it.tag as AutocompleteResponseItem)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.search_item, parent, false)
        return ItemHolder(view)
    }

    override fun onBindViewHolder(holder: ItemHolder, position: Int) {
        try {
            return holder.bind(searchList[position], recyclerItemClickListener)
        } catch (ex: Exception) {
            Log.e(TAG, ex.message!!)
        }

    }


    override fun getItemCount(): Int {
        return searchList.size
    }

    fun updateSearchList(searchResult: ArrayList<AutocompleteResponseItem>?) {
        if (searchResult != null) {
            searchList = searchResult
            notifyDataSetChanged()
        }
    }
}