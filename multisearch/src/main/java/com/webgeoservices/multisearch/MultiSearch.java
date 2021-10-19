package com.webgeoservices.multisearch;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;

import com.webgeoservices.multisearch.configs.ProviderConfig;
import com.webgeoservices.multisearch.interfaces.Scorable;
import com.webgeoservices.multisearch.listeners.MultiSearchListener;
import com.webgeoservices.multisearch.providers.AbstractProvider;
import com.webgeoservices.multisearch.providers.AddressProvider;
import com.webgeoservices.multisearch.providers.LocalitiesProvider;
import com.webgeoservices.multisearch.providers.PlacesProvider;
import com.webgeoservices.multisearch.providers.StoreProvider;
import com.webgeoservices.multisearch.scorers.FuseScorer;
import com.webgeoservices.multisearch.searchdatamodels.AutocompleteResponseItem;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;

/***
 * MultiSearch class which exposes <code>autocomplete</code> and <code>details</code> methods
 */
public class MultiSearch {
    private LinkedHashMap<SearchProviderType, AbstractProvider> providers = new LinkedHashMap<>();
    private MultiSearchListener listener;
    private long debounceTime=0;
    private Scorable scorable;
    private Context context;
    private CompletableFuture completableFuture;
    private String lastSearchString="";
    private SearchProviderType lastApiCalled=null;
    private int lastResultCount=-1;
    private String searchString;
    private Handler handler;
    private Runnable runnable;


    /***
     * Default constructor
     * @param context the context
     */
    public MultiSearch(Context context){
        init(context,0);
    }

    /***
     * The constructor
     * @param debounceTime The amount of time in ms that the autocomplete function will wait after the last received call before executing the next one
     * @param context the context
     */
    public MultiSearch(Context context,long debounceTime){
        init(context,debounceTime);
    }

    /***
     * Initializes private members
     * @param context - the context
     * @param debounceTime - the debounce time
     */
    private void init(Context context,long debounceTime){
        if (context == null){
            throw new IllegalStateException("Context cannot be null");
        }
        this.context = context;
        this.debounceTime = debounceTime;
        initializeScorer();
        handler = new Handler(Looper.getMainLooper());
    }


    /***
     * Returns debounce time
     * @return Long
     */
    public long getDebounceTime() {
        return debounceTime;
    }

    /***
     * Sets the debounce time
     * @param debounceTime The amount of time in ms that the autocomplete function will wait after the last received call before executing the next one
     */
    public void setDebounceTime(long debounceTime) {
        this.debounceTime = debounceTime;
    }

    /***
     * Initializes scorer object which scores and sorts the results returned by APIs
     */
    private void initializeScorer(){
        JSONObject fuseConfiguration = new JSONObject();
        JSONArray keys = new JSONArray();
        try{
            keys.put("description");

            fuseConfiguration.put("includeScore",true);
            fuseConfiguration.put("findAllMatches",true);
            fuseConfiguration.put("ignoreLocation",true);
            fuseConfiguration.put("ignoreFieldNorm",true);
            fuseConfiguration.put("threshold",0.6);
            fuseConfiguration.put("keys",keys);
            scorable = FuseScorer.getInstance(context,fuseConfiguration);
        }
        catch (JSONException ex){
            Log.d(MultiSearch.class.getName(),ex.toString());
        }
    }

    /***
     * Adds a provider to providers collection. Only one type of provider can be added to the collection
     * APIs will be called in the order they were added to the collection
     * @param providerConfig Configuration of the provider
     */
    public void addProvider(@NonNull ProviderConfig providerConfig){
        if(providerConfig.getType().equals(SearchProviderType.LOCALITIES)){
            providers.put(SearchProviderType.LOCALITIES,new LocalitiesProvider(providerConfig));
        }else if(providerConfig.getType().equals(SearchProviderType.ADDRESS)){
            providers.put(SearchProviderType.ADDRESS,new AddressProvider(providerConfig));
        }else if(providerConfig.getType().equals(SearchProviderType.STORE)){
            providers.put(SearchProviderType.STORE,new StoreProvider(providerConfig));
        }else if(providerConfig.getType().equals(SearchProviderType.PLACES)){
            providers.put(SearchProviderType.PLACES,new PlacesProvider(providerConfig));
        }
    }

    /***
     * Removes the provider from the collection based on the provider type
     * @param apiType - Type of the provider which needs to be removed
     */
    public void removeProvider(SearchProviderType apiType){
        providers.remove(apiType);
    }

    /***
     * Removes all providers from the collection
     */
    public void removeAllProviders(){
        providers = new LinkedHashMap<>();
    }

    /***
     * Attaches <code>MultiSearchListener</code> callback
     * @param listener An object implementing <code>MultiSearchListener</code> callback interface
     */
    public void addSearchListener(MultiSearchListener listener){
        this.listener=listener;
    }

    /**
     * autocomplete API implementation of all the providers present in the collection
     */
    private void autocomplete(){
        if (isProviderConfigCollectionEmpty()){
            throw new IllegalStateException(context.getString(R.string.__wgs_no_config_provided_error));
        }
        //return if there is no input given
        if (searchString.trim().equalsIgnoreCase("")){
            if(listener!=null){
                listener.onSearchComplete(new ArrayList<>(),null);
            }
            invalidateLastAutocompleteValues();
            return;
        }

        //cancel the ongoing task
        try{
            if (completableFuture!=null){
                completableFuture.cancel(true);
            }
        }
        catch (Exception ex){
            Log.e(MultiSearch.class.getName(),ex.toString());
        }
        completableFuture = CompletableFuture.supplyAsync(()->{
            List<JSONObject> providerSearchResult; //Arraylist which will hold single providers result
            ArrayList<AutocompleteResponseItem> finalResult = new ArrayList<>(); //Arraylist which will hold final results
            int optionalProviderResultCount=0; //Variable which will hold count of filtered results which was produced by providers where fallbackBreakpoint != false
            boolean callOnlyNonFallbackApi = false; //Variable which specifies if only those APIs should be called who's shouldIgnoreFallbackBreakPoint() == true
            try{
                if (searchString.length() < lastSearchString.length()){
                    invalidateLastAutocompleteValues();
                }
                for (AbstractProvider provider :providers.values()){
                    boolean shouldApiBeCalled = true; //Variable which specifies if provider's api call should be made

                    //If provider's shouldIgnoreFallbackBreakPoint == false and only nonfallback API need to be called. then skip this provider
                    if (callOnlyNonFallbackApi && (provider.getProviderConfig().shouldIgnoreFallbackBreakPoint()==false)){
                        continue;
                    }

                    // Do not call api if the last call was done on another fallbacking API which is after, in the ordered list.
                    if (isNewInputIsContainedInLastInput(searchString)){
                        if (!isCurrentApiAfterTheLastCalledAPI(provider.getProviderConfig().getType()) && !provider.getProviderConfig().shouldIgnoreFallbackBreakPoint()){
                            continue;
                        }
                        //Check if last called API is the same as this API and if returned 0 result. If yes then skip this API.
                        else if (provider.getProviderConfig().getType() == lastApiCalled && lastResultCount==0){
                            continue;
                        }
                    }

                    //If search string's length < provider.getProviderConfig().getMinInputLength() then do not call the API
                    //And next APIs should be called only if provider.getProviderConfig().shouldIgnoreFallbackBreakPoint() == true
                    if (searchString.length() < provider.getProviderConfig().getMinInputLength()){
                        shouldApiBeCalled=false;
                        callOnlyNonFallbackApi = true;
                    }

                    //check if api should be called.
                    if (shouldApiBeCalled){
                        if (provider.getProviderConfig().shouldIgnoreFallbackBreakPoint()){//if Provider should ignore fallbackBreakpoint. Then add all the results without any checks and filter
                            providerSearchResult = fetchProviderSearch(provider,searchString);

                            setLastSearchValues(searchString,provider.getProviderConfig().getType(),providerSearchResult.size());
                            for(JSONObject result: providerSearchResult){
                                finalResult.add(AutocompleteResponseItem.fromJSON(result,provider.getProviderConfig().getType(),searchString));
                            }
                        }
                        else if (optionalProviderResultCount==0){ //Check if previous optional providers(i.e. ones with shouldIgnoreFallbackBreakPoint == false) produced any results. If not only then proceed with calling provider search method
                            providerSearchResult = fetchProviderSearch(provider,searchString);
                            providerSearchResult = scorable.scoreResults(providerSearchResult,searchString,provider.getProviderConfig().getFallbackBreakpoint()); // Score and sort the results

                            setLastSearchValues(searchString,provider.getProviderConfig().getType(),providerSearchResult.size());
                            for(JSONObject result: providerSearchResult){
                                try{
                                    if (result.getDouble("score")<=provider.getProviderConfig().getFallbackBreakpoint()){ //Filter the results based on fallbackbreakpoint
                                        finalResult.add(AutocompleteResponseItem.fromJSON(result,provider.getProviderConfig().getType(),searchString));
                                        optionalProviderResultCount++; //Increase this counter
                                    }
                                }
                                catch (JSONException ex){
                                    Log.e(MultiSearch.class.getName(),ex.toString());
                                }
                            }
                        }
                    }
                }
            }
            catch (Exception ex){
                throw new CompletionException(ex.getMessage(),ex.getCause());
            }
            return finalResult;
        }).whenComplete((result, exception) ->{
            if (listener!=null){
                if (exception!=null){
                    invalidateLastAutocompleteValues();
                    listener.onSearchComplete(null,new WoosmapException(exception.getMessage()));
                }
                else{
                    listener.onSearchComplete(result,null);
                }
            }
        });

    }

    /***
     * Check if the new input prefixes with the last input.
     * If yes that means this is could be a progressing search (user is typing)
     * @param input input that needs to be checked with last inp
     * @return
     */
    private boolean isNewInputIsContainedInLastInput(String input){
        if (lastSearchString.equalsIgnoreCase("")){
            return false;
        }
        if (input.length() < lastSearchString.length()){
            return false;
        }
        return input.startsWith(lastSearchString);
    }

    /***
     * Check if the passed API is called after last API called.
     * @param currentApi
     * @return
     */
    private boolean isCurrentApiAfterTheLastCalledAPI(SearchProviderType currentApi){
        if (lastApiCalled==null){
            return false;
        }
        int lastApiIndex;
        int currentApiIndex;
        List<SearchProviderType> providerTypes = new ArrayList<>(providers.keySet());
        lastApiIndex = providerTypes.indexOf(lastApiCalled);
        currentApiIndex = providerTypes.indexOf(currentApi);
        return currentApiIndex>=lastApiIndex;
    }

    /***
     *
     */
    private void setLastSearchValues(String searchString, SearchProviderType providerType,int resultCount){
        if (!providers.get(providerType).getProviderConfig().shouldIgnoreFallbackBreakPoint()){
            lastSearchString = searchString;
            lastApiCalled = providerType;
            lastResultCount=resultCount;
        }
    }

    /***
     * Invalidates last search params
     */
    private void invalidateLastAutocompleteValues(){
        lastSearchString = "";
        lastApiCalled = null;
        lastResultCount = -1;
    }

    /**
     * autocomplete API implementation of the specified provider
     * @param type Type of the provider which needs to invoke <code>autocomplete</code> API
     */
    private void autocomplete(SearchProviderType type){
        if (!isProviderConfigPresent(type)){
            throw new IllegalStateException(context.getString(R.string.__wgs_api_config_not_found_error));
        }

        //check if search string length is less than min input length
        if (providers.get(type).getProviderConfig().getMinInputLength()>0 && searchString.length() < providers.get(type).getProviderConfig().getMinInputLength()){
            if(listener!=null){
                listener.onSearchComplete(new ArrayList<>(),null);
            }
            return;
        }

        //return if there is no input given
        if (searchString.trim().equalsIgnoreCase("")){
            if(listener!=null){
                listener.onSearchComplete(new ArrayList<>(),null);
            }
            return;
        }

        //cancel the ongoing task
        try{
            if (completableFuture!=null){
                completableFuture.cancel(true);
            }
        }
        catch (Exception ex){
            Log.e(MultiSearch.class.getName(),ex.toString());
        }
        completableFuture = CompletableFuture.supplyAsync(() ->{
            List<JSONObject> providerSearchResult;
            ArrayList<AutocompleteResponseItem> finalResult = new ArrayList<>();

            try{
                AbstractProvider provider = providers.get(type);

                providerSearchResult=fetchProviderSearch(provider,searchString);
                providerSearchResult = scorable.scoreResults(providerSearchResult,searchString,provider.getProviderConfig().getFallbackBreakpoint());
                //Filtering and scoring logic needs to be implemented here
                for(JSONObject result: providerSearchResult){
                    try{
                        //Check if fallbackbreakpoint should be ignored. If yes then add the item without checking the score
                        if (provider.getProviderConfig().shouldIgnoreFallbackBreakPoint()){
                            finalResult.add(AutocompleteResponseItem.fromJSON(result,provider.getProviderConfig().getType(),searchString));
                        }//Else check if the score is <= provider's fallbackbreakpoint
                        else if (result.getDouble("score")<=provider.getProviderConfig().getFallbackBreakpoint()){
                            finalResult.add(AutocompleteResponseItem.fromJSON(result,provider.getProviderConfig().getType(),searchString));
                        }
                    }
                    catch (JSONException ex){
                        Log.e(MultiSearch.class.getName(),ex.toString());
                    }
                }
            }
            catch (Exception ex){
                throw new CompletionException(ex);
            }
            return  finalResult;
        }).whenComplete((result, exception) ->{
            if (listener!=null){
                if (exception!=null){
                    listener.onSearchComplete(null,new WoosmapException(exception.getMessage()));
                }
                else{
                    listener.onSearchComplete(result,null);
                }
            }
        });
    }

    private boolean isProviderConfigCollectionEmpty(){
        return providers.values().size() == 0;
    }

    private boolean isProviderConfigPresent(SearchProviderType apiType){
        return providers.containsKey(apiType);
    }

    /***
     * Query the different autocomplete APIs from the provider collection. 
     * Depending on the fallbackBreakpoint and the minInputLength defined for each API. 
     * If the first API does not send revelant enough results, 
     * it will query the next one, until results are relevant or no other API is in the list
     * @param searchString - the search string
     */
    public void autocompleteMulti(@NonNull String searchString){
        this.searchString = searchString.trim();
        if (this.debounceTime==0){
            autocomplete();
            return;
        }
        if (runnable!=null){
            handler.removeCallbacks(runnable);
        }
        runnable = new Runnable() {
            @Override
            public void run() {
                autocomplete();
            }
        };
        handler.postDelayed(runnable,this.debounceTime);
    }

    /***
     * Query the autocomplete Woosmap Address API
     * @param searchString - the search string
     */
    public void autocompleteAddress( @NonNull String searchString){
        this.searchString = searchString.trim();
        if (this.debounceTime==0){
            autocomplete(SearchProviderType.ADDRESS);
            return;
        }
        if (runnable!=null){
            handler.removeCallbacks(runnable);
        }
        runnable = new Runnable() {
            @Override
            public void run() {
                autocomplete(SearchProviderType.ADDRESS);
            }
        };
    }

    /***
     * Query the autocomplete Google Places API
     * @param searchString - the search string
     */
    public void autocompletePlaces(@NonNull String searchString){
        this.searchString = searchString.trim();
        if (this.debounceTime==0){
            autocomplete(SearchProviderType.PLACES);
            return;
        }
        if (runnable!=null){
            handler.removeCallbacks(runnable);
        }
        runnable = new Runnable() {
            @Override
            public void run() {
                autocomplete(SearchProviderType.PLACES);
            }
        };
    }

    /***
     * Query the autocomplete Woosmap Localities API
     * @param searchString - the search string
     */
    public void autocompleteLocalities(@NonNull String searchString){
        this.searchString = searchString.trim();
        if (this.debounceTime==0){
            autocomplete(SearchProviderType.LOCALITIES);
            return;
        }
        if (runnable!=null){
            handler.removeCallbacks(runnable);
        }
        runnable = new Runnable() {
            @Override
            public void run() {
                autocomplete(SearchProviderType.LOCALITIES);
            }
        };
    }

    /***
     * Query the autocomplete Woosmap Store API
     * @param searchString - the search string
     */
    public void autocompleteStore(@NonNull String searchString){
        this.searchString = searchString.trim();
        if (this.debounceTime==0){
            autocomplete(SearchProviderType.STORE);
            return;
        }
        if (runnable!=null){
            handler.removeCallbacks(runnable);
        }
        runnable = new Runnable() {
            @Override
            public void run() {
                autocomplete(SearchProviderType.STORE);
            }
        };
    }

    /***
     * Query the details api to get details of an item
     * @param autocompleteResponseItem - <code>AutocompleteResponseItem</code> object for whom the details need to be fetched
     */
    public void detailsMulti(AutocompleteResponseItem autocompleteResponseItem){
        detailsMulti(autocompleteResponseItem.getId(),autocompleteResponseItem.getApi());
    }

    /***
     * Query the details api to get details of an item
     * @param id - The public id of the place
     * @param apiType - Type of the provider from whome the details need to be fetched. i.e. <code>localities</code>, <code>store</code>, <code>address</code>, <code>places</code>
     */
    public void detailsMulti(String id, SearchProviderType apiType){
        if (!isProviderConfigPresent(apiType)){
            throw new IllegalStateException(context.getString(R.string.__wgs_api_config_not_found_error));
        }
        //cancel the ongoing task
        try{
            if (completableFuture!=null){
                completableFuture.cancel(true);
            }
        }
        catch (Exception ex){
            Log.e(MultiSearch.class.getName(),ex.toString());
        }

        completableFuture = CompletableFuture.supplyAsync(()->{
            try{
                return providers.get(apiType).details(id);
            }
            catch (Exception ex){
                throw  new CompletionException(ex.getMessage(),ex.getCause());
            }
        }).whenComplete((result, exception) ->{
            if (listener!=null){
                if (exception!=null){
                    listener.onDetailComplete(null,new WoosmapException(exception.getMessage()));
                }
                else{
                    listener.onDetailComplete(result,null);
                }
            }
        });
    }

    /***
     * Query the geocode Woosmap Address API to get details of an address
     * @param autocompleteResponseItem - <code>AutocompleteResponseItem</code> object
     */
    public void detailsAddress(AutocompleteResponseItem autocompleteResponseItem){
        if (autocompleteResponseItem.getApi()==SearchProviderType.ADDRESS){
            detailsMulti(autocompleteResponseItem);
        }
    }

    /***
     * Query the geocode Woosmap Address API to get details of an address
     * @param id - Public id of the place
     */
    public void detailsAddress(String id){
        detailsMulti(id,SearchProviderType.ADDRESS);
    }

    /***
     * Query the details Woosmap Details API. Check out the API
     * @param autocompleteResponseItem - <code>AutocompleteResponseItem</code> object
     */
    public void detailsLocalities(AutocompleteResponseItem autocompleteResponseItem){
        if (autocompleteResponseItem.getApi()==SearchProviderType.LOCALITIES){
            detailsMulti(autocompleteResponseItem);
        }
    }

    /***
     * Query the details Woosmap Details API. Check out the API
     * @param id Public id of the place
     */
    public void detailsLocalities(String id){
        detailsMulti(id,SearchProviderType.LOCALITIES);
    }

    /***
     * Query the details Google Places API to get details of a place
     * @param autocompleteResponseItem - <code>AutocompleteResponseItem</code> object
     */
    public void detailsPlaces(AutocompleteResponseItem autocompleteResponseItem){
        if (autocompleteResponseItem.getApi()==SearchProviderType.PLACES){
            detailsMulti(autocompleteResponseItem);
        }
    }

    /***
     * Query the details Google Places API to get details of a place
     * @param id - Public id of the place
     */
    public void detailsPlaces(String id){
        detailsMulti(id,SearchProviderType.PLACES);
    }

    /***
     * Query the search Woosmap Store API to get details of a store
     * @param autocompleteResponseItem <code>AutocompleteResponseItem</code> object
     */
    public void detailsStore(AutocompleteResponseItem autocompleteResponseItem){
        if (autocompleteResponseItem.getApi()==SearchProviderType.STORE){
            detailsMulti(autocompleteResponseItem);
        }
    }

    /***
     * Query the search Woosmap Store API to get details of a store
     * @param id - Public id of the place
     */
    public void detailsStore(String id){
        detailsMulti(id,SearchProviderType.STORE);
    }

    /***
     * Call <code>autocomplete</code> API of the given provider for the given search string
     * @param provider - Provider to from whom results be fetched
     * @param searchString - the search string
     * @return - List of <code>JSONObject</code>
     * @throws ExecutionException
     * @throws InterruptedException
     */
    private List<JSONObject>fetchProviderSearch(AbstractProvider provider,String searchString) throws WoosmapException {
        List<JSONObject> apiData;
        apiData = provider.search(searchString);
        return apiData;
    }

}
