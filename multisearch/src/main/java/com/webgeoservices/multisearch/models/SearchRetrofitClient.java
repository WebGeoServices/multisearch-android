package com.webgeoservices.multisearch.models;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 *
 */
public class SearchRetrofitClient {
    private static Retrofit searchRetrofit = null;
    private static final String baseURL="https://api.woosmap.com/";

    /***
     *
     * @return
     */
    public static Retrofit getClient() {
        if (searchRetrofit==null) {
            searchRetrofit = new Retrofit.Builder()
                    .baseUrl(baseURL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return searchRetrofit;

    }
}
