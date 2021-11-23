# Woosmap MultiSearch Android Library
Smart search of multiple places and addresses APIs
### Overview
---
Woosmap MultiSearch Android is a native library designed to return location suggestions by calling several autocomplete services. This library makes location searches more efficient and cost-effective by allowing you to easily combine Woosmap Localities API, Woosmap Address API, Woosmap Search API (stores) and Google Places APIs (Places Autocomplete and Places Details).

{:.info}
**No Interface Provided**  
This library does not provide any user interface but focuses on querying autocomplete services. However, it is pretty
easy to display results on your own.

### How are autocomplete services combined?
---
Autocomplete services are requested in your desired order. Most often, only the first service will be queried. In some cases, for instance when searching for street addresses, Woosmap Localities can be insufficient.

By comparing the user input to the returned results and computing a string matching score between these two values, the library can automatically switch to the next autocomplete service and thereby provide suggestions that better suits the needs.

### Installation
---
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
	        implementation 'com.github.WebGeoServices:multisearch-android:tag'
	}
```

### The `MultiSearch` Object

---

The class that represents the wrapper to autocomplete services. 

When instantiating the `Multisearch`  object, you need, at least, to specify your desired autocomplete services’ API keys and the order in which these services will be called:

Create `MultiSearch` object
```java
    MultiSearch multiSearch = new MultiSearch(getApplicationContext());
```

Create `ProviderConfig` objects by using `ProviderConfig.Builder` class and add provider to `MultiSearch` object.

```java
    ProviderConfig.Builder builder;
    
    builder = new ProviderConfig.Builder(SearchProviderType.LOCALITIES)
                .key(<<woosmap_private_key>>);
    multiSearch.addProvider(builder.build());//Add locality provider
    
    builder = new ProviderConfig.Builder(SearchProviderType.STORE)
            .key(<<woosmap_private_key>>);
    multiSearch.addProvider(builder.build());//Add Store provider

    builder = new ProviderConfig.Builder(SearchProviderType.ADDRESS)
            .key(<<woosmap_private_key>>);
    multiSearch.addProvider(builder.build());//Add Address provider

    builder = new ProviderConfig.Builder(SearchProviderType.PLACES)
            .key(<<google_maps_api_key>>);
    multiSearch.addProvider(builder.build());//Add Places provider
```
### Specify `debounceTime`
---
Debounce time is the  amount of time in miliseconds the autocomplete function will wait after the last received call before executing the next one. If no value is specified then autocomplete function will not wait for the next excecution call. You can specify debounce time by setting `setDebounceTime` property.

```java
    multiSearch.setDebounceTime(500);
```

### Setting up callback listener
---

`MultiSearch` returns the autocomplete and details result using `MultiSearchListener` callback interface. This interface has two callback methods.

- **`onSearchComplete`**: This callback will be invoked once MultiSearch finishes any of it's autocomplete methods. Method has two parameters: <br/>
**1)** List of `AutocompleteResponseItem` objects. Value will be null if any exception has occurred. <br/>
**2)** `WoosmapException` exception object. Will be null if there was no exception during the execution.

- **`onDetailComplete`**: This callback will be invoked once MultiSearch finishes any of it's details methods.Method has two parameters:<br/>
**1)** `DetailsResponseItem` object. Value will be null if any exception has occurred.<br/>
**2)** `WoosmapException` exception object. Will be null if there was no exception during the execution.

```java
    MultiSearchListener multiSearchListener = new MultiSearchListener() {
        @Override
        public void onSearchComplete(List<AutocompleteResponseItem> searchResult, WoosmapException exception) {
            if (exception==null){ //Check if no exception has occured during the autocomplete 
                Log.d("Multisearch","Result count is: " + searchResult.size());
            }
            else{
                Log.d("Multisearch","Exception is: " + exception.getMessage());
            }
        }

        @Override
        public void onDetailComplete(DetailsResponseItem detailResult, WoosmapException exception) {
            if (exception==null){ //Check if no exception has occured during the autocomplete 
                Log.d("Multisearch","Detail id is: " + detailResult.getId());
            }
            else{
                Log.d("Multisearch","Exception is: " + exception.getMessage());
            }
        }
    };

    multiSearch.addSearchListener(multiSearchListener);
```

### Retrieve suggestions
---

You can retrieve suggestions from the user input by calling the `autocompleteMulti` method:

```java
    multiSearch.autocompleteMulti("paris");
```

### Get Details
---

Finally, to get the suggestion details when a user selects one from the pick list, call the `detailsMulti` method. This method accepts two parameters.

- **`id`**: id of the item retrieved from `autocompleteMulti` method.
- **`apiType`**: Underlying API type which will be called to fetch the details. Accepts enum of `SearchProviderType` type.  

```java
multiSearch.detailsMulti(<<place_id>>,SearchProviderType.LOCALITIES);
```

### Configure the Woosmap MultiSearch
---
The library requires to set a `key` for each available API in your MultiSearch implementation, whether it’s a Woosmap API or for Google Places API.

{:.info}
Please refer to the [documentation](https://developers.woosmap.com/support/api-keys/) to get an API Key if necessary.

```java
    ProviderConfig.Builder builder;
    
    builder = new ProviderConfig.Builder(SearchProviderType.LOCALITIES)
                .key(<<woosmap_private_key>>);
    multiSearch.addProvider(builder.build());//Add locality provider
    
    builder = new ProviderConfig.Builder(SearchProviderType.STORE)
            .key(<<woosmap_private_key>>);
    multiSearch.addProvider(builder.build());//Add Store provider

    builder = new ProviderConfig.Builder(SearchProviderType.ADDRESS)
            .key(<<woosmap_private_key>>);
    multiSearch.addProvider(builder.build());//Add Address provider

    builder = new ProviderConfig.Builder(SearchProviderType.PLACES)
            .key(<<google_maps_api_key>>);
    multiSearch.addProvider(builder.build());//Add Places provider
```

### API options
---
Additionally, for each provider configuration you can apply the same optional parameters as defined in the corresponding provider documentation.

- [Address Provider](/implementation_doc/address_config.md)
- [Store Provider](/implementation_doc/store_config.md)
- [Localities Provider](/implementation_doc/localities_config.md)
- [Places Provider](/implementation_doc/places_config.md)

Set the optional parameters for the providers
```java
    builder = new ProviderConfig.Builder(SearchProviderType.LOCALITIES)
            .key(getString(R.string.woosmap_private_key))
            .minInputLength(1)
            .component(new Component(new String[]{"FR"}))
            .searchType("locality")
            .searchType("country")
            .searchType("postal_code");
    multiSearch.addProvider(builder.build());//Add locality provider

    builder = new ProviderConfig.Builder(SearchProviderType.STORE)
            .key(getString(R.string.woosmap_private_key))
            .ignoreFallbackBreakPoint(true);
    multiSearch.addProvider(builder.build());//Add Store provider

    builder = new ProviderConfig.Builder(SearchProviderType.ADDRESS)
            .key(getString(R.string.woosmap_private_key))
            .fallbackBreakPoint(0.8f)
            .minInputLength(1)
            .component(new Component(new String[]{"FR"},"fr"));
    multiSearch.addProvider(builder.build());//Add Address provider

    builder = new ProviderConfig.Builder(SearchProviderType.PLACES)
            .key(getString(R.string.places_key))
            .fallbackBreakPoint(0.7f)
            .minInputLength(1)
            .component(new Component(new String[]{"fr"}))
            .language("it");
    multiSearch.addProvider(builder.build());//Add places provider
```

APIs will be called based on the order in which they were provided to `MultiSearch` object. In above code snippet following will be the order of the APIs:  
`LOCALITIES` &#8594; `STORE` &#8594; `ADDRESS` &#8594; `PLACES`

### Fallback configuration
---
Fallback system enables to switch from one provider to another. This system is flexible and can be manually adjusted for each provider in order to be efficient for each of your specific use cases.

Three parameters have an impact on the fallback:

**minInputLength**: Autocomplete service will return an empty result and no fallback will be triggered until the input length reaches the `minInputLength` value.

**fallbackBreakpoint**: Float value (between 0 and 1): When the suggestion score is lower than the `fallbackBreakpoint` value set, the library will stop calling the corresponding provider and switch to the next one (depending on the provider order).

A default value is defined for each provider:

| Provider | Default Value |
| ------ | ------ |
| `SearchProviderType.LOCALITIES` | `0.4` |
| `SearchProviderType.STORE` | `1` |
| `SearchProviderType.ADDRESS` | `0.5` |
| `SearchProviderType.PLACES` | `1` |

**ignoreFallbackBreakpoint**: When value is set to true, library will ignore `fallbackBreakpoint` for the given provider and will continue to provide a suggestion.

### How is the score calculated?
---
The score could be considered as a [Levenshtein Distance](https://en.wikipedia.org/wiki/Levenshtein_distance) between those two strings: the input from the user and the value (`description`) of a returned autocomplete item. We use the fuzzy searching JavaScript library [Fuse.js](https://fusejs.io/).

Generally speaking, fuzzy searching (formerly known as approximate string matching) is the technique of finding strings that are approximately equal to a given pattern (rather than exactly).

Have a look at the [Fuse.js scoring explanation](https://fusejs.io/concepts/scoring-theory.html) for more details.
