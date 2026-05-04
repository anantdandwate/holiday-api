package com.acn.holidayapi.service;

import com.acn.holidayapi.dto.CountryDto;
import com.acn.holidayapi.exception.UnsupportedCountryException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CountryValidationServiceTest {

    @Mock
    private WebClient webClient;

    @Mock
    private WebClient.RequestHeadersUriSpec requestHeadersUriSpec;

    @Mock
    private WebClient.RequestHeadersSpec requestHeadersSpec;

    @Mock
    private WebClient.ResponseSpec responseSpec;

    private CountryValidationService countryValidationService;

    @BeforeEach
    void setUp() {
        countryValidationService = new CountryValidationService(webClient);

        // Set the URL using ReflectionTestUtils (cleaner approach)
        ReflectionTestUtils.setField(
                countryValidationService,
                "availableCountriesUrl",
                "https://date.nager.at/api/v3/AvailableCountries");

        ReflectionTestUtils.setField(
                countryValidationService,
                "cacheTtlMinutes",
                1440L);
    }

    @Test
    void testIsSupported_ValidCountry() {
        // Arrange
        CountryDto[] countries = {
                createCountryDto("US", "United States"),
                createCountryDto("GB", "United Kingdom")
        };

        // Mock the complete WebClient chain
        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(CountryDto[].class)).thenReturn(Mono.just(countries));

        // Act
        boolean isUSSupported = countryValidationService.isSupported("US");
        boolean isUSLowerSupported = countryValidationService.isSupported("us");
        boolean isGBSupported = countryValidationService.isSupported("GB");

        // Assert
        assertTrue(isUSSupported, "US should be supported");
        assertTrue(isUSLowerSupported, "us (lowercase) should be supported (case insensitive)");
        assertTrue(isGBSupported, "GB should be supported");

        // Verify the API was called only once (due to initialization)
        verify(webClient, times(1)).get();
    }

    @Test
    void testIsSupported_InvalidCountry() {
        // Arrange
        CountryDto[] countries = {
                createCountryDto("US", "United States"),
                createCountryDto("GB", "United Kingdom")
        };

        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(CountryDto[].class)).thenReturn(Mono.just(countries));

        // Act
        boolean isINSupported = countryValidationService.isSupported("IN");
        boolean isXXSupported = countryValidationService.isSupported("XX");

        // Assert
        assertFalse(isINSupported, "IN should not be supported");
        assertFalse(isXXSupported, "XX should not be supported");
    }

    @Test
    void testValidateCountryCode_ValidCountry() {
        // Arrange
        CountryDto[] countries = {
                createCountryDto("US", "United States")
        };

        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(CountryDto[].class)).thenReturn(Mono.just(countries));

        // Act & Assert - should not throw exception
        assertDoesNotThrow(() -> countryValidationService.validateCountryCode("US"));
    }

    @Test
    void testValidateCountryCode_ThrowsException_ForUnsupportedCountry() {
        // Arrange
        CountryDto[] countries = {
                createCountryDto("US", "United States")
        };

        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(CountryDto[].class)).thenReturn(Mono.just(countries));

        // Act & Assert
        UnsupportedCountryException exception = assertThrows(
                UnsupportedCountryException.class,
                () -> countryValidationService.validateCountryCode("IN"));

        assertEquals("IN", exception.getCountryCode());
        assertTrue(exception.getMessage().contains("IN"));
    }

    @Test
    void testFetchSupportedCountries_ApiFailure_UsesFallback() {
        // Arrange - simulate API failure
        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(CountryDto[].class))
                .thenReturn(Mono.error(new RuntimeException("API Error")));

        // Act
        boolean isUSSupported = countryValidationService.isSupported("US");
        boolean isINSupported = countryValidationService.isSupported("IN");

        // Assert - fallback countries should be loaded
        assertTrue(isUSSupported, "US should be in fallback list");
        assertFalse(isINSupported, "IN should not be in fallback list");
    }

    @Test
    void testGetSupportedCountries() {
        // Arrange
        CountryDto[] countries = {
                createCountryDto("US", "United States"),
                createCountryDto("GB", "United Kingdom"),
                createCountryDto("DE", "Germany")
        };

        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(CountryDto[].class)).thenReturn(Mono.just(countries));

        // Act
        var supportedCountries = countryValidationService.getSupportedCountries();

        // Assert
        assertNotNull(supportedCountries);
        assertEquals(3, supportedCountries.size());
        assertTrue(supportedCountries.contains("US"));
        assertTrue(supportedCountries.contains("GB"));
        assertTrue(supportedCountries.contains("DE"));
    }

    @Test
    void testRefreshCache() {
        // Arrange
        CountryDto[] initialCountries = {
                createCountryDto("US", "United States")
        };

        CountryDto[] refreshedCountries = {
                createCountryDto("US", "United States"),
                createCountryDto("CA", "Canada")
        };

        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(CountryDto[].class))
                .thenReturn(Mono.just(initialCountries))
                .thenReturn(Mono.just(refreshedCountries));

        // Act
        countryValidationService.isSupported("US"); // Initialize
        countryValidationService.refreshCache();

        // Assert
        assertTrue(countryValidationService.isSupported("CA"), "CA should be available after refresh");
        verify(webClient, times(2)).get(); // Once for init, once for refresh
    }

    private CountryDto createCountryDto(String code, String name) {
        CountryDto dto = new CountryDto();
        dto.setCountryCode(code);
        dto.setName(name);
        return dto;
    }
}