package com.webgeoservices.multisearch;

public class UnitTestParams{
    public String intput;
    public String expectedFirstResult;
    public String expectedLastResult;
    public int expectedResultCount;
    public SearchProviderType expectedFirstApi;
    public SearchProviderType expectedLastApi;

    public UnitTestParams(String intput,String expectedFirstResult,int expectedResultCount, SearchProviderType expectedFirstApi, SearchProviderType expectedLastApi){
        this.expectedFirstResult = expectedFirstResult;
        this.intput = intput;
        this.expectedResultCount=expectedResultCount;
        this.expectedFirstApi = expectedFirstApi;
        this.expectedLastApi = expectedLastApi;
    }
}