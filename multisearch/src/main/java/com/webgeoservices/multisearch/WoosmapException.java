package com.webgeoservices.multisearch;

/***
 * Wrapper class
 */
public class WoosmapException extends Exception {
    public WoosmapException(){}

    public WoosmapException(Throwable throwable){
        super(throwable);
    }

    public WoosmapException(String message){
        super(message);
    }

    public WoosmapException(Exception ex){
        super(ex);
    }

    public WoosmapException(String message, Throwable throwable){
        super(message,throwable);
    }
}
