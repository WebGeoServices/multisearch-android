package com.webgeoservices.multisearch.searchdatamodels;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.webgeoservices.multisearch.SearchProviderType;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


/***
 * Data container class which holds the data returned by <code>details</code> method
 */
public class DetailsResponseItem implements Parcelable {
    private static final String TAG=DetailsResponseItem.class.getSimpleName();
    private String id;
    private String formattedAddress;
    private String name;
    private String[] types;
    private JSONObject item;
    private Geometry geometry;
    private AddressComponent[] addressComponents;

    /***
     * Private constructor
     */
    private DetailsResponseItem(){
    }

    /***
     * Construct object from a parcel
     * @param in
     */
    protected DetailsResponseItem(Parcel in) {
        try {
            id = in.readString();
            formattedAddress = in.readString();
            name = in.readString();
            types = in.createStringArray();
            item=new JSONObject(in.readString());
            geometry= in.readParcelable(Geometry.class.getClassLoader());
            addressComponents=in.createTypedArray(AddressComponent.CREATOR);
        }catch (JSONException ex){
            Log.e(TAG,ex.getMessage());
        }

    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(formattedAddress);
        dest.writeString(name);
        dest.writeStringArray(types);
        dest.writeString(item.toString());
        dest.writeParcelable(geometry, flags);
        dest.writeTypedArray(addressComponents,flags);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    /***
     * Creator to create <code>AutocompleteResponseItem</code> object from a parcel
     */
    public static final Creator<DetailsResponseItem> CREATOR = new Creator<DetailsResponseItem>() {
        @Override
        public DetailsResponseItem createFromParcel(Parcel in) {
            return new DetailsResponseItem(in);
        }

        @Override
        public DetailsResponseItem[] newArray(int size) {
            return new DetailsResponseItem[size];
        }
    };


    /***
     * Item identifier. For <code>address</code> API, this is an internal identifier of the library
     * @return String
     */
    public String getId() {
        return id;
    }

    /***
     * String containing the human-readable address of this item
     * @return String
     */
    public String getFormattedAddress() {
        return formattedAddress;
    }

    /***
     * Item name
     * @return String
     */
    public String getName() {
        return name;
    }

    /**
     * Array of feature types describing the given item (like <code>locality</code> or <code>postal_town</code>)
     * @return String Array
     */
    public String[] getTypes() {
        return types;
    }

    /***
     * Underlying raw JSON that was returned by the API
     * @return <code>JSONObject</code>
     */
    public JSONObject getItem() {
        return item;
    }

    /***
     * Item geometry returned by the underlying API as <code>Geometry</code> object
     * @return <code>Geometry</code> object
     */
    public Geometry getGeometry() {
        return geometry;
    }

    /***
     * Returns and array containing <code>AddressComponent</code> objects applicable to this address.
     * Each component has a long name (full text description or name of the address component),
     * a short name (abbreviated textual name for the address component, if available)
     * and types (array indicating the type of the address component which can be locality, street_number, country, route or postal_code)
     * @return
     */
    public AddressComponent[] getAddressComponents() {
        return addressComponents;
    }

    /***
     * Static mathod which constructs and returns a new <code>DetailsResponseItem</code> object from the raw JSON response
     * @param jsonObject - Raw json response returned from the API
     * @param apiType - Type of the API which was used. i.e. <code>store</code>, <code>address</code>, <code>localities</code> and <code>places</code>
     * @param id - id of the object
     * @return <code>DetailsResponseItem</code> object
     */
    public static DetailsResponseItem fromJSON(JSONObject jsonObject,SearchProviderType apiType,String id){
        switch (apiType){
            case ADDRESS:
                return populateAddressDetail(jsonObject,id);
            case LOCALITIES:
                return populateLocalityDetail(jsonObject);
            case STORE:
                return populateStoreDetail(jsonObject,id);
            case PLACES:
                return populatePlaceDetail(jsonObject);
        }
        return null;
    }

    /***
     * Creates and returns <code>DetailsResponseItem</code> object from the response returned by <code>address</code> API
     * @param jsonObject RAW json response returned by <code>address</code> API
     * @param id Public id
     * @return <code>DetailsResponseItem</code> object
     */
    private static DetailsResponseItem populateAddressDetail(JSONObject jsonObject,String id){
        DetailsResponseItem detailsResponseItem=new DetailsResponseItem();
        Geometry geometryDetail;
        try {
            detailsResponseItem.formattedAddress=jsonObject.getString("formatted_address");
            detailsResponseItem.id=id;
            JSONArray typesArray=jsonObject.getJSONArray("types");
            String[] types=new String[typesArray.length()];
            for(int i=0;i<typesArray.length();i++){
                types[i]=typesArray.getString(i);
            }
            detailsResponseItem.types= types;
            detailsResponseItem.item=jsonObject;

            if (jsonObject.has("geometry")){
                geometryDetail = new Geometry();

                geometryDetail.setLocation(new Location(
                        jsonObject.getJSONObject("geometry").getJSONObject("location").getDouble("lat"),
                        jsonObject.getJSONObject("geometry").getJSONObject("location").getDouble("lng")
                ));
                detailsResponseItem.geometry = geometryDetail;
            }

            if (jsonObject.has("address_components")){
                detailsResponseItem.addressComponents = AddressComponent.getAddressComponents(jsonObject.getJSONArray("address_components"));
            }
            else {
                detailsResponseItem.addressComponents = new AddressComponent[0];
            }

        }catch (Exception ex){
            Log.e(TAG,ex.getMessage());
        }
        return detailsResponseItem;
    }

    /***
     * Creates and returns <code>DetailsResponseItem</code> object from the response returned by <code>localities</code> API
     * @param jsonObject RAW json response returned by <code>localities</code> API
     * @return <code>DetailsResponseItem</code> object
     */
    private static DetailsResponseItem populateLocalityDetail(JSONObject jsonObject){
        DetailsResponseItem detailsResponseItem=new DetailsResponseItem();
        Geometry geometryDetail;
        try{
            detailsResponseItem.formattedAddress=jsonObject.getString("formatted_address");
            JSONArray typesArray=jsonObject.getJSONArray("types");
            String[] types=new String[typesArray.length()];
            for(int i=0;i<typesArray.length();i++){
                types[i]=typesArray.getString(i);
            }
            detailsResponseItem.types= types;
            detailsResponseItem.id=jsonObject.getString("public_id");
            detailsResponseItem.item=jsonObject;
            if (jsonObject.has("name")){
                detailsResponseItem.name=jsonObject.getString("name");
            }


            if (jsonObject.has("geometry")){
                geometryDetail = new Geometry();


                geometryDetail.setLocation(new Location(
                        jsonObject.getJSONObject("geometry").getJSONObject("location").getDouble("lat"),
                        jsonObject.getJSONObject("geometry").getJSONObject("location").getDouble("lng")
                ));

                detailsResponseItem.geometry = geometryDetail;
            }

            if (jsonObject.has("address_components")){
                detailsResponseItem.addressComponents = AddressComponent.getAddressComponents(jsonObject.getJSONArray("address_components"));
            }
            else {
                detailsResponseItem.addressComponents = new AddressComponent[0];
            }

        }catch (Exception ex){
            Log.e(TAG,ex.getMessage());
        }
        return detailsResponseItem;
    }

    /***
     * Creates and returns <code>DetailsResponseItem</code> object from the response returned by <code>store</code> API
     * @param jsonObject RAW json response returned by <code>store</code> API
     * @param id Public id
     * @return <code>DetailsResponseItem</code> object
     */
    private static DetailsResponseItem populateStoreDetail(JSONObject jsonObject,String id){
        DetailsResponseItem detailsResponseItem=new DetailsResponseItem();
        JSONObject properties;
        JSONArray addressLineArray;
        try{

            detailsResponseItem.id=id;
            properties=jsonObject.getJSONObject("properties");
            if(properties.has("address")){
               addressLineArray=properties.getJSONObject("address").getJSONArray("lines");
               StringBuilder formattedAddress=new StringBuilder();
               for(int i=0;i<addressLineArray.length();i++){
                   formattedAddress.append(addressLineArray.getString(i));
               }
                detailsResponseItem.formattedAddress=formattedAddress.toString().trim();
            }else {
                detailsResponseItem.formattedAddress=properties.getString("name");
            }
            JSONArray typesArray=properties.getJSONArray("types");
            if(typesArray.length()>0){
                String[] types=new String[typesArray.length()];
                for(int i=0;i<typesArray.length();i++){
                    types[i]=typesArray.getString(i);
                }
                detailsResponseItem.types= types;
            }else {
                detailsResponseItem.types=new String[0];
            }
            detailsResponseItem.item=jsonObject;
            detailsResponseItem.name=properties.getString("name");

            if (jsonObject.has("geometry")){
                Geometry geometryDetail = new Geometry();

                geometryDetail.setLocation(new Location(
                        jsonObject.getJSONObject("geometry").getJSONArray("coordinates").getDouble(1),
                        jsonObject.getJSONObject("geometry").getJSONArray("coordinates").getDouble(0)
                ));

                detailsResponseItem.geometry = geometryDetail;
            }

            if (properties.has("address_components")){
                detailsResponseItem.addressComponents = AddressComponent.getAddressComponents(properties.getJSONArray("address_components"));
            }
            else {
                detailsResponseItem.addressComponents = new AddressComponent[0];
            }

        }catch (Exception ex){
            Log.e(TAG,ex.getMessage());
        }
        return detailsResponseItem;
    }

    /***
     * Creates and returns <code>DetailsResponseItem</code> object from the response returned by <code>places</code> API
     * @param jsonObject RAW json response returned by <code>places</code> API
     * @return <code>DetailsResponseItem</code> object
     */
    private static DetailsResponseItem populatePlaceDetail(JSONObject jsonObject){
        DetailsResponseItem detailsResponseItem=new DetailsResponseItem();
        Geometry geometryDetail;
        try{

            detailsResponseItem.formattedAddress=jsonObject.getString("formatted_address");
            JSONArray typesArray=jsonObject.getJSONArray("types");
            String[] types=new String[typesArray.length()];
            for(int i=0;i<typesArray.length();i++){
                types[i]=typesArray.getString(i);
            }
            detailsResponseItem.types= types;
            detailsResponseItem.id=jsonObject.getString("place_id");
            detailsResponseItem.item=jsonObject;
            detailsResponseItem.name=jsonObject.getString("name");

            if (jsonObject.has("geometry")){
                geometryDetail = new Geometry();

                geometryDetail.setLocation(new Location(
                        jsonObject.getJSONObject("geometry").getJSONObject("location").getDouble("lat"),
                        jsonObject.getJSONObject("geometry").getJSONObject("location").getDouble("lng")
                ));

                detailsResponseItem.geometry = geometryDetail;
            }

            if (jsonObject.has("address_components")){
                detailsResponseItem.addressComponents = AddressComponent.getAddressComponents(jsonObject.getJSONArray("address_components"));
            }
            else {
                detailsResponseItem.addressComponents = new AddressComponent[0];
            }

        }catch (Exception ex){
            Log.e(TAG,ex.getMessage());
        }
        return detailsResponseItem;
    }
}
