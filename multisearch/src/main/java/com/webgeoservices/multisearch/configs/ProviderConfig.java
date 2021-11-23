package com.webgeoservices.multisearch.configs;


import com.webgeoservices.multisearch.SearchProviderType;
import com.webgeoservices.multisearch.utils.SearchUtil;

import java.util.ArrayList;

/***
 * ProviderConfig provides a way configure options to MultiSearch API
 */
public class ProviderConfig {
    private final SearchProviderType type;
    private final String key;
    private float fallbackBreakpoint =-1;
    private int minInputLength=0;
    private ConfigParams configParams;
    private boolean ignoreFallbackBreakpoint=false;
    /**
     * Returns the type of the provider. Pssible values are <code>LOCALITIES</code>, <code>ADDRESS</code>, <code>STORE</code> and <code>PLACES</code>
     * @return
     */
    public SearchProviderType getType() {
        return type;
    }

    /**
     * Returns API key which will be used by the provider
     * @return
     */
    public String getKey() {
        return key;
    }

    /**
     * Returns if the fallbackBreakPoint value of the provider
     * @return
     */
    public float getFallbackBreakpoint() {
        if (fallbackBreakpoint >=0 && fallbackBreakpoint <=1){
            return fallbackBreakpoint;
        }
        switch (type){
            case ADDRESS:
                return 0.5f;
            case PLACES:
                return 1f;
            case LOCALITIES:
                return 0.4f;
            case STORE:
                return 1f;
            default:
                return 0f;
        }
    }

    /**
     * Empty result will be sent by the API and no fallback will be triggered if the input length does not reach the minInputLength value
     * @return
     */
    public int getMinInputLength() {
        return minInputLength;
    }

    /**
     * Returns <code>ConfigParams</code> object which returns additional parameters like <code>country</code>, <code>language</code>
     * @return
     */
    public ConfigParams getConfigParams() {
        return configParams;
    }

    /***
     * Returns if the provider will ignore fallbackbreakpoint value and include all results
     * @return
     */
    public boolean shouldIgnoreFallbackBreakPoint(){
        return ignoreFallbackBreakpoint;
    }

    /***
     * Private constructure used by the <code>Builder</code> class to construct ProviderConfig object
     * @param builder
     */
    private ProviderConfig(Builder builder){
        if (SearchUtil.isNullEmpty(builder.key)){
            throw new RuntimeException("API Key cannot be null or empty");
        }
        if (configParams==null){
            configParams = new ConfigParams();
        }

        this.type = builder.searchProviderType;
        this.key = builder.key;
        this.ignoreFallbackBreakpoint = builder.ignoreFallbackBreakpoint;
        this.fallbackBreakpoint = builder.fallbackBreakpoint;
        this.minInputLength = builder.minInputLength;

        if (builder.searchType!=null){
            configParams.setSearchType(builder.searchType.toArray(new String[builder.searchType.size()]));
        }

        if (builder.query!=null){
            configParams.setQuery(builder.query.toArray(new String[builder.query.size()]));
        }

        if (builder.language!=null){
            configParams.setLanguage(builder.language);
        }

        if (builder.component!=null){
            configParams.setComponent(builder.component);
        }

        configParams.setData(builder.data);
        configParams.setExtended(builder.extended);
        configParams.setFields(builder.fields);
    }

    /***
     * A builder class which is used to create ProviderConfig configuration object which will be passed to MultiSearch API
     */
    public static class Builder{
        private final SearchProviderType searchProviderType;
        private String key=null;
        private float fallbackBreakpoint=-1;
        private int minInputLength=0;
        private boolean ignoreFallbackBreakpoint=false;
        private ArrayList<String> searchType=null;
        private String language=null;
        private ArrayList<String> query;
        private Component component;
        private Data data;
        private String extended ="";
        private String fields ="";

        /***
         * Public constructor
         * @param searchProviderType
         */
        public Builder(SearchProviderType searchProviderType){
            this.searchProviderType = searchProviderType;
        }

        /***
         * API key which will be used by the provider.
         * @param key - the API key
         * @return
         */
        public Builder key(String key) {
            this.key = key;
            return this;
        }

        /***
         * <code>fallbackBreakPoint</code> value used by the provider.
         * If no <code>fallbackBreakPoint</code> is provided then the provider will keep the default value based on the provider type. 
         * Following are the default values for each provider: 
         * <code>store</code> - 1
         * <code>localities</code> - 0.4
         * <code>address</code> - 0.5
         * <code>places</code> - 1
         * @param fallbackBreakpoint - the fallBackBreakPoint
         * @return
         */
        public Builder fallbackBreakpoint(float fallbackBreakpoint) {
            if (fallbackBreakpoint<0 || fallbackBreakpoint > 1){
                throw new RuntimeException("fallbackBreakpoint must be between 0 and 1");
            }
            this.fallbackBreakpoint = fallbackBreakpoint;
            return this;
        }

        /***
         * Minimum input length of the search string.
         * Empty result will be sent by the API and no callback will be triggered if the input length does not reach the minInputLength value
         * @param minInputLength - the minInputLength
         * @return
         */
        public Builder minInputLength(int minInputLength) {
            this.minInputLength = minInputLength;
            return this;
        }

        /***
         * Specifies if provider should ignore <code>fallbackBreakPoint</code> value and include all results by the provider
         * @param ignoreFallbackBreakpoint
         * @return
         */
        public Builder ignoreFallbackBreakpoint(boolean ignoreFallbackBreakpoint) {
            this.ignoreFallbackBreakpoint = ignoreFallbackBreakpoint;
            return this;
        }

        /***
         * The types of suggestions to return. Several types are available
         * Some possible values are <code>locality</code>, <code>postal_code</code>, <code>address</code>, <code>admin_level</code>, <code>country</code>
         * @param searchType - String Array with search types
         * @return
         */
        public Builder searchType(String searchType) {
            if (this.searchType == null){
                this.searchType = new ArrayList<>();
            }
            this.searchType.add(searchType);
            return this;
        }

        /***
         * The language code, indicating in which language the results should be returned, if possible
         * @param language - the language.
         * @return
         */
        public Builder language(String language) {
            this.language = language;
            return this;
        }

        /***
         *
         * @param query
         * @return
         */
        public Builder query(String query){
            if (this.query == null){
                this.query = new ArrayList<>();
            }
            this.query.add(query);
            return this;
        }

        /***
         * A grouping of places to which you would like to restrict your results. Currently, you can use components to filter over countries.
         * @param component
         * @return
         */
        public Builder component(Component component){
            this.component = component;
            return this;
        }

        /***
         * Two values for this parameter: standard or advanced.
         * By default, if the parameter is not defined, value is set as standard.
         * The advanced value opens suggestions to worldwide postal codes in addition to postal codes for Western Europe.
         * A dedicated option subject to specific billing on your license is needed to use this parameter.
         * Please contact us if you are interested in using this parameter and you do not have subscribed the proper option yet.
         * @param data
         * @return
         */
        public Builder data(Data data){
            this.data = data;
            return this;
        }

        /***
         * If set, this parameter allows a refined search over locality names that bears the same postal code.
         * By triggering this parameter, integrators will benefit from a search spectrum on the locality type that includes postal codes.
         * To avoid confusion, it is recommended not to activate this parameter along with the postal_code type which could lead to duplicate locations.
         * Also, the default description returned by the API changes to name (postal code), admin_1, admin_0.
         * It is only available for France and Italy.
         * @param extended
         * @return
         */
        public Builder extended(String extended){
            this.extended = extended.trim();
            return this;
        }

        /***
         * You can use this parameter to limit the returning fields (by default, all fields are return).
         * Available fields are (geometry) (fields should be separated by a ,). 
         * By using this parameter you will limit content of responses to the geometry part. 
         * No address component provided.
         * @param fields
         * @return
         */
        public Builder fields(String fields){
            this.fields = fields.trim();
            return this;
        }

        /***
         * Builds and returns <code>ProviderConfig</code> object based on the provided parameters
         * @return the ProviderConfig object
         */
        public ProviderConfig build(){
            return new ProviderConfig(this);
        }
    }
}
