package com.webgeoservices.multisearch.searchdatamodels;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Base64;
import android.util.Log;

import com.webgeoservices.multisearch.SearchProviderType;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/***
 * Data container class which holds the data returned by <code>autocomplete</code> method
 */
public class AutocompleteResponseItem implements Parcelable {
    private SearchProviderType api;
    private String description;
    private String id;
    private String highlight;
    private float score;
    private JSONArray matchedSubStrings;
    private JSONObject item;
    private String[]resultTypes;

    /***
     * Private constructor
     */
    private AutocompleteResponseItem(){
    }

    /***
     * Construct object from a parcel
     * @param in
     */
    protected AutocompleteResponseItem(Parcel in) {
        try {
            api= SearchProviderType.valueOf(in.readString());
            description = in.readString();
            id = in.readString();
            highlight = in.readString();
            score = in.readFloat();
            matchedSubStrings=new JSONArray(in.readString());
            item=new JSONObject(in.readString());
            resultTypes = in.createStringArray();
        }catch (JSONException ex){
            Log.e("AutocompleteResponseItem",ex.getMessage());
        }

    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(api.name());
        dest.writeString(description);
        dest.writeString(id);
        dest.writeString(highlight);
        dest.writeFloat(score);
        dest.writeString(matchedSubStrings.toString());
        dest.writeString(item.toString());
        dest.writeStringArray(resultTypes);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    /***
     * Creator to create <code>AutocompleteResponseItem</code> object from a parcel
     */
    public static final Creator<AutocompleteResponseItem> CREATOR = new Creator<AutocompleteResponseItem>() {
        @Override
        public AutocompleteResponseItem createFromParcel(Parcel in) {
            return new AutocompleteResponseItem(in);
        }

        @Override
        public AutocompleteResponseItem[] newArray(int size) {
            return new AutocompleteResponseItem[size];
        }
    };

    /***
     * The api the result was retrieved from. Possible values are
     * <code>localities</code>
     * <code>address</code>
     * <code>store</code>
     * <code>places</code>
     * @return <code>SearchProviderType</code> enum
     */
    public SearchProviderType getApi() {
        return api;
    }

    /***
     *  Contains the human-readable name for the returned result
     * @return String
     */
    public String getDescription() {
        return description;
    }

    /***
     * Item identifier. For <code>address</code> API, this is an internal identifier of the library
     * @return String
     */
    public String getId() {
        return id;
    }

    /***
     * HTML description in which the entered term in the prediction result text are in <mark>tags
     * @return String
     */
    public String getHighlight() {
        return highlight;
    }

    /***
     * Score of item that was calculated by a <code>Scorable</code> object
     * @return Float
     */
    public float getScore() {
        return score;
    }

    /***
     * Contains an array with offset value and length
     * These describe the location of the entered term in the prediction result text,
     * so that the term can be highlighted if desired
     * @return <code>JSONArray</code>
     */
    public JSONArray getMatchedSubStrings() {
        return matchedSubStrings;
    }

    /***
     * Underlying raw JSON that was returned by the API
     * @return <code>JSONObject</code>
     */
    public JSONObject getItem() {
        return item;
    }

    /***
     * Array of types that apply to this item
     * @return String Arrray
     */
    public String[] getResultTypes() {
        return resultTypes;
    }

    /***
     * Static mathod which construct and returns a new <code>AutocompleteResponseItem</code> object from the raw JSON response
     * @param jsonObject - Raw json response returned from the API
     * @param apiType - Type of the API which was used. i.e. <code>store</code>, <code>address</code>, <code>localities</code> and <code>places</code>
     * @param searchString - the search string for which API returned the value
     * @return <code>AutocompleteResponseItem</code> object
     */
    public static AutocompleteResponseItem fromJSON(JSONObject jsonObject,SearchProviderType apiType, String searchString){
        switch (apiType){
            case ADDRESS:
                return populateAddressResponseItem(jsonObject,searchString);
            case LOCALITIES:
                return populateLocalitiesResponseItem(jsonObject,searchString);
            case STORE:
                return populateStoreResponseItem(jsonObject,searchString);
            case PLACES:
                return populatePlacesResponseItem(jsonObject,searchString);
        }
        return null;
    }

    /***
     * Creates and returns <code>AutocompleteResponseItem</code> object from the response returned by <code>localities</code> API
     * @param jsonObject RAW json response returned by <code>localities</code> API
     * @param searchString the search string which was passed to <code>localities</code> API
     * @return <code>AutocompleteResponseItem</code> object
     */
    private static AutocompleteResponseItem populateLocalitiesResponseItem(JSONObject jsonObject, String searchString) {
        JSONObject searchItem;
        try {
            searchItem = jsonObject;
            if (jsonObject.has("item")) {
                searchItem = jsonObject.getJSONObject("item");
            }
            AutocompleteResponseItem searchResult = new AutocompleteResponseItem();
            searchResult.api = SearchProviderType.LOCALITIES;
            searchResult.description = searchItem.getString("description");
            searchResult.id = searchItem.getString("public_id");

            searchResult.score = 0;
            if (jsonObject.has("score")) {
                searchResult.score = (float) jsonObject.getDouble("score");
            }
            if (searchItem.has("matched_substrings")) {
                searchResult.matchedSubStrings = searchItem.getJSONObject("matched_substrings")
                        .optJSONArray("description");
                searchResult.highlight = getHighlightedTextString(searchItem.getString("description"),
                        searchItem.getJSONObject("matched_substrings").optJSONArray("description"));
            } else {
                searchResult.highlight = searchItem.getString("description");
            }
            searchResult.resultTypes = new String[]{searchItem.getString("type")};
            searchResult.item = searchItem;

            return searchResult;
        } catch (JSONException ex) {
            Log.e(AutocompleteResponseItem.class.getName(), ex.toString());
        }
        return null;
    }

    /***
     * Creates and returns <code>AutocompleteResponseItem</code> object from the response returned by <code>address</code> API
     * @param jsonObject RAW json response returned by <code>address</code> API
     * @param searchString the search string which was passed to <code>address</code> API
     * @return <code>AutocompleteResponseItem</code> object
     */
    private static AutocompleteResponseItem populateAddressResponseItem(JSONObject jsonObject, String searchString) {
        JSONObject searchItem;
        try {
            searchItem = jsonObject;
            if (jsonObject.has("item")) {
                searchItem = jsonObject.getJSONObject("item");
            }

            AutocompleteResponseItem searchResult = new AutocompleteResponseItem();
            searchResult.api = SearchProviderType.ADDRESS;
            searchResult.description = searchItem.getString("description");
            searchResult.id = Base64.encodeToString(searchItem.getString("description").getBytes(), Base64.NO_WRAP | Base64.URL_SAFE);
            searchResult.score = 0;
            if (jsonObject.has("score")) {
                searchResult.score = (float) jsonObject.getDouble("score");
            }
            if (searchItem.has("matched_substring")) {
                searchResult.matchedSubStrings = searchItem.getJSONObject("matched_substring")
                        .getJSONArray("description");
                searchResult.highlight = getHighlightedTextString(searchItem.getString("description"),
                        searchItem.getJSONObject("matched_substring").getJSONArray("description"));
            } else {
                searchResult.highlight = searchItem.getString("description");
            }
            searchResult.resultTypes = new String[]{searchItem.getString("type")};
            searchResult.item = searchItem;
            return searchResult;
        } catch (JSONException ex2) {
            Log.e(AutocompleteResponseItem.class.getName(), ex2.toString());
        }
        return null;
    }

    /***
     * Creates and returns <code>AutocompleteResponseItem</code> object from the response returned by <code>places</code> API
     * @param jsonObject RAW json response returned by <code>places</code> API
     * @param searchString the search string which was passed to <code>places</code> API
     * @return <code>AutocompleteResponseItem</code> object
     */
    private static AutocompleteResponseItem populatePlacesResponseItem(JSONObject jsonObject, String searchString) {
        JSONObject searchItem;
        try {

            searchItem = jsonObject;
            if (jsonObject.has("item")) {
                searchItem = jsonObject.getJSONObject("item");
            }

            AutocompleteResponseItem searchResult = new AutocompleteResponseItem();
            searchResult.api = SearchProviderType.PLACES;
            searchResult.description = searchItem.getString("description");
            searchResult.id = searchItem.getString("place_id");

            searchResult.score = 0;
            if (jsonObject.has("score")) {
                searchResult.score = (float) jsonObject.getDouble("score");
            }
            if (searchItem.has("matched_substrings")) {
                searchResult.matchedSubStrings = searchItem.getJSONArray("matched_substrings");
                searchResult.highlight = getHighlightedTextString(searchItem.getString("description"),
                        searchItem.getJSONArray("matched_substrings"));
            } else {
                searchResult.highlight = searchItem.getString("description");
            }
            searchResult.resultTypes = searchItem.getJSONArray("types").join(",").split(",");
            searchResult.item = searchItem;

            return searchResult;
        } catch (JSONException ex) {
            Log.e(AutocompleteResponseItem.class.getName(), ex.toString());
        }
        return null;
    }

    /***
     * Creates and returns <code>AutocompleteResponseItem</code> object from the response returned by <code>store</code> API
     * @param jsonObject RAW json response returned by <code>store</code> API
     * @param searchString the search string which was passed to <code>store</code> API
     * @return <code>AutocompleteResponseItem</code> object
     */
    private static AutocompleteResponseItem populateStoreResponseItem(JSONObject jsonObject, String searchString) {
        JSONObject searchItem;
        try {
            searchItem = jsonObject;
            if (jsonObject.has("item")) {
                searchItem = jsonObject.getJSONObject("item");
            }

            AutocompleteResponseItem searchResult = new AutocompleteResponseItem();
            searchResult.api = SearchProviderType.STORE;
            searchResult.description = searchItem.getString("description");
            searchResult.id = searchItem.getString("store_id");

            searchResult.score = 0;
            if (jsonObject.has("score")) {
                searchResult.score = (float) jsonObject.getDouble("score");
            }

            if (searchItem.has("matched_substrings")) {
                searchResult.matchedSubStrings = searchItem.getJSONArray("matched_substrings");
                searchResult.highlight = getHighlightedTextString(searchItem.getString("name"), searchItem.getJSONArray("matched_substrings"));
            } else {
                searchResult.highlight = searchItem.getString("description");
            }

            searchResult.resultTypes = searchItem.getJSONArray("types").join(",").split(",");
            searchResult.item = searchItem;

            return searchResult;
        } catch (JSONException ex) {
            Log.e(AutocompleteResponseItem.class.getName(), ex.toString());
        }
        return null;
    }

    /***
     * Generates and returns HTML description in which the entered term in the prediction result text are in <mark>tags
     * @param description string returned by the API
     * @param matchedSubStringArray JSONArray returned by the API
     * @return String
     */

    private static String getHighlightedTextString(String description, JSONArray matchedSubStringArray) {
        StringBuilder updatedHighlightedString = new StringBuilder();
        try {
            if (matchedSubStringArray != null && matchedSubStringArray.length() > 0) {
                JSONObject object, nextObject;
                for (int i = 0; i < matchedSubStringArray.length(); i++) {
                    object = matchedSubStringArray.getJSONObject(i);
                    if (i + 1 < matchedSubStringArray.length()) {
                        nextObject = matchedSubStringArray.getJSONObject(i + 1);
                    } else {
                        nextObject = matchedSubStringArray.getJSONObject(i);
                    }
                    int length = object.getInt("length");
                    int offset = object.getInt("offset");
                    if (i == 0) {
                        updatedHighlightedString.append(description.substring(0, offset));
                    }
                    updatedHighlightedString.append("<mark>");
                    updatedHighlightedString.append(description.substring(offset, offset + length));
                    updatedHighlightedString.append("</mark>");
                    if (!object.equals(nextObject)) {
                        updatedHighlightedString.append(description.substring(offset + length, nextObject.getInt("offset")));
                    } else {
                        updatedHighlightedString.append(description.substring(offset + length));
                    }

                }
                return updatedHighlightedString.toString();
            }
            return description;
        } catch (Exception ex) {
            return description;
        }
    }
}
