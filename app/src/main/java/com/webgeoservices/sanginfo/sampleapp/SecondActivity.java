package com.webgeoservices.sanginfo.sampleapp;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.webgeoservices.multisearch.MultiSearch;
import com.webgeoservices.multisearch.SearchProviderType;
import com.webgeoservices.multisearch.WoosmapException;
import com.webgeoservices.multisearch.configs.Component;
import com.webgeoservices.multisearch.configs.ProviderConfig;
import com.webgeoservices.multisearch.listeners.MultiSearchListener;
import com.webgeoservices.multisearch.searchdatamodels.AutocompleteResponseItem;
import com.webgeoservices.multisearch.searchdatamodels.DetailsResponseItem;

import java.util.List;

public class SecondActivity extends AppCompatActivity {
    EditText editText;
    TextView textView;
    MultiSearch multiSearch;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.second_layout);
        initControls();
        initMultiSearch();
    }

    private void initControls(){
        editText = findViewById(R.id.search_text);
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                multiSearch.autocompleteMulti(s.toString());
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        textView = findViewById(R.id.result);
    }

    private void initMultiSearch(){
        ProviderConfig.Builder builder;
        multiSearch = new MultiSearch(getApplicationContext());
        multiSearch.setDebounceTime(100);

        builder = new ProviderConfig.Builder(SearchProviderType.LOCALITIES)
                .key(getString(R.string.woosmap_private_key))
                .minInputLength(1)
                .component(new Component(new String[]{"FR"}))
                .searchType("locality")
                .searchType("country")
                .searchType("postal_code");
        multiSearch.addProvider(builder.build());//Add locality provider

        builder = new ProviderConfig.Builder(SearchProviderType.STORE)
                .key(getString(R.string.woosmap_private_key))
                .ignoreFallbackBreakpoint(true);
        multiSearch.addProvider(builder.build());//Add Store provider

        builder = new ProviderConfig.Builder(SearchProviderType.ADDRESS)
                .key(getString(R.string.woosmap_private_key))
                .fallbackBreakpoint(0.8f)
                .minInputLength(1)
                .component(new Component(new String[]{"FR"},"fr"));
        multiSearch.addProvider(builder.build());//Add Address provider

        builder = new ProviderConfig.Builder(SearchProviderType.PLACES)
                .key(getString(R.string.places_key))
                .fallbackBreakpoint(0.7f)
                .minInputLength(1)
                .component(new Component(new String[]{"fr"}))
                .language("it");
        multiSearch.addProvider(builder.build());//Add places provider


        multiSearch.addSearchListener(new MultiSearchListener() {
            @Override
            public void onSearchComplete(List<AutocompleteResponseItem> searchResult, WoosmapException exception) {
                if (exception==null){
                    if (searchResult.size()==0){
                        textView.setText("No results found");
                        return;
                    }
                    StringBuilder stringBuilder = new StringBuilder();
                    Log.d(SecondActivity.class.getName(),searchResult.toString());
                    for(AutocompleteResponseItem autocompleteResponseItem: searchResult){
                        stringBuilder.append(autocompleteResponseItem.getDescription() + "\n");
                    }
                    textView.setText(stringBuilder.toString());
                }
                else{
                    textView.setText(exception.getMessage());
                }
                multiSearch.detailsMulti(searchResult.get(0).getId(),searchResult.get(0).getApi());
            }

            @Override
            public void onDetailComplete(DetailsResponseItem detailResult, WoosmapException exception) {
                if (exception==null){
                    Log.d(SecondActivity.class.getName(),detailResult.toString());
                }
            }
        });
        multiSearch.autocompleteMulti("she");

    }
}
