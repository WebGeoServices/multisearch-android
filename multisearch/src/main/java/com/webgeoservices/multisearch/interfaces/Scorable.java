package com.webgeoservices.multisearch.interfaces;

import org.json.JSONObject;
import java.util.List;

/***
 * Interface which can be used to implement scoring of the search results
 * Known implementation is <code>FuseScorer</code>
 */
public interface Scorable {
    /***
     * Method to accept search results. Score them based on the search string. Filter them using fallbackBreakpoint
     * Return the resulting array
     * @param data - List of JSONObject (s) which needs to be scored, sorted
     * @param searchString - Searh string based on which the scoring should take place
     * @return List of JSONObject (s) which are scored
     */
    List<JSONObject> scoreResults(List<JSONObject> data,String searchString,float fallbackBreakpoint);
}
