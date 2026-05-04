package com.acn.holidayapi.client;

import com.acn.holidayapi.model.Holiday;
import com.acn.holidayapi.exception.ApiException;
import com.acn.holidayapi.exception.UnsupportedCountryException;

import lombok.extern.slf4j.Slf4j;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Slf4j
@Component
public class NagerDateApiClient {

    private final WebClient webClient;

    // Constructor injection - Spring will inject the SSL-configured WebClient bean
    public NagerDateApiClient(WebClient webClient) {
        this.webClient = webClient;
        log.info("✅ NagerDateApiClient initialized with configured WebClient");
    }

    @Cacheable(value = "holidays", key = "#year + '-' + #countryCode")
    public List<Holiday> getPublicHolidays(int year, String countryCode) {
        try {
            log.debug("🌍 Fetching holidays for country: {}, year: {}", countryCode, year);

            Holiday[] holidays = webClient.get()
                    .uri("/PublicHolidays/{year}/{countryCode}", year, countryCode)
                    .retrieve()
                    .bodyToMono(Holiday[].class)
                    .block();

            if (holidays == null || holidays.length == 0) {
                log.warn("⚠️ No holidays found for {}/{}", countryCode, year);
                return Collections.emptyList();
            }

            log.info("✅ Successfully fetched {} holidays for {}/{}", holidays.length, countryCode, year);
            return Arrays.asList(holidays);

        } catch (WebClientResponseException.NotFound e) {
            log.error("❌ Country '{}' not found in Nager.Date API", countryCode);

            throw new UnsupportedCountryException(countryCode);
        } catch (WebClientResponseException e) {
            log.error("❌ API error for {}/{}: {} - {}",
                    countryCode, year, e.getStatusCode(), e.getResponseBodyAsString());
            throw new ApiException("Failed to fetch holidays from Nager.Date API: " + e.getMessage(), e);

        } catch (Exception e) {
            log.error("❌ Unexpected error fetching holidays for {}/{}: {}",
                    countryCode, year, e.getMessage(), e);
            throw new ApiException("Unexpected error when calling Nager.Date API: " + e.getMessage(), e);
        }
    }
}