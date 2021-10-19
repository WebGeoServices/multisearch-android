package com.webgeoservices.sanginfo.sampleapp.viewModels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.webgeoservices.multisearch.MultiSearch
import com.webgeoservices.multisearch.SearchProviderType
import com.webgeoservices.multisearch.WoosmapException
import com.webgeoservices.multisearch.configs.Component
import com.webgeoservices.multisearch.configs.ProviderConfig
import com.webgeoservices.multisearch.listeners.MultiSearchListener
import com.webgeoservices.multisearch.searchdatamodels.AutocompleteResponseItem
import com.webgeoservices.multisearch.searchdatamodels.DetailsResponseItem
import com.webgeoservices.sanginfo.sampleapp.R


class AutocompleteViewModel(application: Application) : AndroidViewModel(application),
    MultiSearchListener {
    val autocompleteResult: MutableLiveData<ArrayList<AutocompleteResponseItem>> = MutableLiveData()
    val detailsResult: MutableLiveData<DetailsResponseItem> = MutableLiveData()
    private val context = getApplication<Application>().applicationContext
    private val multiSearch: MultiSearch
    val searchApiError: MutableLiveData<String> = MutableLiveData()
    val detailSearchApiError: MutableLiveData<String> = MutableLiveData()

    init {
        multiSearch = MultiSearch(context)
        multiSearch.addSearchListener(this)
    }

    override fun onSearchComplete(
        searchResult: MutableList<AutocompleteResponseItem>?,
        exception: WoosmapException?
    ) {
        if (exception == null) {
            autocompleteResult.postValue(searchResult as ArrayList<AutocompleteResponseItem>?)
        } else {
            searchApiError.postValue(exception.message)
        }

    }

    override fun onDetailComplete(
        detailResult: DetailsResponseItem?,
        exception: WoosmapException?
    ) {
        if (exception == null) {
            detailsResult.postValue(detailResult as DetailsResponseItem)
        } else {
            detailSearchApiError.postValue(exception.message)
        }
    }

    fun autocomplete(searchString: String) {
        multiSearch.autocompleteMulti(searchString)
    }

    fun details(autocompleteResponseItem: AutocompleteResponseItem) {
        multiSearch.detailsMulti(autocompleteResponseItem)
    }


    fun initializeProviders() {
        multiSearch.debounceTime = 0
        var builder = ProviderConfig.Builder(SearchProviderType.LOCALITIES)
                .key(context.getString(R.string.woosmap_private_key))
                .minInputLength(1)
                .component(Component(arrayOf("FR")))
                .searchType("locality")
                .searchType("country")
                .searchType("postal_code")
        multiSearch.addProvider(builder.build())//Add locality provider

        builder = ProviderConfig.Builder(SearchProviderType.STORE)
                .key(context.getString(R.string.woosmap_private_key))
                .ignoreFallbackBreakpoint(true)
                .minInputLength(1)
        multiSearch.addProvider(builder.build()) //Add store provider

        builder = ProviderConfig.Builder(SearchProviderType.ADDRESS)
                .key(context.getString(R.string.woosmap_private_key))
                .fallbackBreakpoint(0.8f)
                .minInputLength(1)
                .component(Component(arrayOf("FR")))
                .language("fr")
        multiSearch.addProvider(builder.build()) //Add Address provider

        builder = ProviderConfig.Builder(SearchProviderType.PLACES)
                .key(context.getString(R.string.places_key))
                .fallbackBreakpoint(0.7f)
                .minInputLength(1)
                .component(Component(arrayOf("FR")))
                .language("it")
        multiSearch.addProvider(builder.build())

    }
}