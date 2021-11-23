package com.webgeoservices.multisearch;

import android.content.Context;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import com.webgeoservices.multisearch.configs.Component;
import com.webgeoservices.multisearch.configs.ProviderConfig;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/***
 * Details unit tests defined by Leo in Test scenarios Google Sheet
 */
@RunWith(AndroidJUnit4.class)
public class DetailsTests {

    private String apiKey;
    private String googleKey;
    private MultiSearch multiSearch;

    @Before
    public void setUp(){
        apiKey = InstrumentationRegistry.getInstrumentation().getTargetContext().getString(R.string.__wgs_woosemap_private_key);
        googleKey = InstrumentationRegistry.getInstrumentation().getTargetContext().getString(R.string.__wgs_google_api_key);
        multiSearch = new MultiSearch(InstrumentationRegistry.getInstrumentation().getTargetContext());
    }

    /***
     * #3. Test Google places details for id  ChIJEW4ls3nVwkcRYGNkgT7xCgQ
     */
    @Test
    public void testDetailPlace(){
        multiSearch.removeAllProviders();
        final TestSearchListener multiSearchListener = new TestSearchListener();

        ProviderConfig.Builder builder;

        builder = new ProviderConfig.Builder(SearchProviderType.PLACES)
                .key(googleKey);

        multiSearch.addProvider(builder.build());//Add places provider
        multiSearch.addSearchListener(multiSearchListener);

        multiSearch.detailsPlaces("ChIJEW4ls3nVwkcRYGNkgT7xCgQ");
        synchronized (multiSearchListener){
            try {
                multiSearchListener.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        if (multiSearchListener.exception==null){
            Assert.assertEquals("Expected formated address","Lille, France",multiSearchListener.detailsResponseItem.getFormattedAddress());
        }
        else{
            Assert.fail(multiSearchListener.exception.toString());
        }

    }

    /**
     * Cross check the id returned from Google Maps Autocomplete to that of given in the sheet (#3)
     */
    @Test
    public void testDetailPlaceAutocomplete(){
        multiSearch.removeAllProviders();
        final TestSearchListener multiSearchListener = new TestSearchListener();

        ProviderConfig.Builder builder;

        builder = new ProviderConfig.Builder(SearchProviderType.PLACES)
                .key(googleKey);

        multiSearch.addProvider(builder.build());//Add places provider
        multiSearch.addSearchListener(multiSearchListener);

        multiSearch.autocompletePlaces("Lille, France");
        try{
            synchronized (multiSearchListener){
                multiSearchListener.wait();
            }
        }
        catch (Exception ex){}
        if (multiSearchListener.exception!=null){
            Assert.fail(multiSearchListener.exception.toString());
        }
        Assert.assertEquals("expectedID","ChIJEW4ls3nVwkcRYGNkgT7xCgQ",multiSearchListener.items.get(0).getId());
    }

    /***
     * #4. Check localities details for id vWA87ZwgR3V1RMRpY0QXZEGQoFk=
     */
    @Test
    public void testDetailLocalities(){
        multiSearch.removeAllProviders();
        final TestSearchListener multiSearchListener = new TestSearchListener();

        ProviderConfig.Builder builder;

        builder = new ProviderConfig.Builder(SearchProviderType.LOCALITIES)
                .key(apiKey).component(new Component(new String[]{"fr"})).language("en").searchType("locality")
                .searchType("postal_code");

        multiSearch.addProvider(builder.build());//Add localities provider
        multiSearch.addSearchListener(multiSearchListener);

        multiSearch.detailsLocalities("vWA87ZwgR3V1RMRpY0QXZEGQoFk=");
        synchronized (multiSearchListener){
            try {
                multiSearchListener.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        if (multiSearchListener.exception==null){
            Assert.assertEquals("Expected formated address","Montpellier, Hérault",multiSearchListener.detailsResponseItem.getFormattedAddress());
        }
        else{
            Assert.fail(multiSearchListener.exception.toString());
        }
    }

    /**
     * Crosscheck localities autocomplete to match the id given in the sheet (#4)
     */
    @Test
    public void testDetailLocalitiesAutocomplete(){
        multiSearch.removeAllProviders();
        final TestSearchListener multiSearchListener = new TestSearchListener();

        ProviderConfig.Builder builder;

        builder = new ProviderConfig.Builder(SearchProviderType.LOCALITIES)
                .key(apiKey).component(new Component(new String[]{"fr"})).language("en").searchType("locality")
                .searchType("postal_code");

        multiSearch.addProvider(builder.build());//Add localities provider
        multiSearch.addSearchListener(multiSearchListener);

        multiSearch.autocompleteLocalities("Montpellier, Hérault, France");
        try{
            synchronized (multiSearchListener){
                multiSearchListener.wait();
            }
            if (multiSearchListener.exception!=null){
                Assert.fail(multiSearchListener.exception.toString());
            }
            Assert.assertEquals("expectedID","vWA87ZwgR3V1RMRpY0QXZEGQoFk=",multiSearchListener.items.get(0).getId());
        }
        catch (Exception ex){}

    }

    /**
     * #5. Localities API for id 69r+o9VWkKkuWNWdQSBY96oQvTk=
     */
    @Test
    public void testDetailLocalitieWiganRoad(){
        multiSearch.removeAllProviders();
        final TestSearchListener multiSearchListener = new TestSearchListener();

        ProviderConfig.Builder builder;

        builder = new ProviderConfig.Builder(SearchProviderType.LOCALITIES)
                .key(apiKey)
                .component(new Component(new String[]{"gb"})).searchType("address").searchType("postal_code").language("en");

        multiSearch.addProvider(builder.build());//Add localities provider

        multiSearch.addSearchListener(multiSearchListener);
        multiSearch.detailsLocalities("69r+o9VWkKkuWNWdQSBY96oQvTk=");
        synchronized (multiSearchListener){
            try {
                multiSearchListener.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        if (multiSearchListener.exception==null){
            Assert.assertEquals("Expected formated address","Wigan Road Farm, Wigan Road, Golborne, Warrington, WA3 3UF",multiSearchListener.detailsResponseItem.getFormattedAddress());
        }
        else{
            Assert.fail(multiSearchListener.exception.toString());
        }
    }

    /***
     * Crosscheck #5 - Wigan Road Farm, Wigan Road, Golborne, Warrington, WA3 3UF
     */
    @Test
    public void testDetailLocalitieWiganRoadAutocomplete(){
        multiSearch.removeAllProviders();
        final TestSearchListener multiSearchListener = new TestSearchListener();

        ProviderConfig.Builder builder;

        builder = new ProviderConfig.Builder(SearchProviderType.LOCALITIES)
                .key(apiKey)
                .component(new Component(new String[]{"gb"})).searchType("address").searchType("postal_code").language("en");

        multiSearch.addProvider(builder.build());//Add localities provider

        multiSearch.addSearchListener(multiSearchListener);

        multiSearch.autocompleteLocalities("Wigan Road Farm, Wigan Road, Golborne, Warrington, WA3 3UF");
        try{
            synchronized (multiSearchListener){
                multiSearchListener.wait();
            }
        }catch (Exception ex){}
        if (multiSearchListener.exception!=null){
            Assert.fail(multiSearchListener.exception.toString());
        }
        Assert.assertEquals("expectedID","69r+o9VWkKkuWNWdQSBY96oQvTk=",multiSearchListener.items.get(0).getId());
    }

    /***
     * #6. Check store details for id 8ad198b442c766280142c76629be00a5
     */
    @Test
    public void testDetailStore(){
        multiSearch.removeAllProviders();
        final TestSearchListener multiSearchListener = new TestSearchListener();

        ProviderConfig.Builder builder;

        builder = new ProviderConfig.Builder(SearchProviderType.STORE)
                .key(apiKey).query("type:type1");

        multiSearch.addProvider(builder.build());//Add store provider

        multiSearch.addSearchListener(multiSearchListener);
        multiSearch.detailsStore("10008_98261");

        synchronized (multiSearchListener){
            try {
                multiSearchListener.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        if (multiSearchListener.exception==null){
            Assert.assertEquals("Expected formated address","1385 East Vista Way",multiSearchListener.detailsResponseItem.getFormattedAddress());
        }
        else{
            Assert.fail(multiSearchListener.exception.toString());
        }
    }

    /***
     * Cross check #6
     */
    @Test
    public void testDetailStoreAutocomplete(){
        multiSearch.removeAllProviders();
        final TestSearchListener multiSearchListener = new TestSearchListener();

        ProviderConfig.Builder builder;

        builder = new ProviderConfig.Builder(SearchProviderType.STORE)
                .key(apiKey)
                .query("type:type1");

        multiSearch.addProvider(builder.build());//Add store provider

        multiSearch.addSearchListener(multiSearchListener);
        multiSearch.autocompleteStore("Vista & Foothill, Vista");
        synchronized (multiSearchListener){
            try{
                multiSearchListener.wait();
            }
            catch (Exception ex){}
        }
        if (multiSearchListener.exception!=null){
            Assert.fail(multiSearchListener.exception.toString());
        }
        Assert.assertEquals("expectedID","10008_98261",multiSearchListener.items.get(0).getId());
    }

    /***
     * #7. Get Address details for id VmlhIERvbWl0aWFuYSwgODAwNzggUG96enVvbGkgTkEsIEl0YWxpYQ==
     */
    @Test
    public void testDetailAddress(){
        multiSearch.removeAllProviders();
        final TestSearchListener multiSearchListener = new TestSearchListener();

        ProviderConfig.Builder builder;

        builder = new ProviderConfig.Builder(SearchProviderType.ADDRESS)
                .key(apiKey)
                .language("it")
                .component(new Component(new String[]{"it"}));

        multiSearch.addProvider(builder.build());//Add address provider

        multiSearch.addSearchListener(multiSearchListener);
        multiSearch.detailsAddress("aGVyZTphZjpzdHJlZXQ6WVlhYnBWdEtON3RnSmx2YzlOdEZ0Qw==");
        synchronized (multiSearchListener){
            try {
                multiSearchListener.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        if (multiSearchListener.exception==null){
            Assert.assertEquals("Expected formated address","Via Domitiana, 80078 Pozzuoli NA, Italia",multiSearchListener.detailsResponseItem.getFormattedAddress());
        }
        else{
            Assert.fail(multiSearchListener.exception.toString());
        }
    }

    /***
     * Cross check #7. Via Domitiana, 80078 Pozzuoli NA, Italia
     */
    @Test
    public void testDetailAddressAutocomplete(){
        multiSearch.removeAllProviders();
        final TestSearchListener multiSearchListener = new TestSearchListener();

        ProviderConfig.Builder builder;

        builder = new ProviderConfig.Builder(SearchProviderType.ADDRESS)
                .key(apiKey)
                .language("it")
                .component(new Component(new String[]{"it"}));

        multiSearch.addProvider(builder.build());//Add address provider

        multiSearch.addSearchListener(multiSearchListener);
        multiSearch.autocompleteAddress("Via Domitiana, 80078 Pozzuoli NA, Italia");
        synchronized (multiSearchListener){
            try {
                multiSearchListener.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        if (multiSearchListener.exception==null){
            Assert.assertEquals("expectedID","aGVyZTphZjpzdHJlZXQ6WVlhYnBWdEtON3RnSmx2YzlOdEZ0Qw==",multiSearchListener.items.get(0).getId());
        }
        else{
            Assert.fail(multiSearchListener.exception.toString());
        }
    }
}
