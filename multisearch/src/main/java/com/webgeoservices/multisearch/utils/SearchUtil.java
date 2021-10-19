package com.webgeoservices.multisearch.utils;

import android.text.TextUtils;

import com.webgeoservices.multisearch.SearchProviderType;
import com.webgeoservices.multisearch.configs.ConfigParams;
import com.webgeoservices.multisearch.configs.ProviderConfig;

import java.util.HashMap;

/***
 * Utility class to generate request URLs
 */
public class SearchUtil {

    public static final String googleApi="https://maps.googleapis.com/maps/api/place/autocomplete/json?";
    public static final String googleDetailsApi="https://maps.googleapis.com/maps/api/place/details/json?";
    public static final String[] ADDRESS_TYPES={"locality","street_number","country","route","postal_code","postal_codes"};

    public static boolean isNullEmpty(String str) {
        // check if string is null
        if (str == null) {
            return true;
        }
        // check if string is empty
        return str.trim().isEmpty();
    }

    public static String getCountryApiParameters(String[]stringArray){
        if(stringArray!=null&&stringArray.length>0){
            return "country:"+TextUtils.join("|country:",stringArray);
        }
        return "";
    }
    public static String getRegionApiParameters(String[] stringArray) {
        if(stringArray!=null&&stringArray.length>0){
            return TextUtils.join("|",stringArray);
        }
        return "";
    }

    public static String getStoreApiParameter(String queryString) {
        return "localized:"+"\""+queryString+"\"";
    }
    public static String getStoreQueryParamWithSearchString(String[]params,String searchString){
        StringBuilder query=new StringBuilder();
        query.append(getRegionApiParameters(params)).append("|").append(getStoreApiParameter(searchString));
        return query.toString();
    }
    public static String getDetailsStoreQueryParamWithSearchString(String[]params,String id){
        StringBuilder query=new StringBuilder();
        query.append(getRegionApiParameters(params)).append("|").append(getStoreDetailIDApiParameter(id));
        return query.toString();
    }

    public static String getStoreDetailIDApiParameter(String id) {
        return "idstore:"+"\""+id+"\"";
    }
    public static String getComponentsLanguageApiParams(String language){
        StringBuilder componentLan=new StringBuilder("language");
        componentLan.append(":").append(language);
        return componentLan.toString();
    }

    public static HashMap<String,String> getApiQueryParameters(ProviderConfig providerConfig){

        HashMap<String,String>queryParams=new HashMap<>();
        if(providerConfig.getConfigParams()!=null){
            ConfigParams configParams=providerConfig.getConfigParams();
            if(configParams.getCountries()!=null&&configParams.getCountries().length>0){
                queryParams.put("components",SearchUtil.getCountryApiParameters(providerConfig.getConfigParams().getCountries()));
            }
            if(configParams.getComponent()!=null&&configParams.getComponent().getLanguage()!=null&&!configParams.getComponent().getLanguage().trim().isEmpty()){
                queryParams.put(getComponentsLanguageApiParams(configParams.getComponent().getLanguage().trim()),"");
            }
            if(configParams.getSearchType()!=null&&configParams.getSearchType().length>0){
                queryParams.put("types",SearchUtil.getRegionApiParameters(providerConfig.getConfigParams().getSearchType()));
            }
            if(configParams.getLanguage()!=null&&!configParams.getLanguage().trim().isEmpty()){
                queryParams.put("language",providerConfig.getConfigParams().getLanguage());
            }
            if(providerConfig.getType().equals(SearchProviderType.ADDRESS)||providerConfig.getType().equals(SearchProviderType.LOCALITIES)){
                if(configParams.getQuery()!=null&&configParams.getQuery().length>0){
                    queryParams.put("query",getRegionApiParameters(configParams.getQuery()));

                }
            }
            if(providerConfig.getConfigParams().getData()!=null){
                queryParams.put("data",providerConfig.getConfigParams().getData().toString().toLowerCase());
            }
            if (!providerConfig.getConfigParams().getExtended().equalsIgnoreCase("")){
                queryParams.put("extended",providerConfig.getConfigParams().getExtended());
            }
        }
        return queryParams;
    }
}
