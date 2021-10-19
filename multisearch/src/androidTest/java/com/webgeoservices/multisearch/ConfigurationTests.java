 package com.webgeoservices.multisearch;

import android.util.Log;

import com.webgeoservices.multisearch.configs.Component;
import com.webgeoservices.multisearch.configs.ProviderConfig;
import com.webgeoservices.multisearch.listeners.MultiSearchListener;
import com.webgeoservices.multisearch.searchdatamodels.AutocompleteResponseItem;
import com.webgeoservices.multisearch.searchdatamodels.DetailsResponseItem;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import androidx.test.platform.app.InstrumentationRegistry;

/***
 * Tests SDK configuration with valid and invalid values and check the expected behaviour.
 */
public class ConfigurationTests {

    private String apiKey;
    private String googleKey;
    private MultiSearch multiSearch;
    private int searchParamCounter =0;

    @Before
    public void setUp(){
        apiKey = InstrumentationRegistry.getInstrumentation().getTargetContext().getString(R.string.__wgs_woosemap_private_key);
        googleKey = InstrumentationRegistry.getInstrumentation().getTargetContext().getString(R.string.__wgs_google_api_key);
        multiSearch = new MultiSearch(InstrumentationRegistry.getInstrumentation().getTargetContext());
    }

    @Test
    public void test_NullApiKey(){
        multiSearch.setDebounceTime(100);
        multiSearch.removeAllProviders();

        ProviderConfig.Builder builder;
        try{

            builder = new ProviderConfig.Builder(SearchProviderType.LOCALITIES)
                    .key(null)
                    .fallbackBreakpoint(0.4f)
                    .component(new Component(new String[]{null}))
                    .language(null)
                    .searchType(null)
                    .searchType(null);
            multiSearch.addProvider(builder.build());//Add locality provider

            multiSearch.addSearchListener(new MultiSearchListener() {
                @Override
                public void onSearchComplete(List<AutocompleteResponseItem> searchResult, WoosmapException exception) {
                    searchResult.toString();
                }

                @Override
                public void onDetailComplete(DetailsResponseItem detailResult, WoosmapException exception) {

                }
            });

            multiSearch.autocompleteMulti("Montp");
            Assert.fail("Runtime error should have occurred");
        }
        catch (Exception ex){
            Log.e(ConfigurationTests.class.getName(),ex.toString());
        }
    }

    @Test
    public void test_NullConfigParams_Localities(){
        multiSearch.setDebounceTime(100);
        multiSearch.removeAllProviders();
        final TestSearchListener multiSearchListener = new TestSearchListener();

        ProviderConfig.Builder builder;

        builder = new ProviderConfig.Builder(SearchProviderType.LOCALITIES)
                .key(apiKey);
        multiSearch.addProvider(builder.build());//Add locality provider

        multiSearch.addSearchListener(multiSearchListener);

        multiSearch.autocompleteMulti("Montp");
        synchronized (multiSearchListener){
            try{
                multiSearchListener.wait();
            }
            catch (Exception ex){}
        }
        if (multiSearchListener.exception!=null){
            Assert.fail(multiSearchListener.exception.toString());
        }
        if (multiSearchListener.items.size() == 0){
            Assert.fail("No result");
        }
        Assert.assertEquals("Expected result count: ", 5,multiSearchListener.items.size());
        Assert.assertEquals("Expected first result: ", "Montpelier, Vermont, United States",multiSearchListener.items.get(0).getDescription());
    }

    @Test
    public void test_NullConfigParams_Places(){
        multiSearch.setDebounceTime(100);
        multiSearch.removeAllProviders();
        final TestSearchListener multiSearchListener = new TestSearchListener();

        ProviderConfig.Builder builder;

        builder = new ProviderConfig.Builder(SearchProviderType.PLACES)
                .key(googleKey);
        multiSearch.addProvider(builder.build());//Add locality provider

        multiSearch.addSearchListener(multiSearchListener);
        multiSearch.autocompleteMulti("Montp");
        synchronized (multiSearchListener){
            try{
                multiSearchListener.wait();
            }
            catch (Exception ex){}
        }
        if (multiSearchListener.exception!=null){
            Assert.fail(multiSearchListener.exception.toString());
        }
        if (multiSearchListener.items.size() == 0){
            Assert.fail("No result");
        }
        Assert.assertEquals("Expected result count: ", 5,multiSearchListener.items.size());
        Assert.assertEquals("Expected first result: ", "Montpellier, France",multiSearchListener.items.get(0).getDescription());
    }

    @Test
    public void test_NullConfigParams_Store(){
        multiSearch.setDebounceTime(100);
        multiSearch.removeAllProviders();
        final TestSearchListener multiSearchListener = new TestSearchListener();
        ProviderConfig.Builder builder;

        builder = new ProviderConfig.Builder(SearchProviderType.STORE)
                .key(apiKey);
        multiSearch.addProvider(builder.build());//Add locality provider

        multiSearch.addSearchListener(multiSearchListener);

        multiSearch.autocompleteMulti("outlet");
        synchronized (multiSearchListener){
            try{
                multiSearchListener.wait();
            }
            catch (Exception ex){}
        }
        if (multiSearchListener.exception!=null){
            Assert.fail(multiSearchListener.exception.toString());
        }
        if (multiSearchListener.items.size() == 0){
            Assert.fail("No result");
        }
        Assert.assertEquals("Expected result count: ", 5,multiSearchListener.items.size());
        Assert.assertEquals("Expected first result: ", "OUTLET SWINDON",multiSearchListener.items.get(0).getDescription());
    }

    @Test
    public void test_NullConfigParams_Address(){
        multiSearch.setDebounceTime(100);
        multiSearch.removeAllProviders();

        ProviderConfig.Builder builder;

        builder = new ProviderConfig.Builder(SearchProviderType.ADDRESS)
                .key(apiKey);
        multiSearch.addProvider(builder.build());//Add locality provider


        multiSearch.addSearchListener(new MultiSearchListener() {
            @Override
            public void onSearchComplete(List<AutocompleteResponseItem> searchResult, WoosmapException exception) {
                Assert.assertEquals("Expected result count: ", 4,searchResult.size());
                Assert.assertEquals("Expected first result: ", "Montpellier, Occitanie, France",searchResult.get(0).getDescription());
            }

            @Override
            public void onDetailComplete(DetailsResponseItem detailResult, WoosmapException exception) {

            }
        });

        multiSearch.autocompleteMulti("Montp");
    }

    @Test
    public void test_InvalidLocalitiesApiKey(){
        multiSearch.setDebounceTime(100);
        multiSearch.removeAllProviders();
        final TestSearchListener multiSearchListener = new TestSearchListener();

        ProviderConfig.Builder builder;
        builder = new ProviderConfig.Builder(SearchProviderType.LOCALITIES)
                .key("1234567")
                .component(new Component(new String[]{"fr"}))
                .language("fr");
        multiSearch.addProvider(builder.build());//Add locality provider
        multiSearch.addSearchListener(multiSearchListener);

        multiSearch.autocompleteMulti("Montp");
        synchronized (multiSearchListener){
            try{
                multiSearchListener.wait();
            }
            catch (Exception ex){}
        }
        if (multiSearchListener.exception==null){
            Assert.fail("Invalid API Key expection should have been raised");
        }
    }

    @Test
    public void test_InvalidAddressApiKey(){
        multiSearch.setDebounceTime(100);
        multiSearch.removeAllProviders();
        final TestSearchListener multiSearchListener = new TestSearchListener();

        ProviderConfig.Builder builder;
        builder = new ProviderConfig.Builder(SearchProviderType.ADDRESS)
                .key("1234567")
                .component(new Component(new String[]{"fr"}))
                .language("fr");
        multiSearch.addProvider(builder.build());//Add locality provider
        multiSearch.addSearchListener(multiSearchListener);
        multiSearch.autocompleteMulti("Montp");
        synchronized (multiSearchListener){
            try{
                multiSearchListener.wait();
            }
            catch (Exception ex){}
        }
        if (multiSearchListener.exception==null){
            Assert.fail("Invalid API Key expection should have been raised");
        }
    }

    @Test
    public void test_InvalidStoreApiKey(){
        multiSearch.setDebounceTime(100);
        multiSearch.removeAllProviders();
        final TestSearchListener multiSearchListener = new TestSearchListener();

        ProviderConfig.Builder builder;
        builder = new ProviderConfig.Builder(SearchProviderType.STORE)
                .key("1234567")
                .component(new Component(new String[]{"fr"}))
                .language("fr");
        multiSearch.addProvider(builder.build());//Add locality provider

        multiSearch.addSearchListener(multiSearchListener);

        multiSearch.autocompleteMulti("Montp");
        synchronized (multiSearchListener){
            try{
                multiSearchListener.wait();
            }
            catch (Exception ex){}
        }
        if (multiSearchListener.exception==null){
            Assert.fail("Invalid API Key expection should have been raised");
        }
    }

    @Test
    public void test_InvalidPlacesApiKey(){
        multiSearch.setDebounceTime(100);
        multiSearch.removeAllProviders();
        final TestSearchListener multiSearchListener = new TestSearchListener();

        ProviderConfig.Builder builder;
        builder = new ProviderConfig.Builder(SearchProviderType.PLACES)
                .key("1234567")
                .component(new Component(new String[]{"fr"}))
                .language("fr");
        multiSearch.addProvider(builder.build());//Add locality provider
        multiSearch.addSearchListener(multiSearchListener);

        multiSearch.autocompleteMulti("Montp");
        synchronized (multiSearchListener){
            try{
                multiSearchListener.wait();
            }
            catch (Exception ex){}
        }
        if (multiSearchListener.exception==null){
            Assert.fail("Invalid API Key expection should have been raised");
        }
    }

    @Test
    public void test_PlaceDetailInvalidApiKey(){
        multiSearch.setDebounceTime(100);
        multiSearch.removeAllProviders();
        final TestSearchListener multiSearchListener = new TestSearchListener();

        ProviderConfig.Builder builder;
        builder = new ProviderConfig.Builder(SearchProviderType.PLACES)
                .key("1234567")
                .component(new Component(new String[]{"fr"}))
                .language("fr");
        multiSearch.addProvider(builder.build());//Add locality provider
        multiSearch.addSearchListener(multiSearchListener);

        multiSearch.detailsPlaces("ChIJEW4ls3nVwkcRYGNkgT7xCgQ");
        synchronized (multiSearchListener){
            try{
                multiSearchListener.wait();
            }
            catch (Exception ex){}
        }
        if (multiSearchListener.exception==null){
            Assert.fail("Invalid API Key expection should have been raised");
        }
    }

    @Test
    public void test_AddressDetailInvalidApiKey(){
        multiSearch.setDebounceTime(100);
        multiSearch.removeAllProviders();
        final TestSearchListener multiSearchListener = new TestSearchListener();

        ProviderConfig.Builder builder;
        builder = new ProviderConfig.Builder(SearchProviderType.ADDRESS)
                .key("1234567")
                .component(new Component(new String[]{"fr"}))
                .language("fr");
        multiSearch.addProvider(builder.build());//Add locality provider
        multiSearch.addSearchListener(multiSearchListener);

        multiSearch.detailsAddress("VmlhIERvbWl0aWFuYSwgODAwNzggUG96enVvbGkgTkEsIEl0YWxpYQ==");
        synchronized (multiSearchListener){
            try{
                multiSearchListener.wait();
            }
            catch (Exception ex){}
        }
        if (multiSearchListener.exception==null){
            Assert.fail("Invalid API Key expection should have been raised");
        }
    }

    @Test
    public void test_StoreDetailInvalidApiKey(){
        multiSearch.setDebounceTime(100);
        multiSearch.removeAllProviders();
        final TestSearchListener multiSearchListener = new TestSearchListener();

        ProviderConfig.Builder builder;
        builder = new ProviderConfig.Builder(SearchProviderType.STORE)
                .key("1234567")
                .component(new Component(new String[]{"fr"}))
                .language("fr");
        multiSearch.addProvider(builder.build());//Add locality provider
        multiSearch.addSearchListener(multiSearchListener);

        multiSearch.detailsStore("8ad198b442c766280142c76629be00a5");
        synchronized (multiSearchListener){
            try{
                multiSearchListener.wait();
            }
            catch (Exception ex){}
        }
        if (multiSearchListener.exception==null){
            Assert.fail("Invalid API Key expection should have been raised");
        }
    }

    @Test
    public void test_LocalitiesDetailInvalidApiKey(){
        multiSearch.setDebounceTime(100);
        multiSearch.removeAllProviders();
        final TestSearchListener multiSearchListener = new TestSearchListener();

        ProviderConfig.Builder builder;
        builder = new ProviderConfig.Builder(SearchProviderType.LOCALITIES)
                .key("1234567")
                .component(new Component(new String[]{"fr"}))
                .language("fr");
        multiSearch.addProvider(builder.build());//Add locality provider
        multiSearch.addSearchListener(multiSearchListener);
        multiSearch.detailsLocalities("vWA87ZwgR3V1RMRpY0QXZEGQoFk=");
        synchronized (multiSearchListener){
            try{
                multiSearchListener.wait();
            }
            catch (Exception ex){}
        }
        if (multiSearchListener.exception==null){
            Assert.fail("Invalid API Key expection should have been raised");
        }
    }

    @Test
    public void test_SpecialCharactersInLocalitiesConfig(){
        multiSearch.setDebounceTime(100);
        multiSearch.removeAllProviders();
        final TestSearchListener multiSearchListener = new TestSearchListener();

        ProviderConfig.Builder builder;

        builder = new ProviderConfig.Builder(SearchProviderType.LOCALITIES)
                .key(apiKey)
                .fallbackBreakpoint(0.4f)
                .component(new Component(new String[]{"#&:"}))
                .language("#&:")
                .searchType("#&:")
                .searchType("#&:");
        multiSearch.addProvider(builder.build());//Add locality provider


        multiSearch.addSearchListener(multiSearchListener);
        multiSearch.autocompleteMulti("23 rue de");
        synchronized (multiSearchListener){
            try {
                multiSearchListener.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        if (multiSearchListener.exception==null){
            Assert.fail("Internal error should have been thrown");
        }
    }

    @Test
    public void test_SpecialCharactersInAddressConfig(){
        multiSearch.setDebounceTime(100);
        multiSearch.removeAllProviders();
        final TestSearchListener multiSearchListener = new TestSearchListener();

        ProviderConfig.Builder builder;

        builder = new ProviderConfig.Builder(SearchProviderType.ADDRESS)
                .key(apiKey)
                .fallbackBreakpoint(0.5f)
                .minInputLength(5)
                .component(new Component(new String[]{"#$%&%!*^"}))
                .language("#$%&%!*^");
        multiSearch.addProvider(builder.build());//Add Address provider

        multiSearch.addSearchListener(multiSearchListener);
        multiSearch.autocompleteMulti("23 rue de");
        synchronized (multiSearchListener){
            try {
                multiSearchListener.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        if (multiSearchListener.exception==null){
            Assert.fail("Internal error should have been thrown");
        }

    }

    @Test
    public void test_SpecialCharactersInStoreConfig(){
        multiSearch.setDebounceTime(100);
        multiSearch.removeAllProviders();
        final TestSearchListener multiSearchListener = new TestSearchListener();

        ProviderConfig.Builder builder;

        builder = new ProviderConfig.Builder(SearchProviderType.STORE)
                .key(apiKey)
                .fallbackBreakpoint(0.5f)
                .minInputLength(5)
                .query("type:!@#$%^&*()bose_store");
        multiSearch.addProvider(builder.build());//Add Store provider

        multiSearch.addSearchListener(multiSearchListener);
        multiSearch.autocompleteMulti("23 rue de");
        synchronized (multiSearchListener){
            try {
                multiSearchListener.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        if (multiSearchListener.exception==null){
            Assert.fail("Internal error should have been thrown");
        }

    }

    @Test
    public void test_SpecialCharactersInPlacesConfig(){
        multiSearch.setDebounceTime(100);
        multiSearch.removeAllProviders();
        final TestSearchListener multiSearchListener = new TestSearchListener();

        ProviderConfig.Builder builder;

        builder = new ProviderConfig.Builder(SearchProviderType.PLACES)
                .key(googleKey)
                .fallbackBreakpoint(1.0f)
                .minInputLength(8)
                .component(new Component(new String[]{"!@#$%^&*()"}))
                .language("!@#$%^&*()");
        multiSearch.addProvider(builder.build());//Add places provider

        multiSearch.addSearchListener(multiSearchListener);
        multiSearch.autocompleteMulti("23 rue de");
        synchronized (multiSearchListener){
            try {
                multiSearchListener.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        if (multiSearchListener.exception==null){
            Assert.fail("Internal error should have been thrown");
        }

    }

    @Test
    public void test_MinimumInputLength(){
        multiSearch.setDebounceTime(100);
        multiSearch.removeAllProviders();
        TestSearchListener multiSearchListener = new TestSearchListener();

        ProviderConfig.Builder builder;

        builder = new ProviderConfig.Builder(SearchProviderType.STORE)
                .key(apiKey)
                .ignoreFallbackBreakpoint(true)
                .minInputLength(10)
                .query("type:bose_store");
        multiSearch.addProvider(builder.build());//Add store provider

        builder = new ProviderConfig.Builder(SearchProviderType.LOCALITIES)
                .key(apiKey)
                .ignoreFallbackBreakpoint(true)
                .minInputLength(10)
                .component(new Component(new String[]{"gb"}))
                .language("gb")
                .searchType("locality")
                .searchType("postal_code");
        multiSearch.addProvider(builder.build());//Add locality provider

        builder = new ProviderConfig.Builder(SearchProviderType.ADDRESS)
                .key(apiKey)
                .ignoreFallbackBreakpoint(true)
                .minInputLength(10)
                .component(new Component(new String[]{"gb"}))
                .language("gb");
        multiSearch.addProvider(builder.build());//Add Address provider

        builder = new ProviderConfig.Builder(SearchProviderType.PLACES)
                .key(googleKey)
                .ignoreFallbackBreakpoint(true)
                .minInputLength(10)
                .component(new Component(new String[]{"gb"}));
        multiSearch.addProvider(builder.build());//Add places provider

        multiSearch.addSearchListener(multiSearchListener);

        multiSearch.autocompleteMulti("23 rue");

        synchronized (multiSearchListener){
            try {
                multiSearchListener.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        if (multiSearchListener.items.size()>0){
            Assert.fail("Search should have returned 0 results");
        }

    }

}
