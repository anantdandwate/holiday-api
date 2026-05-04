package com.acn.holidayapi.service;

import com.acn.holidayapi.dto.CountryDto;
import com.acn.holidayapi.exception.UnsupportedCountryException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import jakarta.annotation.PostConstruct;
import java.time.Duration;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.Arrays;

@Service
@Slf4j
public class CountryValidationService {

    private final WebClient webClient;
    private final Set<String> supportedCountries = ConcurrentHashMap.newKeySet();
    private volatile boolean initialized = false;

    @Value("${nager.api.available-countries-url:https://date.nager.at/api/v3/AvailableCountries}")
    private String availableCountriesUrl;

    @Value("${country.validation.cache.ttl-minutes:1440}") // 24 hours default
    private long cacheTtlMinutes;

    public CountryValidationService(WebClient webClient) {
        this.webClient = webClient;
    }

    /**
     * Initialize supported countries on application startup
     */
    @PostConstruct
    public void init() {
        log.info("🌍 Initializing CountryValidationService...");
        fetchSupportedCountries();
    }

    /**
     * Check if a country code is supported
     * Thread-safe lazy initialization with double-checked locking
     */
    public boolean isSupported(String countryCode) {
        if (!initialized) {
            synchronized (this) {
                if (!initialized) {
                    fetchSupportedCountries();
                }
            }
        }
        return supportedCountries.contains(countryCode.toUpperCase());
    }

    /**
     * Validate country code and throw exception if not supported
     */
    public void validateCountryCode(String countryCode) {
        if (!isSupported(countryCode)) {
            log.warn("⚠️ Unsupported country code requested: {}", countryCode);
            throw new UnsupportedCountryException(countryCode);
        }
    }

    /**
     * Get all supported countries (cached for 24 hours)
     */
    @Cacheable(value = "supportedCountries", unless = "#result.isEmpty()")
    public Set<String> getSupportedCountries() {
        if (!initialized) {
            fetchSupportedCountries();
        }
        return Collections.unmodifiableSet(supportedCountries);
    }

    /**
     * Fetch supported countries from Nager.Date API
     * Uses WebClient for better performance and error handling
     */
    private void fetchSupportedCountries() {
        try {
            log.debug("📡 Fetching supported countries from Nager.Date API...");

            CountryDto[] countries = webClient.get()
                    .uri(availableCountriesUrl)
                    .retrieve()
                    .bodyToMono(CountryDto[].class)
                    .timeout(Duration.ofSeconds(10))
                    .onErrorResume(e -> {
                        log.error("❌ Failed to fetch supported countries: {}", e.getMessage());
                        return Mono.just(new CountryDto[0]);
                    })
                    .block();

            if (countries != null && countries.length > 0) {
                supportedCountries.clear();
                supportedCountries.addAll(
                        Arrays.stream(countries)
                                .map(CountryDto::getCountryCode)
                                .map(String::toUpperCase)
                                .collect(Collectors.toSet()));
                initialized = true;
                log.info("✅ Successfully loaded {} supported countries", supportedCountries.size());
                log.debug("📋 Supported countries: {}", supportedCountries);
            } else {
                log.warn("⚠️ No countries received from API, using fallback");
                initializeFallbackCountries();
            }

        } catch (Exception e) {
            log.error("❌ Critical error fetching supported countries: {}", e.getMessage(), e);
            initializeFallbackCountries();
        }
    }

    /**
     * Fallback to a minimal set of common countries if API fails
     * This prevents complete service failure
     */
    private void initializeFallbackCountries() {
        supportedCountries.clear();
        supportedCountries.addAll(Set.of(
                "US", "GB", "DE", "FR", "CA", "AU", "NZ", "JP", "BR", "MX",
                "IT", "ES", "NL", "CH", "AT", "BE", "SE", "NO", "DK", "FI",
                "PL", "PT", "IE", "CZ", "GR", "HU", "RO", "SK", "BG", "HR"));
        initialized = true;
        log.warn("⚠️ Using fallback country list with {} countries", supportedCountries.size());
    }

    /**
     * Refresh the supported countries cache
     * Can be called by admin endpoint or scheduled task
     */
    public void refreshCache() {
        log.info("🔄 Manually refreshing supported countries cache...");
        initialized = false;
        fetchSupportedCountries();
    }

}