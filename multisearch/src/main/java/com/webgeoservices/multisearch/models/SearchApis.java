package com.webgeoservices.multisearch.models;

import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;
import retrofit2.http.QueryMap;
import retrofit2.http.Url;

public interface SearchApis {

    @GET("localities/autocomplete?")
    Call<ResponseBody> getLocality(@Query(value = "private_key") String key,
                                   @QueryMap Map<String, String> queryParameters);

    @GET("address/autocomplete/json?")
    Call<ResponseBody> getAddress(@Query(value = "private_key") String key,
                                  @QueryMap Map<String, String> queryParameters);

    @GET("stores/autocomplete?")
    Call<ResponseBody> getStore(@Query(value = "private_key") String key,
                                @QueryMap Map<String, String> queryParameters);
    @GET()
    Call<ResponseBody> getPlaces(@Url String googlePlaceApi);

    @GET("localities/details?")
    Call<ResponseBody> getLocalityDetails(@Query(value = "private_key") String key,
                                          @QueryMap Map<String, String> queryParameters);

    @GET("address/details/json?")
    Call<ResponseBody> getAddressDetails(@Query(value = "private_key") String key,
                                         @QueryMap Map<String, String> queryParameters);

    @GET("stores/search/?")
    Call<ResponseBody> getStoreDetails(@Query(value = "private_key") String key,
                                       @QueryMap Map<String, String> queryParameters);
    @GET()
    Call<ResponseBody> getPlaceDetails(@Url String placeDetailApi);
}
