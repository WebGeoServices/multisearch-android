package com.webgeoservices.sanginfo.sampleapp.listeners

import com.webgeoservices.multisearch.searchdatamodels.AutocompleteResponseItem

interface RecyclerItemClickListener {
    fun onItemClickListener(autocompleteResponseItem: AutocompleteResponseItem?)
}