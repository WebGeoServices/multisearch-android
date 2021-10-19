package com.webgeoservices.multisearch.configs;


import android.util.Log;

/***
 * ConfigParams class provides a way to pass additional parameters such as countries, search types and language to the <code>ProviderConfig</code> class.
 */
public class ConfigParams {
    private String[]searchType;
    private String[]query;
    private String language;
    private Component component;
    private Data data;
    private String extended;

    /***
     * Protected constructor
     */
    protected ConfigParams(){ }

    /***
     *  Returns countries array from Component object with ISO 3166-1 Alpha-2 compatible country code.
     * @return String Array
     */
    public String[] getCountries() {
        try{
            if(component!=null&&component.getCountries()!=null){
                return component.getCountries();
            }
        }
        catch (Exception ex){ Log.e(ConfigParams.class.getName(),ex.toString());}
        return null;
    }

    /**
     * Returns a string array with types of suggestion
     * @return String Array
     */
    public String[] getSearchType() {
        return searchType;
    }

    /**
     * Returns the language code.
     * @return String
     */
    public String getLanguage() {
        return language;
    }

    /***
     * Returns the <code>query</code> parameter
     * @return String array
     */
    public String[] getQuery() {
        return query;
    }

    /***
     * The types of suggestion to return. Several types are available. Some possible values are <code>locality</code>, <code>postal_code</code>, <code>address</code>, <code>admin_level</code>, <code>country</code>
     * @param searchType String Array
     */
    protected void setSearchType(String[] searchType) {
        this.searchType = searchType;
    }

    /***
     * The language code, indicating in which language the results should be returned, if possible
     * @param language String
     */
    protected void setLanguage(String language) {
        this.language = language;
    }

    /***
     * Sets the <code>query</code> parameter
     * @param query
     */
    protected void setQuery(String[] query) {
        this.query = query;
    }

    /***
     * Returns <code>component</code> object of the configuration
     * @return
     */
    public Component getComponent() {
        return component;
    }

    /***
     * Sets <code>component</code> object of the configuration
     */
    protected void setComponent(Component component) {
        this.component = component;
    }

    /***
     * Returns the <code>data</code> type of the configuration object
     * @return
     */
    public Data getData() {
        return data;
    }

    /***
     * Sets the <code>data</code> type of the configuration object
     */
    protected void setData(Data data) {
        this.data = data;
    }

    /***
     * Gets the extended data parameter.
     * @return
     */
    public String getExtended() {
        return extended;
    }

    /***
     * Sets the extended data parameter.
     * @param extended
     */
    protected void setExtended(String extended) {
        this.extended = extended;
    }
}
