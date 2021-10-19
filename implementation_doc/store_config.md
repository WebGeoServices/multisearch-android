# Configuring Store Provider

A provider is a wrapper around a Woosmap API or a Google API which will be called to return the result for a query or get details of a place. Each provider must be created from a provider configuration and this provider must then be passed to `MultiSearch` object by calling `MultiSearch.addProvider` method. Use `ProviderConfig.Builder` class in order to create a provider configuration.

To create a configuration for Address provider use

```java
ProviderConfig.Builder builder;
builder = new ProviderConfig.Builder(SearchProviderType.STORE)
```

### Required Parameters
Certain parameters are required to create a Store provider configuration. 

**type**: Type of the provider. Passed as `SearchProviderType` enum. In this case type will be `SearchProviderType.STORE`

---

**key**: Your project’s private key. This key identifies your Woosmap Project for purposes of quota management.

### Optional Parameters
**minInputLength**: Minimum input length of the search string. Empty result will be sent by the API and no callback will be triggered if the input length does not reach the minInputLength value. If no value is specified, library will ignore minimum input length and call the API starting from first character of the search string. 

---

**fallbackBreakpoint**: Float value (between 0 and 1): When the suggestion score is lower than the fallbackBreakpoint value set, the library will stop calling the corresponding provider and switch to the next one (depending on the provider order).<br/> Default value for Store provider is `0.4`

---

**ignoreFallbackBreakpoint**: When value is set to true, library will ignore `fallbackBreakpoint` for the given provider and will continue to provide a suggestion.

---

**query**: This API accepts the query parameter which is a search query combining one or more search clauses. Each search clause is made up of three parts structured as `field` `:` `operator` `value`. , e.g. `name:="My cool store"`

For more on on building query check [Woosmap Store API documentation](https://developers.woosmap.com/products/search-api/search-query/)

Example of Store config builder is below

```java
    ProviderConfig.Builder builder;

    builder = new ProviderConfig.Builder(SearchProviderType.STORE)
        .key(apiKey)
        .query("type:bose_store");
```

