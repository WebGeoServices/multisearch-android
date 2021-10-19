package com.webgeoservices.multisearch.scorers;

import android.content.Context;
import android.util.Log;

import com.webgeoservices.multisearch.interfaces.Scorable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.liquidplayer.javascript.JSContext;
import org.liquidplayer.javascript.JSValue;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/***
 * A singleton class that uses <a href="https://fusejs.io/concepts/scoring-theory.html">Fuse.js</a> Javascript library to score the results given by different providers
 * Implements Scorable interface
 */
public class FuseScorer implements Scorable {
    private final Context context;
    private final JSContext jsContext;
    private static FuseScorer instance = null;
    private final JSONObject configuration;

    /***
     * Returns an instance of FuseScorer class
     * @param context - the context
     * @param configuration Fuse.js configuration options. See <a href="https://fusejs.io/api/options.html">here</a> for more
     * @return
     */
    public static FuseScorer getInstance(Context context, JSONObject configuration){
        if (instance == null){
            instance = new FuseScorer(context,configuration);
        }
        return instance;
    }

    /***
     * The constructor
     * @param context
     * @param configuration
     */
    private FuseScorer(Context context, JSONObject configuration){
        this.context = context.getApplicationContext();
        this.configuration = configuration;
        jsContext = new JSContext();
        initializeFuseJS();
    }

    /***
     * Scores and sorts the results based on given search string
     * @param data - List of JSONObject (s) which needs to be scored and sorted
     * @param searchString - Searh string based on which the scoring should take place
     * @param fallbackBreakpoint - At what point does the match algorithm give up
     * @return A List of JSONObject (s) which are scored and sorted
     */
    @Override
    public List<JSONObject> scoreResults(List<JSONObject> data, String searchString, float fallbackBreakpoint) {
        JSValue jsValue;
        JSONArray dataList = new JSONArray();
        List<JSONObject> scroredData;
        for(JSONObject element: data){
            dataList.put(element);
        }

        jsContext.evaluateScript("configuration.threshold = " + fallbackBreakpoint + ";");
        jsContext.property("configuration");

        jsContext.property("dataList",dataList);
        jsContext.property("dataList");

        jsContext.property("searchString",searchString);
        jsContext.property("searchString");

        jsContext.evaluateScript("fuse = new Fuse(dataList, configuration);");
        jsContext.evaluateScript("result = fuse.search(searchString);");

        jsValue = jsContext.property("result");

        try{
            dataList = new JSONArray(jsValue.toJSON());
            scroredData = new ArrayList<>();
            for(int counter=0;counter<dataList.length();counter++){
                scroredData.add(dataList.getJSONObject(counter));
            }
            return scroredData;
        }
        catch (JSONException ex){
            Log.e(FuseScorer.class.getName(),ex.toString());
        }
        return data;
    }

    /***
     * initializes Fuse.js
     */
    private void initializeFuseJS(){
        jsContext.property("configuration",configuration);

        String fileContent = getFuseSourceFromAssets();
        jsContext.evaluateScript(fileContent);

        jsContext.property("dataList",null);
        jsContext.property("fuse",null);
        jsContext.property("result",null);

        Log.d(FuseScorer.class.getName(),"Here");
    }

    /***
     * Reads from an asset file and returns it's content in string format
     * @return File content
     */
    private String getFuseSourceFromAssets(){
        BufferedReader reader = null;
        StringBuilder stringBuilder=new StringBuilder();
        try {
            reader = new BufferedReader(new InputStreamReader(context.getAssets().open("fuse_prod.js"), StandardCharsets.UTF_8));
            // do reading, usually loop until end of file reading
            String mLine;
            while ((mLine = reader.readLine()) != null) {
                //process line
                stringBuilder.append(mLine);
            }
        } catch (IOException e) {
            //log the exception
            Log.e(FuseScorer.class.getName(),e.toString());
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    //log the exception
                    Log.e(FuseScorer.class.getName(),e.toString());
                }
            }
        }
        return stringBuilder.toString();
    }
}
