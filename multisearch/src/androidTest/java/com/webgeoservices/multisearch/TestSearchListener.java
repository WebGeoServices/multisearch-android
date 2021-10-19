package com.webgeoservices.multisearch;

import com.webgeoservices.multisearch.listeners.MultiSearchListener;
import com.webgeoservices.multisearch.searchdatamodels.AutocompleteResponseItem;
import com.webgeoservices.multisearch.searchdatamodels.DetailsResponseItem;

import java.util.ArrayList;
import java.util.List;

public class TestSearchListener implements MultiSearchListener {
    public List<AutocompleteResponseItem> items = new ArrayList<>();
    public WoosmapException exception;
    public DetailsResponseItem detailsResponseItem;

    @Override
    public void onSearchComplete(List<AutocompleteResponseItem> searchResult, WoosmapException exception) {
        if (exception==null){
            items.addAll(searchResult);
        }
        else{
            this.exception=exception;
        }
        synchronized (this){
            notifyAll();
        }
    }

    @Override
    public void onDetailComplete(DetailsResponseItem detailResult, WoosmapException exception) {
        if (exception==null){
            detailsResponseItem = detailResult;
        }
        else{
            this.exception=exception;
        }
        synchronized (this){
            notifyAll();
        }
    }
}