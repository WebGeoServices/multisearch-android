package com.webgeoservices.multisearch.providers;

import android.util.Log;

import com.webgeoservices.multisearch.SearchProviderType;
import com.webgeoservices.multisearch.WoosmapException;
import com.webgeoservices.multisearch.configs.ConfigParams;
import com.webgeoservices.multisearch.configs.ProviderConfig;
import com.webgeoservices.multisearch.models.SearchApis;
import com.webgeoservices.multisearch.models.SearchRetrofitClient;
import com.webgeoservices.multisearch.utils.SearchUtil;
import com.webgeoservices.multisearch.searchdatamodels.DetailsResponseItem;

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Response;

/**
 * Implements <code>autocomplete</code> and <code>details</code> API of Google Places provider
 */
public class PlacesProvider extends AbstractProvider {
    private final long sessionToken;

    /***
     * Calls Google Places <code>autocomplete</code> API
     * @param searchString - the search string
     * @return Returns a list of JSONObjects
     * @throws WoosmapException - Throws an exception if any was raised
     */
    @Override
    public List<JSONObject> search(String searchString)throws WoosmapException {
        SearchApis apis;
        try {
            if (getProviderConfig() == null) {
                Log.e("PlacesProvider", "You have not initialized Place provider");
                return null;
            }
            if (getProviderConfig().getKey().isEmpty()) {
                Log.e("PlacesProvider", "Woosemap key can not be null");
                return null;
            }
            cancelPreviousApiCall();
            String url=SearchUtil.googleApi+"input="+ URLEncoder.encode(searchString,"UTF-8")+"&key="+getProviderConfig().getKey();
            url=getParameterAddedString(url,getProviderConfig(),false);
            apis = SearchRetrofitClient.getClient().create(SearchApis.class);
            call = apis.getPlaces(url);
            if (this.previousApiURl.equals(call.request().url().toString())) {
                return this.previousApiResult;
            }
            Response<ResponseBody> response= call.execute();
            if(response.isSuccessful()){
                List<JSONObject>apiResult=new ArrayList<>();
                assert response.body() != null;
                JSONObject object=new JSONObject(response.body().string());
                if (object.getString("status").equalsIgnoreCase("OK") || object.getString("status").equalsIgnoreCase("ZERO_RESULTS")){
                    JSONArray addressArray=object.getJSONArray("predictions");
                    for (int i=0;i<addressArray.length();i++){
                        apiResult.add(addressArray.getJSONObject(i));
                    }
                    this.previousApiResult=apiResult;
                    this.previousApiURl=call.request().url().toString();
                    return apiResult;
                } else{
                    if (object.has("error_message")){
                        throw new WoosmapException(object.getString("error_message"));
                    }
                    else{
                        throw new WoosmapException(object.getString("status"));
                    }
                }
            }else {
                throw new WoosmapException("Error " + response.code());
            }
        }catch (Exception exception){
            if (!call.isCanceled()){
                throw new WoosmapException(exception.getMessage());
            }
        }
        return new ArrayList<>();
    }

    /***
     * Calls Google Place Details API
     * @param id - the place id of the place that needs to be fetched
     * @return Returns an object of <code>DetailsResponseItem</code> containing details
     * @throws WoosmapException - Throws an exception if any was raised
     */
    @Override
    public DetailsResponseItem details(String id) throws WoosmapException {
        SearchApis apis;
        DetailsResponseItem detailsResponseItem;
        try {
            cancelPreviousApiCall();
            apis = SearchRetrofitClient.getClient().create(SearchApis.class);
            String detailsUrl=SearchUtil.googleDetailsApi+"key="+getProviderConfig().getKey()+"&place_id="+id;
            detailsUrl=getParameterAddedString(detailsUrl,getProviderConfig(),true);
            call=apis.getPlaceDetails(detailsUrl);
            Response<ResponseBody> response= call.execute();
            if (response.isSuccessful()){
                assert response.body() != null;
                JSONObject object=new JSONObject(response.body().string());
                if (object.getString("status").equalsIgnoreCase("OK")){
                    detailsResponseItem=DetailsResponseItem.fromJSON(object.getJSONObject("result")
                            , SearchProviderType.PLACES,id);

                    return detailsResponseItem;
                }
                else{
                    if (object.has("error_message")){
                        throw new WoosmapException(object.getString("error_message"));
                    }
                    if (object.has("status")){
                        throw new WoosmapException(object.getString("status"));
                    }
                    throw new WoosmapException("Internal error, please try again later.");
                }
            }else {
                assert response.errorBody() != null;
                JSONObject errorObject=new JSONObject(response.errorBody().string());
                throw new WoosmapException(errorObject.getString("value"));
            }
        }catch (Exception ex){
            throw new WoosmapException(ex.getMessage());
        }
    }

    /**
     * The constructor
     * @param providerConfig
     */
    public PlacesProvider(ProviderConfig providerConfig){
        setProviderConfig(providerConfig);
        sessionToken=System.currentTimeMillis();
    }

    /***
     * Create the a request URL based on the configuration options
     * @param url - Base URL
     * @param providerConfig - Configuration options of the provider
     * @param isForDetailApi - Checking Which api being called.
     * @return - Formated request URL
     */
    private String getParameterAddedString(String url, ProviderConfig providerConfig, boolean isForDetailApi) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(url);
        if (providerConfig != null) {
            ConfigParams configParams = providerConfig.getConfigParams();
            if (configParams.getCountries() != null && configParams.getCountries().length > 0) {
                stringBuilder.append("&components=").append(SearchUtil.getCountryApiParameters(configParams.getCountries()));

            }
            if (configParams.getSearchType() != null && configParams.getSearchType().length > 0) {
                stringBuilder.append("&types=(regions)");
            }
            if (configParams.getLanguage() != null && !configParams.getLanguage().trim().isEmpty()) {
                stringBuilder.append("&language=").append(configParams.getLanguage());
            }
            if (configParams.getData() != null) {
                stringBuilder.append("&data=").append(configParams.getData().toString().toLowerCase());
            }
            if (!configParams.getExtended().equalsIgnoreCase("")) {
                stringBuilder.append("&extended=").append(configParams.getExtended());
            }
            if (!configParams.getFields().equalsIgnoreCase("")) {
                stringBuilder.append("&fields=").append(configParams.getFields());
            }
        }
        if (isForDetailApi) {
            stringBuilder.append("&fields=address_component,adr_address,formatted_address,geometry,icon,name,place_id,type,url,vicinity");
        }
        stringBuilder.append("&sessiontoken=").append(sessionToken);
        return stringBuilder.toString();

    }
}
