package com.webgeoservices.multisearch.providers;

import android.util.Base64;
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

import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Response;

/***
 * Implements <code>autocomplete</code> and <code>details</code> API of Address provider
 */
public class AddressProvider extends AbstractProvider {

    /***
     * Calls Address <code>autocomplete</code> API
     * @param searchString - the search string
     * @return Returns a list of JSONObjects
     * @throws WoosmapException - Throws an exception if any was raised
     */
    @Override
    public List<JSONObject> search(String searchString) throws WoosmapException{
        SearchApis apis;
        try {
            if (getProviderConfig() == null) {
                Log.e(AddressProvider.class.getName(), "You have not initialized Address provider");
                return null;
            }
            if (getProviderConfig().getKey().isEmpty()) {
                Log.e(AddressProvider.class.getName(), "Key can not be null");
                return null;
            }
            cancelPreviousApiCall();
            apis = SearchRetrofitClient.getClient().create(SearchApis.class);
            HashMap<String,String> params=new HashMap<>();
            params.put("input",searchString);
            HashMap<String,String>extraParams=SearchUtil.getApiQueryParameters(getProviderConfig());
            if(!extraParams.isEmpty()){
                params.putAll(extraParams);
            }
            params.put("cc_format","alpha2");
            call=apis.getAddress(getProviderConfig().getKey(),params);
            if (this.previousApiURl.equals(call.request().url().toString())) {
                return this.previousApiResult;
            }
            Response<ResponseBody> response= call.execute();
            if(response.isSuccessful()){
                List<JSONObject>apiResult=new ArrayList<>();
                assert response.body() != null;
                JSONObject object=new JSONObject(response.body().string());
                if (!object.has("error_message")){
                    JSONArray addressArray=object.getJSONArray("predictions");
                    for (int i=0;i<addressArray.length();i++){
                        apiResult.add(addressArray.getJSONObject(i));
                    }
                    this.previousApiResult=apiResult;
                    this.previousApiURl=call.request().url().toString();
                    return apiResult;

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
        }catch (Exception exception){
            if (!call.isCanceled()){
                throw new WoosmapException(exception.getMessage());
            }
        }
        return new ArrayList<>();
    }

    /***
     * Calls details API of the Address provider
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
            params.put("address", URLDecoder.decode(new String(Base64.decode(id,Base64.NO_WRAP|Base64.URL_SAFE)),"UTF-8"));
            HashMap<String,String>extraParams=SearchUtil.getApiQueryParameters(getProviderConfig());
            if(!extraParams.isEmpty()){
                params.putAll(extraParams);
            }
            params.put("cc_format","alpha2");
            call=apis.getAddressDetails(getProviderConfig().getKey(),params);
            Response<ResponseBody> response= call.execute();
            if (response.isSuccessful()){
                assert response.body() != null;
                JSONObject object;
                object = new JSONObject(response.body().string());
                if (!object.has("error_message")){
                    if (object.getJSONArray("results").length()>0){
                        detailsResponseItem=DetailsResponseItem.fromJSON(object.getJSONArray("results").getJSONObject(0)
                                , SearchProviderType.ADDRESS,id);
                        return detailsResponseItem;
                    }
                    throw new WoosmapException(object.getString("ZERO RESULTS"));
                }
                throw new WoosmapException(object.getString("error_message"));
            }else {
                assert response.errorBody() != null;
                JSONObject errorObject;
                errorObject = new JSONObject(response.errorBody().string());
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
    public AddressProvider(ProviderConfig providerConfig){
        setProviderConfig(providerConfig);
    }

}
