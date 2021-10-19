package com.webgeoservices.multisearch.providers;

import android.util.Log;

import org.json.JSONObject;

import com.webgeoservices.multisearch.WoosmapException;
import com.webgeoservices.multisearch.configs.ProviderConfig;
import com.webgeoservices.multisearch.searchdatamodels.DetailsResponseItem;


import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;

/***
 * An abstract class which layouts the implementation framework for different provider classes. 
 * Known subclasses are <code>AddressProvider</code>, <code>LocalitiesProvider</code>, <code>StoreProvider</code> and <code>PlacesProvider</code>
 */
public abstract class AbstractProvider {
    private ProviderConfig providerConfig;
    protected Call<ResponseBody> call;
    protected List<JSONObject> previousApiResult;
    protected String previousApiURl="";

    /***
     * Calls autocomplete API of the underlying Search provider
     * @param searchString - the search string
     * @return Returns a list of JSONObjects
     * @throws WoosmapException - Throws an exception if any was raised
     */
    public abstract List<JSONObject> search(String searchString) throws WoosmapException;

    /***
     * Calls details API of the underlying Search provider
     * @param id - the place id of the place that needs to be fetched
     * @return Returns an object of <code>DetailsResponseItem</code> containing details
     * @throws WoosmapException - Throws an exception if any was raised
     */
    public abstract DetailsResponseItem details(String id) throws WoosmapException;

    /***
     * Returns the configuration of the underlying search provider
     * @return
     */
    public ProviderConfig getProviderConfig(){
        return providerConfig;
    }

    /***
     * Sets the configuration of the underlying search provider
     * @param providerConfig - the provider configuration
     */
    protected void setProviderConfig(ProviderConfig providerConfig){
        this.providerConfig = providerConfig;
    }

    /***
     * Cancels out previous API call
     */
    protected void cancelPreviousApiCall() {
        try {
            if (call != null) {
                call.cancel();
            }
        }
        catch (Exception ex){
            Log.e(AbstractProvider.class.getName(),ex.toString());
        }

    }

}
