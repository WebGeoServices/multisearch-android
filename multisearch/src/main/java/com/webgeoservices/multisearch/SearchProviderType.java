package com.webgeoservices.multisearch;

/**
 * Enum to represent Search provider types. 
 * <code>LOCALITIES</code> will call Woosmap localities API. 
 * <code>ADDRESS</code> will call Woosmap address API. 
 * <code>STORE</code> will call Woosmap store API. 
 * <code>PLACES</code> will call Google Places API.
 */
public enum SearchProviderType {
    LOCALITIES,ADDRESS,STORE,PLACES
}
