package com.acn.holidayapi.exception;

public class UnsupportedCountryException extends RuntimeException {
    private final String countryCode;
    
    public UnsupportedCountryException(String countryCode) {
        super(String.format("Country code '%s' is not supported by the Nager.Date API", countryCode));
        this.countryCode = countryCode;
    }
    
    public String getCountryCode() {
        return countryCode;
    }
}