package com.webgeoservices.multisearch.searchdatamodels;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.webgeoservices.multisearch.utils.SearchUtil;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;

/***
 * Represents address component in <code>DetailsResponseItem</code> object.
 */
public class AddressComponent implements Parcelable {
    private String[] longName;
    private String[] shortName;
    private String[] types;

    /***
     * Private constructor
     */
    private AddressComponent(){
    }

    /***
     * Constructor creating object from the parcel.
     * @param in
     */
    protected AddressComponent(Parcel in) {
        longName = in.createStringArray();
        shortName = in.createStringArray();
        types = in.createStringArray();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeStringArray(longName);
        dest.writeStringArray(shortName);
        dest.writeStringArray(types);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<AddressComponent> CREATOR = new Creator<AddressComponent>() {
        @Override
        public AddressComponent createFromParcel(Parcel in) {
            return new AddressComponent(in);
        }

        @Override
        public AddressComponent[] newArray(int size) {
            return new AddressComponent[size];
        }
    };

    /***
     * Returns long name (full text description or name of the address component)
     * @return String Array
     */
    public String[] getLongName() {
        return longName;
    }

    /***
     * Returns a short name (abbreviated textual name for the address component, if available)
     * @return String Array
     */
    public String[] getShortName() {
        return shortName;
    }

    /***
     * Returns types (array indicating the type of the address component which can be locality, street_number, country, route or postal_code)
     * @return String Array
     */
    public String[] getTypes() {
        return types;
    }

    /***
     * Creates a single AddressComponent class from the given JSONObject
     * @param data JSONObject
     * @return AddressComponent object
     */
    private static AddressComponent getAddressComponent(JSONObject data){
        AddressComponent addressComponent = new AddressComponent();
        int counter;
        try{
            if  (data.get("types") instanceof  String){
                addressComponent.types = new String[]{data.getString("types")};
            }
            else if (data.get("types") instanceof  JSONArray){
                addressComponent.types = new String[data.getJSONArray("types").length()];

                for(counter=0;counter<data.getJSONArray("types").length();counter++){
                    addressComponent.types[counter] = data.getJSONArray("types").getString(counter);
                    if (addressComponent.types[counter].trim().equalsIgnoreCase("postal_codes")){
                        addressComponent.types[counter] = "postal_code";
                    }
                }
            }

            if  (data.get("long_name") instanceof  String){
                addressComponent.longName = new String[]{data.getString("long_name")};
            }
            else if (data.get("long_name") instanceof  JSONArray){
                addressComponent.longName = new String[data.getJSONArray("long_name").length()];

                for(counter=0;counter<data.getJSONArray("long_name").length();counter++){
                    addressComponent.longName[counter] = data.getJSONArray("long_name").getString(counter);
                }
            }

            if  (data.get("short_name") instanceof  String){
                addressComponent.shortName = new String[]{data.getString("short_name")};
            }
            else if (data.get("short_name") instanceof  JSONArray){
                addressComponent.shortName = new String[data.getJSONArray("short_name").length()];

                for(counter=0;counter<data.getJSONArray("short_name").length();counter++){
                    addressComponent.shortName[counter] = data.getJSONArray("short_name").getString(counter);
                }
            }
        }
        catch (Exception ex){
            Log.e(AddressComponent.class.getName(),ex.toString());
        }
        return addressComponent;
    }

    /***
     * Creates an array of AddressComponent objects from the given JSONArray
     * @param array JSONArray
     * @return array of AddressComponent objects
     */
    protected static AddressComponent[] getAddressComponents(JSONArray array) {
        ArrayList<AddressComponent> addressComponents = new ArrayList<>();
        AddressComponent addressComponent;
        try {
            for (int counter = 0; counter < array.length(); counter++) {
                addressComponent = getAddressComponent(array.getJSONObject(counter));
                if (isAddressTypePresent(addressComponent.getTypes())) {
                    updateTypePostalCodesString(addressComponent.getTypes());
                    addressComponents.add(addressComponent);
                }
            }
        } catch (Exception ex) {
            Log.e(AddressComponent.class.getName(), ex.toString());
        }
        return addressComponents.toArray(new AddressComponent[addressComponents.size()]);
    }

    private static boolean isAddressTypePresent(String[] addressComponentType) {
        try {
            if (addressComponentType != null && addressComponentType.length > 0) {
                for (int i = 0; i < addressComponentType.length; i++) {
                    if (Arrays.asList(SearchUtil.ADDRESS_TYPES).contains(addressComponentType[i])) {
                        return true;
                    }
                }
            }
        } catch (Exception ex) {
            Log.e(AddressComponent.class.getName(), ex.toString());
            return false;
        }
        return false;
    }

    private static void updateTypePostalCodesString(String[] addressComponentType) {
        try {
            if (addressComponentType != null && addressComponentType.length > 0) {
                for (int i = 0; i < addressComponentType.length; i++) {
                    if (addressComponentType[i].equals("postal_codes")) {
                        addressComponentType[i] = "postal_code";
                    }
                }
            }
        } catch (Exception ex) {
            Log.e(AddressComponent.class.getName(), ex.toString());
        }
    }
}
