# Configuring Localities Provider

A provider is a wrapper around a Woosmap API or a Google API which will be called to return the result for a query or get details of a place. Each provider must be created from a provider configuration and this provider must then be passed to `MultiSearch` object by calling `MultiSearch.addProvider` method. Use `ProviderConfig.Builder` class in order to create a provider configuration.

To create a configuration for Localities provider use

```java
ProviderConfig.Builder builder;
builder = new ProviderConfig.Builder(SearchProviderType.LOCALITIES)
```

### Required Parameters
Certain parameters are required to create a Localities provider configuration. 

**type**: Type of the provider. Passed as `SearchProviderType` enum. In this case type will be `SearchProviderType.LOCALITIES`

---

**key**: Your project’s private key. This key identifies your Woosmap Project for purposes of quota management.

### Optional Parameters

**minInputLength**: Minimum input length of the search string. Empty result will be sent by the API and no callback will be triggered if the input length does not reach the minInputLength value. If no value is specified, library will ignore minimum input length and call the API starting from first character of the search string. 

---

**fallbackBreakpoint**: Float value (between 0 and 1): When the suggestion score is lower than the fallbackBreakpoint value set, the library will stop calling the corresponding provider and switch to the next one (depending on the provider order). <br/>Default value for Localities provider is `1`

---

**ignoreFallbackBreakpoint**: When value is set to true, library will ignore `fallbackBreakpoint` for the given provider and will continue to provide a suggestion.

---

**searchType**: The types of suggestion to return. Several types are available, see the list below:

| Type |  |
| ------ | ------ |
| `locality` |includes locality names (from city to village) and suburbs |
| `postal_code` |publicly-used postal codes around the world |
| `address` |addresses (single string text, requires details request) |
| `admin_level` |most commonly used administrative areas |
| `country` |countries as whole point of interest |
| `airport` |includes all medium sized to international sized airports |
| `train_station` |includes all train stations |
| `metro_station` |includes all metro stations |
| `shopping` |includes shopping malls (or “shopping centers”) - may include private retail brands |
| `museum` |includes museums |
| `tourist_attraction` |includes tourist attractions like the Eiffel tower |
| `amusement_park` |includes amusement parks like Disneyland Paris |
| `art_gallery` |includes art galleries |
| `zoo` |includes zoos |

Multiple types can be passed to configuration builder by specifying `searchType` property multiple times.

```java
    builder = new ProviderConfig.Builder(SearchProviderType.LOCALITIES)
        .key(getString(R.string.woosmap_private_key))
        .searchType("train_station")
        .searchType("metro_station")
        .searchType("airport");
```

---

**component**: A grouping of places to which you would like to restrict your results. Currently, you can use components to filter over countries. Countries must be passed as a two character, [ISO 3166-1 Alpha-2 compatible country code.](https://en.wikipedia.org/wiki/List_of_ISO_3166_country_codes) Component can be added to configuration builder using following code.

```java
    Component regionComponent = new Component();
    regionComponent.addCountry("fr");
    regionComponent.addCountry("gb");
    regionComponent.setLanguage("fr");
    
    //Provide component object to provider configuration
    builder = new ProviderConfig.Builder(SearchProviderType.LOCALITIES)
        .key(getString(R.string.woosmap_private_key))
        .minInputLength(1)
        .component(regionComponent);
```

---

**language**: The language code, indicating in which language the results should be returned, if possible. Searches are also biased to the selected language; results in the selected language may be given a higher ranking. If language is not supplied, the Localities service will use the default language of each country. No language necessary for `postal_code` request. You can specify the language to the provider configuration by: 

```java
    builder = new ProviderConfig.Builder(SearchProviderType.LOCALITIES)
            .key(getString(R.string.woosmap_private_key))
            .component(new Component(new String[]{"fr"}))
            .language("fr");
```

---

**data**: Two values for this parameter: standard or advanced. By default, if the parameter is not defined, value is set as standard. The advanced value opens suggestions to worldwide postal codes in addition to postal codes for Western Europe. *A dedicated option subject to specific billing on your license is needed to use this parameter. Please contact us if you are interested in using this parameter and you do not have subscribed the proper option yet.*

```java

    builder = new ProviderConfig.Builder(SearchProviderType.LOCALITIES)
            .key(getString(R.string.woosmap_private_key))
            .data(Data.ADVANCED);
    
```

---

**extended**: If set, this parameter allows a refined search over locality names that bears the same postal code. By triggering this parameter, integrators will benefit from a search spectrum on the locality type that ***includes postal codes***. To avoid confusion, it is recommended not to activate this parameter along with the postal_code type which could lead to duplicate locations. Also, the default description returned by the API changes to name (postal code), admin_1, admin_0. It is only available for France and Italy.

```java
  
  builder = new ProviderConfig.Builder(SearchProviderType.LOCALITIES)
            .key(getString(R.string.woosmap_private_key))
            .extended("postal_code");
            
```

