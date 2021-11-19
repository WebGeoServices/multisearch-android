package com.webgeoservices.multisearch;

import android.util.Log;

import com.webgeoservices.multisearch.configs.Component;
import com.webgeoservices.multisearch.configs.Data;
import com.webgeoservices.multisearch.configs.ProviderConfig;
import com.webgeoservices.multisearch.searchdatamodels.AutocompleteResponseItem;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

/***
 * Autocomplete unit tests defined by Leo in Test scenarios Google Sheet
 */
@RunWith(AndroidJUnit4.class)
public class AutocompleteTests {

    private String apiKey;
    private String googleKey;
    private MultiSearch multiSearch;
    private int searchParamCounter =0;

    @Before
    public void setUp(){
        apiKey =   InstrumentationRegistry.getInstrumentation().getTargetContext().getString(R.string.__wgs_woosemap_private_key);
        googleKey = InstrumentationRegistry.getInstrumentation().getTargetContext().getString(R.string.__wgs_google_api_key);
        multiSearch = new MultiSearch(InstrumentationRegistry.getInstrumentation().getTargetContext());
    }

    /***
     * #1. Localities, Address and Places
     */
    @Test
    public void test_Localities_Address_Places(){
        ArrayList<UnitTestParams> unitTestParams=new ArrayList<>();
        searchParamCounter =0;
        multiSearch.setDebounceTime(100);
        multiSearch.removeAllProviders();
        final TestSearchListener multiSearchListener = new TestSearchListener();

        ProviderConfig.Builder builder;

        builder = new ProviderConfig.Builder(SearchProviderType.LOCALITIES)
                .key(apiKey)
                .fallbackBreakpoint(0.4f)
                .component(new Component(new String[]{"FR"}))
                .language("fr")
                .searchType("locality")
                .searchType("postal_code");
        multiSearch.addProvider(builder.build());//Add locality provider

        builder = new ProviderConfig.Builder(SearchProviderType.ADDRESS)
                .key(apiKey)
                .fallbackBreakpoint(0.5f)
                .minInputLength(5)
                .component(new Component(new String[]{"fr"}))
                .language("fr");
        multiSearch.addProvider(builder.build());//Add Address provider

        builder = new ProviderConfig.Builder(SearchProviderType.PLACES)
                .key(googleKey)
                .fallbackBreakpoint(1.0f)
                .minInputLength(8)
                .component(new Component(new String[]{"fr"}));
        multiSearch.addProvider(builder.build());//Add places provider

        multiSearch.addSearchListener(multiSearchListener);

        unitTestParams.add(new UnitTestParams("23 rue de","23 Rue Delizy, 93500 Pantin, France",5,SearchProviderType.ADDRESS,SearchProviderType.ADDRESS));
        unitTestParams.add(new UnitTestParams("Montp","Montpellier, Hérault, France",5,SearchProviderType.LOCALITIES,SearchProviderType.LOCALITIES));
        unitTestParams.add(new UnitTestParams("Disneyla","Disneyland Paris, Boulevard de Parc, Coupvray, France",5,SearchProviderType.PLACES,SearchProviderType.PLACES));
        unitTestParams.add(new UnitTestParams("34170","34170, Castelnau-le-Lez, France",5,SearchProviderType.LOCALITIES,SearchProviderType.LOCALITIES));

        for(UnitTestParams params: unitTestParams){
            try{
                multiSearchListener.items = new ArrayList<>();
                multiSearchListener.exception = null;

                multiSearch.autocompleteMulti(params.intput);
                synchronized (multiSearchListener){
                    multiSearchListener.wait();
                }
                if (multiSearchListener.exception!=null){
                    Assert.fail(multiSearchListener.exception.toString());
                }

                if (multiSearchListener.items.size() == 0){
                    Assert.fail("No result");
                }
                Assert.assertEquals("Total Results: ",unitTestParams.get(searchParamCounter).expectedResultCount,multiSearchListener.items.size());
                Assert.assertEquals("Expected First Result: ",unitTestParams.get(searchParamCounter).expectedFirstResult,multiSearchListener.items.get(0).getDescription());
                Assert.assertEquals("Expected First Api: ",unitTestParams.get(searchParamCounter).expectedFirstApi,multiSearchListener.items.get(0).getApi());
                Assert.assertEquals("Expected Last Api: ",unitTestParams.get(searchParamCounter).expectedLastApi,multiSearchListener.items.get(multiSearchListener.items.size()-1).getApi());

                searchParamCounter++;
            }
            catch (Exception ex){
                Log.e(AutocompleteTests.class.getName(),ex.toString());
            }
        }
    }

    /***
     * #7. Store, Localities, Address and Places - Store's fallbackBreakpoint = false
     */
    @Test
    public void test_Store_Localities_Address_Places_store_fallbackBreakpoint_is_false(){
        ArrayList<UnitTestParams> unitTestParams=new ArrayList<>();
        searchParamCounter =0;
        multiSearch.setDebounceTime(100);
        multiSearch.removeAllProviders();
        final TestSearchListener multiSearchListener = new TestSearchListener();

        ProviderConfig.Builder builder;

        builder = new ProviderConfig.Builder(SearchProviderType.STORE)
                .key(apiKey)
                .ignoreFallbackBreakpoint(true)
                .query("type:type1");
        multiSearch.addProvider(builder.build());//Add store provider

        builder = new ProviderConfig.Builder(SearchProviderType.LOCALITIES)
                .key(apiKey)
                .fallbackBreakpoint(0.4f)
                .component(new Component(new String[]{"gb"}))
                .language("gb")
                .searchType("locality")
                .searchType("postal_code");
        multiSearch.addProvider(builder.build());//Add locality provider

        builder = new ProviderConfig.Builder(SearchProviderType.ADDRESS)
                .key(apiKey)
                .fallbackBreakpoint(0.5f)
                .minInputLength(5)
                .component(new Component(new String[]{"gb"}))
                .language("gb");
        multiSearch.addProvider(builder.build());//Add Address provider

        builder = new ProviderConfig.Builder(SearchProviderType.PLACES)
                .key(googleKey)
                .fallbackBreakpoint(1.0f)
                .minInputLength(8)
                .component(new Component(new String[]{"gb"}));
        multiSearch.addProvider(builder.build());//Add places provider

        multiSearch.addSearchListener(multiSearchListener);

        unitTestParams.add(new UnitTestParams("vista","Vista & Foothill, Vista",6,SearchProviderType.STORE,SearchProviderType.ADDRESS));
        unitTestParams.add(new UnitTestParams("Lanc","Lancaster, Lancashire, United Kingdom",5,SearchProviderType.LOCALITIES,SearchProviderType.LOCALITIES));

        for(UnitTestParams params: unitTestParams){
            try{
                multiSearchListener.items = new ArrayList<>();
                multiSearchListener.exception = null;

                multiSearch.autocompleteMulti(params.intput);
                synchronized (multiSearchListener){
                    multiSearchListener.wait();
                }
                if (multiSearchListener.exception!=null){
                    Assert.fail(multiSearchListener.exception.toString());
                }

                if (multiSearchListener.items.size() == 0){
                    Assert.fail("No result");
                }


                Assert.assertEquals("Total Results: ",unitTestParams.get(searchParamCounter).expectedResultCount,multiSearchListener.items.size());
                Assert.assertEquals("Expected First Result: ",unitTestParams.get(searchParamCounter).expectedFirstResult,multiSearchListener.items.get(0).getDescription());
                Assert.assertEquals("Expected First Api: ",unitTestParams.get(searchParamCounter).expectedFirstApi,multiSearchListener.items.get(0).getApi());
                Assert.assertEquals("Expected Last Api: ",unitTestParams.get(searchParamCounter).expectedLastApi,multiSearchListener.items.get(multiSearchListener.items.size()-1).getApi());
                searchParamCounter++;
            }
            catch (Exception ex){
                Log.e(AutocompleteTests.class.getName(),ex.toString());
            }
        }
    }

    /***
     * #11. Localities, Address, Places and Store - Store's fallbackBreakpoint = false
     */
    @Test
    public void test_Localities_Address_Places_store_store_fallbackBreakpoint_is_false(){
        ArrayList<UnitTestParams> unitTestParams=new ArrayList<>();
        searchParamCounter =0;
        multiSearch.setDebounceTime(100);
        multiSearch.removeAllProviders();
        final TestSearchListener multiSearchListener = new TestSearchListener();

        ProviderConfig.Builder builder;

        builder = new ProviderConfig.Builder(SearchProviderType.LOCALITIES)
                .key(apiKey)
                .fallbackBreakpoint(0.4f)
                .component(new Component(new String[]{"gb"}))
                .language("gb")
                .searchType("locality")
                .searchType("postal_code");
        multiSearch.addProvider(builder.build());//Add locality provider

        builder = new ProviderConfig.Builder(SearchProviderType.ADDRESS)
                .key(apiKey)
                .fallbackBreakpoint(0.5f)
                .minInputLength(5)
                .component(new Component(new String[]{"gb"}))
                .language("gb");
        multiSearch.addProvider(builder.build());//Add Address provider

        builder = new ProviderConfig.Builder(SearchProviderType.PLACES)
                .key(googleKey)
                .fallbackBreakpoint(1.0f)
                .minInputLength(8)
                .component(new Component(new String[]{"gb"}));
        multiSearch.addProvider(builder.build());//Add places provider
        builder = new ProviderConfig.Builder(SearchProviderType.STORE)
                .key(apiKey)
                .ignoreFallbackBreakpoint(true)
                .searchType("type1");
        multiSearch.addProvider(builder.build());//Add store provider

        multiSearch.addSearchListener(multiSearchListener);

        unitTestParams.add(new UnitTestParams("She","Sheffield, South Yorkshire, United Kingdom",10,SearchProviderType.LOCALITIES,SearchProviderType.STORE));
        unitTestParams.add(new UnitTestParams("United Ki","Hull, City of Kingston upon Hull, United Kingdom",5,SearchProviderType.LOCALITIES,SearchProviderType.LOCALITIES));

        for(UnitTestParams params: unitTestParams){
            try{
                multiSearchListener.items = new ArrayList<>();
                multiSearchListener.exception = null;

                multiSearch.autocompleteMulti(params.intput);
                synchronized (multiSearchListener){
                    multiSearchListener.wait();
                }
                if (multiSearchListener.exception!=null){
                    Assert.fail(multiSearchListener.exception.toString());
                }

                if (multiSearchListener.items.size() == 0){
                    Assert.fail("No result");
                }

                Assert.assertEquals("Total Results: ",unitTestParams.get(searchParamCounter).expectedResultCount,multiSearchListener.items.size());
                Assert.assertEquals("Expected First Result: ",unitTestParams.get(searchParamCounter).expectedFirstResult,multiSearchListener.items.get(0).getDescription());
                Assert.assertEquals("Expected First Api: ",unitTestParams.get(searchParamCounter).expectedFirstApi,multiSearchListener.items.get(0).getApi());
                Assert.assertEquals("Expected Last Api: ",unitTestParams.get(searchParamCounter).expectedLastApi,multiSearchListener.items.get(multiSearchListener.items.size()-1).getApi());
                searchParamCounter++;
            }
            catch (Exception ex){
                Log.e(AutocompleteTests.class.getName(),ex.toString());
            }
        }
    }

    /***
     * #15. Localities, Address and Places - Localities's minInputLength = 3
     */
    @Test
    public void test_Localities_Address_Places_Localities_minInputLength_is_3(){
        ArrayList<UnitTestParams> unitTestParams=new ArrayList<>();
        searchParamCounter =0;
        multiSearch.setDebounceTime(100);
        multiSearch.removeAllProviders();
        final TestSearchListener multiSearchListener = new TestSearchListener();

        ProviderConfig.Builder builder;

        builder = new ProviderConfig.Builder(SearchProviderType.LOCALITIES)
                .key(apiKey)
                .fallbackBreakpoint(0.4f)
                .minInputLength(3)
                .component(new Component(new String[]{"fr"}))
                .language("fr")
                .searchType("locality")
                .searchType("postal_code");
        multiSearch.addProvider(builder.build());//Add locality provider

        builder = new ProviderConfig.Builder(SearchProviderType.ADDRESS)
                .key(apiKey)
                .fallbackBreakpoint(0.5f)
                .minInputLength(5)
                .component(new Component(new String[]{"fr"}))
                .language("fr");
        multiSearch.addProvider(builder.build());//Add Address provider

        builder = new ProviderConfig.Builder(SearchProviderType.PLACES)
                .key(googleKey)
                .fallbackBreakpoint(1.0f)
                .minInputLength(8)
                .component(new Component(new String[]{"fr"}));
        multiSearch.addProvider(builder.build());//Add places provider

        multiSearch.addSearchListener(multiSearchListener);

        unitTestParams.add(new UnitTestParams("Montp","Montpellier, Hérault, France",5,SearchProviderType.LOCALITIES,SearchProviderType.LOCALITIES));
        unitTestParams.add(new UnitTestParams("340","34070, Montpellier, France",4,SearchProviderType.LOCALITIES,SearchProviderType.LOCALITIES));

        for(UnitTestParams params: unitTestParams){
            try{
                multiSearchListener.items = new ArrayList<>();
                multiSearchListener.exception = null;

                multiSearch.autocompleteMulti(params.intput);
                synchronized (multiSearchListener){
                    multiSearchListener.wait();
                }
                if (multiSearchListener.exception!=null){
                    Assert.fail(multiSearchListener.exception.toString());
                }

                if (multiSearchListener.items.size() == 0){
                    Assert.fail("No result");
                }

                Assert.assertEquals("Total Results: ",unitTestParams.get(searchParamCounter).expectedResultCount,multiSearchListener.items.size());
                Assert.assertEquals("Expected First Result: ",unitTestParams.get(searchParamCounter).expectedFirstResult,multiSearchListener.items.get(0).getDescription());
                Assert.assertEquals("Expected First Api: ",unitTestParams.get(searchParamCounter).expectedFirstApi,multiSearchListener.items.get(0).getApi());
                Assert.assertEquals("Expected Last Api: ",unitTestParams.get(searchParamCounter).expectedLastApi,multiSearchListener.items.get(multiSearchListener.items.size()-1).getApi());

                searchParamCounter++;
            }
            catch (Exception ex){
                Log.e(AutocompleteTests.class.getName(),ex.toString());
            }
        }
    }

    /***
     * #19. Localities, Address and Places - Localities' type = metro_station (only)
     */
    @Test
    public void test_Localities_Address_Places_Localities_type_metro_station(){
        ArrayList<UnitTestParams> unitTestParams=new ArrayList<>();
        searchParamCounter =0;
        multiSearch.setDebounceTime(100);
        multiSearch.removeAllProviders();
        final TestSearchListener multiSearchListener = new TestSearchListener();

        ProviderConfig.Builder builder;

        builder = new ProviderConfig.Builder(SearchProviderType.LOCALITIES)
                .key(apiKey)
                .fallbackBreakpoint(0.4f)
                .minInputLength(3)
                .component(new Component(new String[]{"fr"}))
                .language("fr")
                .searchType("metro_station");
        multiSearch.addProvider(builder.build());//Add locality provider

        builder = new ProviderConfig.Builder(SearchProviderType.ADDRESS)
                .key(apiKey)
                .fallbackBreakpoint(0.5f)
                .minInputLength(5)
                .component(new Component(new String[]{"fr"}))
                .language("fr");
        multiSearch.addProvider(builder.build());//Add Address provider

        builder = new ProviderConfig.Builder(SearchProviderType.PLACES)
                .key(googleKey)
                .fallbackBreakpoint(1.0f)
                .minInputLength(8)
                .component(new Component(new String[]{"fr"}));
        multiSearch.addProvider(builder.build());//Add places provider

        multiSearch.addSearchListener(multiSearchListener);

        unitTestParams.add(new UnitTestParams("Montp","Montparnasse-Bienvenüe, Paris, France",5,SearchProviderType.LOCALITIES,SearchProviderType.LOCALITIES));
        unitTestParams.add(new UnitTestParams("Gar","Gare d'Austerlitz, Paris, France",5,SearchProviderType.LOCALITIES,SearchProviderType.LOCALITIES));

        for(UnitTestParams params: unitTestParams){
            try{
                multiSearchListener.items = new ArrayList<>();
                multiSearchListener.exception = null;

                multiSearch.autocompleteMulti(params.intput);
                synchronized (multiSearchListener){
                    multiSearchListener.wait();
                }
                if (multiSearchListener.exception!=null){
                    Assert.fail(multiSearchListener.exception.toString());
                }

                if (multiSearchListener.items.size() == 0){
                    Assert.fail("No result");
                }
                Assert.assertEquals("Total Results: ",unitTestParams.get(searchParamCounter).expectedResultCount,multiSearchListener.items.size());
                Assert.assertEquals("Expected First Result: ",unitTestParams.get(searchParamCounter).expectedFirstResult,multiSearchListener.items.get(0).getDescription());
                Assert.assertEquals("Expected First Api: ",unitTestParams.get(searchParamCounter).expectedFirstApi,multiSearchListener.items.get(0).getApi());
                Assert.assertEquals("Expected Last Api: ",unitTestParams.get(searchParamCounter).expectedLastApi,multiSearchListener.items.get(multiSearchListener.items.size()-1).getApi());
                searchParamCounter++;
            }
            catch (Exception ex){
                Log.e(AutocompleteTests.class.getName(),ex.toString());
            }
        }
    }

    /***
     * #23. Localities - UK
     */
    @Test
    public void test_Localities_uk(){
        ArrayList<UnitTestParams> unitTestParams=new ArrayList<>();
        searchParamCounter =0;
        multiSearch.setDebounceTime(100);
        multiSearch.removeAllProviders();
        final TestSearchListener multiSearchListener = new TestSearchListener();

        ProviderConfig.Builder builder;

        builder = new ProviderConfig.Builder(SearchProviderType.LOCALITIES)
                .key(apiKey)
                .fallbackBreakpoint(0.4f)
                .minInputLength(3)
                .component(new Component(new String[]{"gb"}))
                .language("en")
                .searchType("locality")
                .searchType("address")
                .searchType("postal_code");
        multiSearch.addProvider(builder.build());//Add locality provider

        multiSearch.addSearchListener(multiSearchListener);

        unitTestParams.add(new UnitTestParams("KT80AE, S","KT8 0AE, Surrey, United Kingdom",1,SearchProviderType.LOCALITIES,SearchProviderType.LOCALITIES));
        unitTestParams.add(new UnitTestParams("2 pillor","Spatial, 2 Pillory Street, Nantwich, CW5 5BD, United Kingdom",5,SearchProviderType.LOCALITIES,SearchProviderType.LOCALITIES));
        unitTestParams.add(new UnitTestParams("25 dean street newc","Crispy Bites, 25 Dean Street, Newcastle Upon Tyne, NE1 1PQ, United Kingdom",5,SearchProviderType.LOCALITIES,SearchProviderType.LOCALITIES));

        for(UnitTestParams params: unitTestParams){
            try{
                multiSearchListener.items = new ArrayList<>();
                multiSearchListener.exception = null;

                multiSearch.autocompleteMulti(params.intput);
                synchronized (multiSearchListener){
                    multiSearchListener.wait();
                }
                if (multiSearchListener.exception!=null){
                    Assert.fail(multiSearchListener.exception.toString());
                }

                if (multiSearchListener.items.size() == 0){
                    Assert.fail("No result");
                }
                Assert.assertEquals("Total Results: ",unitTestParams.get(searchParamCounter).expectedResultCount,multiSearchListener.items.size());
                Assert.assertEquals("Expected First Result: ",unitTestParams.get(searchParamCounter).expectedFirstResult,multiSearchListener.items.get(0).getDescription());
                Assert.assertEquals("Expected First Api: ",unitTestParams.get(searchParamCounter).expectedFirstApi,multiSearchListener.items.get(0).getApi());
                Assert.assertEquals("Expected Last Api: ",unitTestParams.get(searchParamCounter).expectedLastApi,multiSearchListener.items.get(multiSearchListener.items.size()-1).getApi());
                searchParamCounter++;
            }
            catch (Exception ex){
                Log.e(AutocompleteTests.class.getName(),ex.toString());
            }
        }
    }

    /***
     * #28. Localities, Address and Places - Localities' language = "fr"
     */
    @Test
    public void test_Localities_address_place_locality_france(){
        ArrayList<UnitTestParams> unitTestParams=new ArrayList<>();
        searchParamCounter =0;
        multiSearch.setDebounceTime(100);
        multiSearch.removeAllProviders();
        final TestSearchListener multiSearchListener = new TestSearchListener();

        ProviderConfig.Builder builder;

        builder = new ProviderConfig.Builder(SearchProviderType.LOCALITIES)
                .key(apiKey)
                .minInputLength(3)
                .component(new Component(new String[]{"gb"}))
                .language("fr")
                .searchType("locality")
                .searchType("postal_code");
        multiSearch.addProvider(builder.build());//Add locality provider

        builder = new ProviderConfig.Builder(SearchProviderType.ADDRESS)
                .key(apiKey)
                .minInputLength(5)
                .component(new Component(new String[]{"fr"}))
                .language("fr");
        multiSearch.addProvider(builder.build());//Add Address provider

        builder = new ProviderConfig.Builder(SearchProviderType.PLACES)
                .key(googleKey)
                .minInputLength(2)
                .language("fr")
                .component(new Component(new String[]{"fr"}));
        multiSearch.addProvider(builder.build());//Add places provider

        multiSearch.addSearchListener(multiSearchListener);

        unitTestParams.add(new UnitTestParams("Londres","Londres, City of London, Royaume-Uni",3,SearchProviderType.LOCALITIES,SearchProviderType.LOCALITIES));
        unitTestParams.add(new UnitTestParams("Cité","Cité de Londres, City of London, Royaume-Uni",5,SearchProviderType.LOCALITIES,SearchProviderType.LOCALITIES));


        for(UnitTestParams params: unitTestParams){
            try{
                multiSearchListener.items = new ArrayList<>();
                multiSearchListener.exception = null;

                multiSearch.autocompleteMulti(params.intput);
                synchronized (multiSearchListener){
                    multiSearchListener.wait();
                }
                if (multiSearchListener.exception!=null){
                    Assert.fail(multiSearchListener.exception.toString());
                }

                if (multiSearchListener.items.size() == 0){
                    Assert.fail("No result");
                }
                Assert.assertEquals("Total Results: ",unitTestParams.get(searchParamCounter).expectedResultCount,multiSearchListener.items.size());
                Assert.assertEquals("Expected First Result: ",unitTestParams.get(searchParamCounter).expectedFirstResult,multiSearchListener.items.get(0).getDescription());
                Assert.assertEquals("Expected First Api: ",unitTestParams.get(searchParamCounter).expectedFirstApi,multiSearchListener.items.get(0).getApi());
                Assert.assertEquals("Expected Last Api: ",unitTestParams.get(searchParamCounter).expectedLastApi,multiSearchListener.items.get(multiSearchListener.items.size()-1).getApi());

                searchParamCounter++;
            }
            catch (Exception ex){
                Log.e(AutocompleteTests.class.getName(),ex.toString());
            }
        }
    }

    /***
     * #32. Localities, Address and Places - Localities' country = ["gb", "fr"]
     */
    @Test
    public void test_Localities_address_place_locality_france_gb(){
        ArrayList<UnitTestParams> unitTestParams=new ArrayList<>();
        searchParamCounter =0;
        multiSearch.setDebounceTime(100);
        multiSearch.removeAllProviders();
        final TestSearchListener multiSearchListener = new TestSearchListener();

        ProviderConfig.Builder builder;

        builder = new ProviderConfig.Builder(SearchProviderType.LOCALITIES)
                .key(apiKey)
                .minInputLength(3)
                .component(new Component(new String[]{"fr","gb"}))
                .language("en")
                .searchType("locality")
                .searchType("postal_code");
        multiSearch.addProvider(builder.build());//Add locality provider

        builder = new ProviderConfig.Builder(SearchProviderType.ADDRESS)
                .key(apiKey)
                .minInputLength(5)
                .component(new Component(new String[]{"fr"}))
                .language("fr");
        multiSearch.addProvider(builder.build());//Add Address provider

        builder = new ProviderConfig.Builder(SearchProviderType.PLACES)
                .key(googleKey)
                .minInputLength(2)
                .language("fr")
                .component(new Component(new String[]{"fr"}));
        multiSearch.addProvider(builder.build());//Add places provider

        multiSearch.addSearchListener(multiSearchListener);

        unitTestParams.add(new UnitTestParams("Lon","London, City of London, United Kingdom",5,SearchProviderType.LOCALITIES,SearchProviderType.LOCALITIES));
        unitTestParams.add(new UnitTestParams("Man","Mandelieu-la-Napoule, Alpes-Maritimes, France",5,SearchProviderType.LOCALITIES,SearchProviderType.LOCALITIES));


        for(UnitTestParams params: unitTestParams){
            try{
                multiSearchListener.items = new ArrayList<>();
                multiSearchListener.exception = null;

                multiSearch.autocompleteMulti(params.intput);
                synchronized (multiSearchListener){
                    multiSearchListener.wait();
                }
                if (multiSearchListener.exception!=null){
                    Assert.fail(multiSearchListener.exception.toString());
                }

                if (multiSearchListener.items.size() == 0){
                    Assert.fail("No result");
                }

                Assert.assertEquals("Total Results: ",unitTestParams.get(searchParamCounter).expectedResultCount,multiSearchListener.items.size());
                Assert.assertEquals("Expected First Result: ",unitTestParams.get(searchParamCounter).expectedFirstResult,multiSearchListener.items.get(0).getDescription());
                Assert.assertEquals("Expected First Api: ",unitTestParams.get(searchParamCounter).expectedFirstApi,multiSearchListener.items.get(0).getApi());
                Assert.assertEquals("Expected Last Api: ",unitTestParams.get(searchParamCounter).expectedLastApi,multiSearchListener.items.get(multiSearchListener.items.size()-1).getApi());

                searchParamCounter++;
            }
            catch (Exception ex){
                Log.e(AutocompleteTests.class.getName(),ex.toString());
            }
        }
    }

    /***
     * #36. Localities, Address and Places - Address's country = ["it"]
     */
    @Test
    public void test_Localities_address_place_locality_italy_adreess(){
        ArrayList<UnitTestParams> unitTestParams=new ArrayList<>();
        searchParamCounter =0;
        multiSearch.setDebounceTime(100);
        multiSearch.removeAllProviders();
        final TestSearchListener multiSearchListener = new TestSearchListener();

        ProviderConfig.Builder builder;

        builder = new ProviderConfig.Builder(SearchProviderType.LOCALITIES)
                .key(apiKey)
                .minInputLength(3)
                .component(new Component(new String[]{"it"}))
                .language("en")
                .searchType("locality")
                .searchType("postal_code");
        multiSearch.addProvider(builder.build());//Add locality provider

        builder = new ProviderConfig.Builder(SearchProviderType.ADDRESS)
                .key(apiKey)
                .minInputLength(5)
                .component(new Component(new String[]{"it"}))
                .language("it");
        multiSearch.addProvider(builder.build());//Add Address provider

        builder = new ProviderConfig.Builder(SearchProviderType.PLACES)
                .key(googleKey)
                //.fallbackBreakpoint(1.0f)
                .minInputLength(2)
                .language("fr")
                .component(new Component(new String[]{"fr"}));
        multiSearch.addProvider(builder.build());//Add places provider

        multiSearch.addSearchListener(multiSearchListener);

        unitTestParams.add(new UnitTestParams("Via dom","Via Domenichino, 20149 Milano MI, Italia",5,SearchProviderType.ADDRESS,SearchProviderType.ADDRESS));
        unitTestParams.add(new UnitTestParams("Tardini","Via Domenico Tardini, 00167 Roma RM, Italia",1,SearchProviderType.ADDRESS,SearchProviderType.ADDRESS));


        for(UnitTestParams params: unitTestParams){
            try{
                multiSearchListener.items = new ArrayList<>();
                multiSearchListener.exception = null;

                multiSearch.autocompleteMulti(params.intput);
                synchronized (multiSearchListener){
                    multiSearchListener.wait();
                }
                if (multiSearchListener.exception!=null){
                    Assert.fail(multiSearchListener.exception.toString());
                }

                if (multiSearchListener.items.size() == 0){
                    Assert.fail("No result");
                }
                Assert.assertEquals("Total Results: ",unitTestParams.get(searchParamCounter).expectedResultCount,multiSearchListener.items.size());
                Assert.assertEquals("Expected First Result: ",unitTestParams.get(searchParamCounter).expectedFirstResult,multiSearchListener.items.get(0).getDescription());
                Assert.assertEquals("Expected First Api: ",unitTestParams.get(searchParamCounter).expectedFirstApi,multiSearchListener.items.get(0).getApi());
                Assert.assertEquals("Expected Last Api: ",unitTestParams.get(searchParamCounter).expectedLastApi,multiSearchListener.items.get(multiSearchListener.items.size()-1).getApi());

                searchParamCounter++;
            }
            catch (Exception ex){
                Log.e(AutocompleteTests.class.getName(),ex.toString());
            }
        }
    }

    /***
     * #40. Localities, Address and Places - Address's language = ["fr"]
     */
    @Test
    public void test_Localities_address_place_locality_address_language_en(){
        ArrayList<UnitTestParams> unitTestParams=new ArrayList<>();
        searchParamCounter =0;
        multiSearch.setDebounceTime(100);
        multiSearch.removeAllProviders();
        final TestSearchListener multiSearchListener = new TestSearchListener();

        ProviderConfig.Builder builder;

        builder = new ProviderConfig.Builder(SearchProviderType.LOCALITIES)
                .key(apiKey)
                .minInputLength(3)
                .component(new Component(new String[]{"it"}))
                .language("en")
                .searchType("locality")
                .searchType("postal_code");
        multiSearch.addProvider(builder.build());//Add locality provider

        builder = new ProviderConfig.Builder(SearchProviderType.ADDRESS)
                .key(apiKey)
                .minInputLength(5)
                .component(new Component(new String[]{"it"}))
                .language("fr");
        multiSearch.addProvider(builder.build());//Add Address provider

        builder = new ProviderConfig.Builder(SearchProviderType.PLACES)
                .key(googleKey)
                .minInputLength(2)
                .language("fr")
                .component(new Component(new String[]{"fr"}));
        multiSearch.addProvider(builder.build());//Add places provider

        multiSearch.addSearchListener(multiSearchListener);

        unitTestParams.add(new UnitTestParams("Via dom","Via Domitiana, 80078 Pouzzoles NA, Italie",5,SearchProviderType.ADDRESS,SearchProviderType.ADDRESS));
        unitTestParams.add(new UnitTestParams("Tardini","Via Domenico Tardini, 00167 Rome RM, Italie",1,SearchProviderType.ADDRESS,SearchProviderType.ADDRESS));

        for(UnitTestParams params: unitTestParams){
            try{
                multiSearchListener.items = new ArrayList<>();
                multiSearchListener.exception = null;

                multiSearch.autocompleteMulti(params.intput);
                synchronized (multiSearchListener){
                    multiSearchListener.wait();
                }
                if (multiSearchListener.exception!=null){
                    Assert.fail(multiSearchListener.exception.toString());
                }

                if (multiSearchListener.items.size() == 0){
                    Assert.fail("No result");
                }
                Assert.assertEquals("Total Results: ",unitTestParams.get(searchParamCounter).expectedResultCount,multiSearchListener.items.size());
                Assert.assertEquals("Expected First Result: ",unitTestParams.get(searchParamCounter).expectedFirstResult,multiSearchListener.items.get(0).getDescription());
                Assert.assertEquals("Expected First Api: ",unitTestParams.get(searchParamCounter).expectedFirstApi,multiSearchListener.items.get(0).getApi());
                Assert.assertEquals("Expected Last Api: ",unitTestParams.get(searchParamCounter).expectedLastApi,multiSearchListener.items.get(multiSearchListener.items.size()-1).getApi());
                searchParamCounter++;
            }
            catch (Exception ex){
                Log.e(AutocompleteTests.class.getName(),ex.toString());
            }
        }
    }

    /***
     * #44. Places - Places' country = ["gb", "fr"]
     */
    @Test
    public void test_place_country_en(){
        ArrayList<UnitTestParams> unitTestParams=new ArrayList<>();
        searchParamCounter =0;
        multiSearch.setDebounceTime(100);
        multiSearch.removeAllProviders();
        final TestSearchListener multiSearchListener = new TestSearchListener();

        ProviderConfig.Builder builder;

        builder = new ProviderConfig.Builder(SearchProviderType.PLACES)
                .key(googleKey)
                .minInputLength(2)
                .language("en")
                .component(new Component(new String[]{"fr","gb"}));
        multiSearch.addProvider(builder.build());//Add places provider

        multiSearch.addSearchListener(multiSearchListener);

        unitTestParams.add(new UnitTestParams("li","Liverpool Street Station, London, UK",5,SearchProviderType.PLACES,SearchProviderType.PLACES));
        unitTestParams.add(new UnitTestParams("Zara","Zara - Oxford Street, Oxford Street, London, UK",5,SearchProviderType.PLACES,SearchProviderType.PLACES));


        for(UnitTestParams params: unitTestParams){
            try{
                multiSearchListener.items = new ArrayList<>();
                multiSearchListener.exception = null;

                multiSearch.autocompleteMulti(params.intput);
                synchronized (multiSearchListener){
                    multiSearchListener.wait();
                }
                if (multiSearchListener.exception!=null){
                    Assert.fail(multiSearchListener.exception.toString());
                }

                if (multiSearchListener.items.size() == 0){
                    Assert.fail("No result");
                }
                Assert.assertEquals("Total Results: ",unitTestParams.get(searchParamCounter).expectedResultCount,multiSearchListener.items.size());
                Assert.assertEquals("Expected First Result: ",unitTestParams.get(searchParamCounter).expectedFirstResult,multiSearchListener.items.get(0).getDescription());
                Assert.assertEquals("Expected First Api: ",unitTestParams.get(searchParamCounter).expectedFirstApi,multiSearchListener.items.get(0).getApi());
                Assert.assertEquals("Expected Last Api: ",unitTestParams.get(searchParamCounter).expectedLastApi,multiSearchListener.items.get(multiSearchListener.items.size()-1).getApi());
                searchParamCounter++;
            }
            catch (Exception ex){
                Log.e(AutocompleteTests.class.getName(),ex.toString());
            }
        }
    }

    /***
     * #48. Store - Store's type = "bose_factory_store"
     */
    @Test
    public void test_store_store_factory(){
        ArrayList<UnitTestParams> unitTestParams=new ArrayList<>();
        searchParamCounter =0;
        multiSearch.setDebounceTime(100);
        multiSearch.removeAllProviders();
        final TestSearchListener multiSearchListener = new TestSearchListener();

        ProviderConfig.Builder builder;

        builder = new ProviderConfig.Builder(SearchProviderType.STORE)
                .key(apiKey)
                .ignoreFallbackBreakpoint(true)
                .query("type:type1");
        multiSearch.addProvider(builder.build());//Add places provider

        multiSearch.addSearchListener(multiSearchListener);

        unitTestParams.add(new UnitTestParams("vista","Vista & Foothill, Vista",1,SearchProviderType.STORE,SearchProviderType.STORE));


        for(UnitTestParams params: unitTestParams){
            try{
                multiSearchListener.items = new ArrayList<>();
                multiSearchListener.exception = null;

                multiSearch.autocompleteMulti(params.intput);
                synchronized (multiSearchListener){
                    multiSearchListener.wait();
                }
                if (multiSearchListener.exception!=null){
                    Assert.fail(multiSearchListener.exception.toString());
                }

                if (multiSearchListener.items.size() == 0){
                    Assert.fail("No result");
                }
                Assert.assertEquals("Total Results: ",unitTestParams.get(searchParamCounter).expectedResultCount,multiSearchListener.items.size());
                Assert.assertEquals("Expected First Result: ",unitTestParams.get(searchParamCounter).expectedFirstResult,multiSearchListener.items.get(0).getDescription());
                Assert.assertEquals("Expected First Api: ",unitTestParams.get(searchParamCounter).expectedFirstApi,multiSearchListener.items.get(0).getApi());
                Assert.assertEquals("Expected Last Api: ",unitTestParams.get(searchParamCounter).expectedLastApi,multiSearchListener.items.get(multiSearchListener.items.size()-1).getApi());
                searchParamCounter++;
            }
            catch (Exception ex){
                Log.e(AutocompleteTests.class.getName(),ex.toString());
            }
        }
    }


    /***
     * #52. Localities and Places - China
     */
    @Test
    public void test_LocalitiesPlacesChina(){
        ArrayList<UnitTestParams> unitTestParams=new ArrayList<>();
        searchParamCounter =0;
        multiSearch.setDebounceTime(100);
        multiSearch.removeAllProviders();
        final TestSearchListener multiSearchListener = new TestSearchListener();

        ProviderConfig.Builder builder;

        builder = new ProviderConfig.Builder(SearchProviderType.LOCALITIES)
                .key(apiKey)
                .minInputLength(3)
                .component(new Component(new String[]{"cn"}))
                .language("en")
                .searchType("locality")
                .searchType("postal_code");
        multiSearch.addProvider(builder.build());//Add localities provider

        builder = new ProviderConfig.Builder(SearchProviderType.PLACES)
                .key(googleKey)
                .minInputLength(2)
                .component(new Component(new String[]{"cn"}))
                .language("en");
        multiSearch.addProvider(builder.build());//Add places provider

        multiSearch.addSearchListener(multiSearchListener);

        unitTestParams.add(new UnitTestParams("北京市","Beijing, 北京市, China",2,SearchProviderType.LOCALITIES,SearchProviderType.LOCALITIES));
        unitTestParams.add(new UnitTestParams("北京市东城区南河沿大街33号-6","33 Nanheyan Street, Dongdan, Beijing, Dongcheng, Beijing, China",1,SearchProviderType.PLACES,SearchProviderType.PLACES));


        for(UnitTestParams params: unitTestParams){
            try{
                multiSearchListener.items = new ArrayList<>();
                multiSearchListener.exception = null;

                multiSearch.autocompleteMulti(params.intput);
                synchronized (multiSearchListener){
                    multiSearchListener.wait();
                }
                if (multiSearchListener.exception!=null){
                    Assert.fail(multiSearchListener.exception.toString());
                }

                if (multiSearchListener.items.size() == 0){
                    Assert.fail("No result");
                }
                Assert.assertEquals("Total Results: ",unitTestParams.get(searchParamCounter).expectedResultCount,multiSearchListener.items.size());
                Assert.assertEquals("Expected First Result: ",unitTestParams.get(searchParamCounter).expectedFirstResult,multiSearchListener.items.get(0).getDescription());
                Assert.assertEquals("Expected First Api: ",unitTestParams.get(searchParamCounter).expectedFirstApi,multiSearchListener.items.get(0).getApi());
                Assert.assertEquals("Expected Last Api: ",unitTestParams.get(searchParamCounter).expectedLastApi,multiSearchListener.items.get(multiSearchListener.items.size()-1).getApi());
                searchParamCounter++;
            }
            catch (Exception ex){
                Log.e(AutocompleteTests.class.getName(),ex.toString());
            }
        }
    }

    /***
     * #54. Localities and Places - Arabic
     */
    @Test
    public void test_LocalitiesPlacesArabic(){
        ArrayList<UnitTestParams> unitTestParams=new ArrayList<>();
        searchParamCounter =0;
        multiSearch.setDebounceTime(100);
        multiSearch.removeAllProviders();
        final TestSearchListener multiSearchListener = new TestSearchListener();

        ProviderConfig.Builder builder;

        builder = new ProviderConfig.Builder(SearchProviderType.LOCALITIES)
                .key(apiKey)
                .minInputLength(3)
                .component(new Component(new String[]{"qa"}))
                .language("ar")
                .searchType("locality")
                .searchType("postal_code");
        multiSearch.addProvider(builder.build());//Add localities provider

        builder = new ProviderConfig.Builder(SearchProviderType.PLACES)
                .key(googleKey)
                .minInputLength(2)
                .component(new Component(new String[]{"qa"}))
                .language("ar");
        multiSearch.addProvider(builder.build());//Add places provider

        multiSearch.addSearchListener(multiSearchListener);

        unitTestParams.add(new UnitTestParams("الدوحة","الدوحة, فريج محمد بن جاسم / مشيرب, Qatar",5,SearchProviderType.LOCALITIES,SearchProviderType.LOCALITIES));
        unitTestParams.add(new UnitTestParams("شارع الاتحاد، الدوحة","شارع الاتحاد، الدوحة، قطر",2,SearchProviderType.PLACES,SearchProviderType.PLACES));


        for(UnitTestParams params: unitTestParams){
            try{
                multiSearchListener.items = new ArrayList<>();
                multiSearchListener.exception = null;

                multiSearch.autocompleteMulti(params.intput);
                synchronized (multiSearchListener){
                    multiSearchListener.wait();
                }
                if (multiSearchListener.exception!=null){
                    Assert.fail(multiSearchListener.exception.toString());
                }

                if (multiSearchListener.items.size() == 0){
                    Assert.fail("No result");
                }
                Assert.assertEquals("Total Results: ",unitTestParams.get(searchParamCounter).expectedResultCount,multiSearchListener.items.size());
                Assert.assertEquals("Expected First Result: ",unitTestParams.get(searchParamCounter).expectedFirstResult,multiSearchListener.items.get(0).getDescription());
                Assert.assertEquals("Expected First Api: ",unitTestParams.get(searchParamCounter).expectedFirstApi,multiSearchListener.items.get(0).getApi());
                Assert.assertEquals("Expected Last Api: ",unitTestParams.get(searchParamCounter).expectedLastApi,multiSearchListener.items.get(multiSearchListener.items.size()-1).getApi());
                searchParamCounter++;
            }
            catch (Exception ex){
                Log.e(AutocompleteTests.class.getName(),ex.toString());
            }
        }
    }

    /***
     * #59.
     */
    @Test
    public void test_Localities_Data_advanced(){
        ArrayList<UnitTestParams> unitTestParams=new ArrayList<>();
        searchParamCounter =0;
        multiSearch.setDebounceTime(100);
        multiSearch.removeAllProviders();
        final TestSearchListener multiSearchListener = new TestSearchListener();

        ProviderConfig.Builder builder;

        builder = new ProviderConfig.Builder(SearchProviderType.LOCALITIES)
                .key(apiKey)
                .fallbackBreakpoint(1f)
                .component(new Component(new String[]{"in"}))
                .language("fr")
                .data(Data.ADVANCED)
                .searchType("postal_code");
        multiSearch.addProvider(builder.build());//Add locality provider

        multiSearch.addSearchListener(multiSearchListener);

        multiSearch.autocompleteMulti("365601");
        synchronized (multiSearchListener){
            try {
                multiSearchListener.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        if (multiSearchListener.exception!=null){
            Assert.fail(multiSearchListener.exception.toString());
        }

        Assert.assertEquals("Total Results: ",2,multiSearchListener.items.size());
        Assert.assertEquals("Expected First Result: ","365601, Amreli, Inde",multiSearchListener.items.get(0).getDescription());
    }

    /***
     * #60.
     */
    @Test
    public void test_Localities_Data_advanced_2(){
        ArrayList<UnitTestParams> unitTestParams=new ArrayList<>();
        searchParamCounter =0;
        multiSearch.setDebounceTime(100);
        multiSearch.removeAllProviders();
        final TestSearchListener multiSearchListener = new TestSearchListener();

        ProviderConfig.Builder builder;

        builder = new ProviderConfig.Builder(SearchProviderType.LOCALITIES)
                .key(apiKey)
                .fallbackBreakpoint(1f)
                .component(new Component(new String[]{"in"}))
                .language("fr")
                .searchType("postal_code");
        multiSearch.addProvider(builder.build());//Add locality provider

        multiSearch.addSearchListener(multiSearchListener);

        multiSearch.autocompleteMulti("365601");
        synchronized (multiSearchListener){
            try {
                multiSearchListener.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        if (multiSearchListener.exception!=null){
            Assert.fail(multiSearchListener.exception.toString());
        }

        Assert.assertEquals("Total Results: ",0,multiSearchListener.items.size());
    }

    /***
     * Check if the ids returned by the APIs are non empty.
     */
    @Test
    public void test_EmptyPublicId(){
        ArrayList<UnitTestParams> unitTestParams=new ArrayList<>();
        searchParamCounter =0;
        multiSearch.setDebounceTime(100);
        multiSearch.removeAllProviders();
        final TestSearchListener multiSearchListener = new TestSearchListener();

        ProviderConfig.Builder builder;

        builder = new ProviderConfig.Builder(SearchProviderType.LOCALITIES)
                .key(apiKey)
                .component(new Component(new String[]{"fr"}))
                .language("en")
                .searchType("locality")
                .searchType("postal_code");
        multiSearch.addProvider(builder.build());//Add locality provider

        builder = new ProviderConfig.Builder(SearchProviderType.ADDRESS)
                .key(apiKey)
                .component(new Component(new String[]{"fr"}))
                .language("fr");
        multiSearch.addProvider(builder.build());//Add Address provider

        builder = new ProviderConfig.Builder(SearchProviderType.STORE)
                .key(apiKey)
                .ignoreFallbackBreakpoint(true)
                .query("type:type1");
        multiSearch.addProvider(builder.build());//Add Address provider

        builder = new ProviderConfig.Builder(SearchProviderType.PLACES)
                .key(googleKey)
                .minInputLength(2)
                .component(new Component(new String[]{"fr"}))
                .language("fr");
        multiSearch.addProvider(builder.build());//Add places provider

        multiSearch.addSearchListener(multiSearchListener);

        multiSearchListener.items = new ArrayList<>();
        multiSearchListener.exception = null;

        //multiSearch.autocompleteMulti("paid");//This search string returns and element with empty id

        multiSearch.autocompleteMulti("paris");
        synchronized (multiSearchListener){
            try {
                multiSearchListener.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        if (multiSearchListener.exception!=null){
            Assert.fail(multiSearchListener.exception.toString());
        }

        for(AutocompleteResponseItem item: multiSearchListener.items){
            if (item.getId().equalsIgnoreCase("")){
                Assert.fail("Public id is empty");
            }
        }
    }

    @Test
    public void test_Localities_extended_true(){
        multiSearch.setDebounceTime(100);
        multiSearch.removeAllProviders();
        final TestSearchListener multiSearchListener = new TestSearchListener();

        ProviderConfig.Builder builder;

        builder = new ProviderConfig.Builder(SearchProviderType.LOCALITIES)
                .key(apiKey)
                .fallbackBreakpoint(1f)
                .minInputLength(1)
                .component(new Component(new String[]{"fr"}))
                .extended("postal_code")
                .searchType("locality");
        multiSearch.addProvider(builder.build());//Add locality provider

        multiSearch.addSearchListener(multiSearchListener);

        multiSearch.autocompleteMulti("60160");
        synchronized (multiSearchListener){
            try {
                multiSearchListener.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        if (multiSearchListener.exception!=null){
            Assert.fail(multiSearchListener.exception.toString());
        }

        Assert.assertEquals("Total Results: ",5,multiSearchListener.items.size());
        Assert.assertEquals("Expected First Result: ","Montataire (60160), Oise, France",multiSearchListener.items.get(0).getDescription());
    }
    @Test
    public void test_Localities_extended_false(){
        multiSearch.setDebounceTime(100);
        multiSearch.removeAllProviders();
        final TestSearchListener multiSearchListener = new TestSearchListener();

        ProviderConfig.Builder builder;

        builder = new ProviderConfig.Builder(SearchProviderType.LOCALITIES)
                .key(apiKey)
                .fallbackBreakpoint(1f)
                .minInputLength(1)
                .component(new Component(new String[]{"fr"}))
                .searchType("locality");
        multiSearch.addProvider(builder.build());//Add locality provider

        multiSearch.addSearchListener(multiSearchListener);

        multiSearch.autocompleteMulti("60160");
        synchronized (multiSearchListener){
            try {
                multiSearchListener.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        if (multiSearchListener.exception!=null){
            Assert.fail(multiSearchListener.exception.toString());
        }

        Assert.assertEquals("Total Results: ",0,multiSearchListener.items.size());

    }
}