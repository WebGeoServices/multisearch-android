package com.webgeoservices.multisearch.providers;

import android.util.Log;

import com.webgeoservices.multisearch.SearchProviderType;
import com.webgeoservices.multisearch.WoosmapException;
import com.webgeoservices.multisearch.configs.ProviderConfig;
import com.webgeoservices.multisearch.models.SearchApis;
import com.webgeoservices.multisearch.models.SearchRetrofitClient;
import com.webgeoservices.multisearch.searchdatamodels.DetailsResponseItem;
import com.webgeoservices.multisearch.utils.SearchUtil;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Response;

/**
 * Implements <code>autocomplete</code> and <code>details</code> API of Store provider
 */
public class StoreProvider  extends AbstractProvider {

    /***
     * Calls Store <code>autocomplete</code> API
     * @param searchString - the search string
     * @return Returns a list of JSONObjects
     * @throws WoosmapException - Throws an exception if any was raised
     */
    @Override
    public List<JSONObject> search(String searchString) throws WoosmapException {
        SearchApis apis;
        try {
            if (getProviderConfig() == null) {
                Log.e("StoreProvider", "You have not initialized Store provider");
                return null;
            }
            if (getProviderConfig().getKey().isEmpty()) {
                Log.e("StoreProvider", "Woosemap key can not be null");
                return null;
            }
            cancelPreviousApiCall();
            apis = SearchRetrofitClient.getClient().create(SearchApis.class);
            HashMap<String,String> params=new HashMap<>();
            if(getProviderConfig().getConfigParams().getQuery()!=null&&getProviderConfig().getConfigParams().getQuery().length>0){
                params.put("query",SearchUtil.getStoreQueryParamWithSearchString(getProviderConfig().getConfigParams().getQuery(),searchString));
            }else {
                params.put("query",SearchUtil.getStoreApiParameter(searchString));
            }
            HashMap<String,String>extraParams=SearchUtil.getApiQueryParameters(getProviderConfig());
            if(!extraParams.isEmpty()){
                params.putAll(extraParams);
            }
            call = apis.getStore((getProviderConfig().getKey()), params);
            if (this.previousApiURl.equals(call.request().url().toString())) {
                return this.previousApiResult;
            }
            Response<ResponseBody> response= call.execute();
            if(response.isSuccessful()){
                List<JSONObject>apiResult=new ArrayList<>();
                assert response.body() != null;
                JSONObject object;
                object = new JSONObject(response.body().string());
                JSONArray addressArray=object.getJSONArray("predictions");
                for (int i=0;i<addressArray.length();i++){
                    //Response does not have key named `description` which is needed for Fuse JS for scoring. Adding a new key `description` and assign the value from `name` key.
                    addressArray.getJSONObject(i).put("description",addressArray.getJSONObject(i).getString("name"));
                    apiResult.add(addressArray.getJSONObject(i));
                }
                this.previousApiResult=apiResult;
                this.previousApiURl=call.request().url().toString();
                return apiResult;
            }else {
                assert response.errorBody() != null;
                JSONObject errorObject;
                errorObject = new JSONObject(response.errorBody().string());
                if (errorObject.has("detail")){
                    throw new WoosmapException(errorObject.getString("detail"));
                }
                if (errorObject.has("value")){
                    throw new WoosmapException(errorObject.getString("value"));
                }
                throw new WoosmapException("Internal error, please try again later.");
            }
        }catch (Exception exception){
            if (!call.isCanceled()){
                throw new WoosmapException(exception.getMessage());
            }
        }
        return new ArrayList<>();
    }

    /***
     * Calls Store Details API
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
            HashMap<String,String> params=new HashMap<>();
            if(getProviderConfig().getConfigParams().getQuery()!=null&&getProviderConfig().getConfigParams().getQuery().length>0){
                params.put("query",SearchUtil.getDetailsStoreQueryParamWithSearchString(getProviderConfig().getConfigParams().getQuery(),id));
            }else {
                params.put("query",SearchUtil.getStoreDetailIDApiParameter(id));
            }
            HashMap<String,String>extraParams=SearchUtil.getApiQueryParameters(getProviderConfig());
            if(!extraParams.isEmpty()){
                params.putAll(extraParams);
            }
            call=apis.getStoreDetails(getProviderConfig().getKey(),params);
            Response<ResponseBody> response= call.execute();
            if (response.isSuccessful()){
                assert response.body() != null;
                JSONObject object;
                object = new JSONObject(response.body().string());
                if (!object.has("error_message")){
                    if (object.getJSONArray("features").length()>0){
                        detailsResponseItem=DetailsResponseItem.fromJSON(object.getJSONArray("features").getJSONObject(0)
                                , SearchProviderType.STORE,id);
                        return detailsResponseItem;
                    }
                    throw new WoosmapException(object.getString("ZERO RESULTS"));
                }
                throw new WoosmapException(object.getString("error_message"));
            }else {
                assert response.errorBody() != null;
                JSONObject errorObject;
                errorObject = new JSONObject(response.errorBody().string());
                if (errorObject.has("detail")){
                    throw new WoosmapException(errorObject.getString("detail"));
                }
                if (errorObject.has("value")){
                    throw new WoosmapException(errorObject.getString("value"));
                }
                throw new WoosmapException("Internal error, please try again later.");
            }
        }catch (Exception ex){
            throw new WoosmapException(ex.getMessage());
        }
    }

    /**
     * The constructor
     * @param providerConfig
     */
    public StoreProvider (ProviderConfig providerConfig){
        setProviderConfig(providerConfig);
    }

}
