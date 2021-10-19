package com.webgeoservices.multisearch.listeners;

import com.webgeoservices.multisearch.WoosmapException;
import com.webgeoservices.multisearch.searchdatamodels.AutocompleteResponseItem;
import com.webgeoservices.multisearch.searchdatamodels.DetailsResponseItem;

import java.util.List;

/**
 * A callback interface for MultiSearch autocomplete and details methods
 */
public interface MultiSearchListener {
    /***
     * This callback will be invoked once MultiSearch finishes any of it's autocomplete methods
     * @param searchResult A list of AutocompleteResponseItem objects. Value will be null if any exception has occurred
     * @param exception A <code>WoosmapException</code> exception object. Will be null if there was no exception during the execution
     */
    void onSearchComplete(List<AutocompleteResponseItem>searchResult, WoosmapException exception);

    /***
     * This callback will be invoked once MultiSearch finishes any of it's details methods
     * @param detailResult A DetailsResponseItem object. Value will be null if any exception has occurred
     * @param exception A <code>WoosmapException</code> exception object. Will be null if there was no exception during the execution
     */
    void onDetailComplete(DetailsResponseItem detailResult, WoosmapException exception);
}
