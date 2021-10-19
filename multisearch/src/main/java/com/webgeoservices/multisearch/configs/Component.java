package com.webgeoservices.multisearch.configs;

import com.webgeoservices.multisearch.utils.SearchUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/***
 * Class represents <code>components</code> equivalent in Multisearch JS SDK. Component is used to filter over countries
 */
public class Component {
    private final List<String> countries;
    private String language;

    /***
     * The constructor
     */
    public Component(){
        countries = new ArrayList<>();
    }

    /***
     * The constructor
     * @param countries String array of country codes. Countries must be passed as a two character, ISO 3166-1 Alpha-2 compatible country code.
     */
    public Component(String[] countries){
        this.countries = Arrays.asList(countries);
    }

    /***
     * The constructor
     * @param countries String array of country codes. Countries must be passed as a two character, ISO 3166-1 Alpha-2 compatible country code.
     * @param language The language code, indicating in which language the results should be returned, if possible.
     */
    public Component(String[] countries, String language){
        this.countries = Arrays.asList(countries);
        this.language = language;
    }

    /***
     * Adds country code to the underlying collection.
     * @param country String. Must be passed as a two character, ISO 3166-1 Alpha-2 compatible country code.
     */
    public void addCountry(String country){
        if (SearchUtil.isNullEmpty(country)){
            throw new RuntimeException("Country cannot be null or empty");
        }
        countries.add(country);
    }

    /***
     * Returns the country list.
     * @return String array
     */
    public String[] getCountries(){
        if(countries!=null){
            return (String[]) countries.toArray();
        }
        return null;

    }

    /**
     * Returns the language.
     * @return
     */
    public String getLanguage() {
        return language;
    }

    /***
     * Sets the language code, indicating in which language the results should be returned, if possible.
     * @param language String
     */
    public void setLanguage(String language) {
        if (SearchUtil.isNullEmpty(language)){
            throw new RuntimeException("language cannot be null or empty");
        }
        this.language = language;
    }
}
