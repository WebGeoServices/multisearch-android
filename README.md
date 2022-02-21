## Install SDK

How to get a Git project into your build:

**Step 1. Add the JitPack repository to your build file**
Add it in your root build.gradle at the end of repositories:
```
	allprojects {
		repositories {
			...
			maven { url 'https://jitpack.io' }
		}
	}
```

**Step 2. Add the dependency**
```
	dependencies {
	        implementation 'com.github.Woosmap:multisearch-android:tag'
	}
```

## wgs-multisearch-android

How to use

**Initialize library**

``` java
  MultiSearch multiSearch = new MultiSearch(getApplicationContext());
  ProviderConfig.Builder builder;
```

**Configure and add providers**

``` java
  builder = new ProviderConfig.Builder(SearchProviderType.LOCALITIES)
                .key(getString(R.string.woosmap_private_key))
                .minInputLength(1)
                .component(new Component(new String[]{"FR"}))
                .searchType("locality")
                .searchType("country")
                .searchType("postal_code");
  multiSearch.addProvider(builder.build());//Add locality provider
  
  builder = new ProviderConfig.Builder(SearchProviderType.ADDRESS)
                .key(getString(R.string.woosmap_private_key))
                .fallbackBreakPoint(0.8f)
                .minInputLength(1)
                .component(new Component(new String[]{"FR"},"fr"));
  multiSearch.addProvider(builder.build());//Add Address provider
  
  builder = new ProviderConfig.Builder(SearchProviderType.STORE)
                .key(getString(R.string.woosmap_private_key))
                .ignoreFallbackBreakPoint(true)
                .query("type:bose_store");
  multiSearch.addProvider(builder.build());//Add Store provider
  
  builder = new ProviderConfig.Builder(SearchProviderType.PLACES)
                .key(getString(R.string.places_key))
                .fallbackBreakPoint(0.7f)
                .minInputLength(1)
                .component(new Component(new String[]{"fr"}))
                .language("it");
  multiSearch.addProvider(builder.build());//Add places provider
```

**Alternate way of adding countries in `component` filter**

```java

  //Create component object. Add countries and set language
  Component regionComponent = new Component();
  regionComponent.addCountry("fr");
  regionComponent.addCountry("gb");
  regionComponent.setLanguage("fr");
  
  
  //Provide component object to provider configuration
  builder = new ProviderConfig.Builder(SearchProviderType.LOCALITIES)
                .key(getString(R.string.woosmap_private_key))
                .minInputLength(1)
                .component(regionComponent)
                .searchType("locality")
                .searchType("country")
                .searchType("postal_code");
  multiSearch.addProvider(builder.build());//Add locality provider

```

**Add listener**

``` java
  multiSearch.addSearchListener(new MultiSearchListener() {
            @Override
            public void onSearchComplete(List<AutocompleteResponseItem> searchResult, WoosmapException exception) {
                
            }

            @Override
            public void onDetailComplete(DetailsResponseItem detailResult, WoosmapException exception) {
                
            }
 });
```

**Getting list**
``` java
        //Mutisearch
        multiSearch.autocompleteMulti("Paris");

        //Address
        multiSearch.autocompleteAddress("Paris");

        //Localities
        multiSearch.autocompleteLocalities("Paris");

        //Places
        multiSearch.autocompletePlaces("Paris");

        //Store
        multiSearch.autocompleteStore("Paris");
```

**Getting details**
``` java
        //Address
        multiSearch.detailsMulti("<public_id>",SearchProviderType.ADDRESS);
        
        //Localities
        multiSearch.detailsMulti("<public_id>",SearchProviderType.LOCALITIES);
        
        //Places
        multiSearch.detailsMulti("<public_id>",SearchProviderType.PLACES);
        
        //Store
        multiSearch.detailsMulti("<public_id>",SearchProviderType.STORE);
```
