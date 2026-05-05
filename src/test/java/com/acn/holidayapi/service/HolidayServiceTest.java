package com.acn.holidayapi.service;

import com.acn.holidayapi.client.NagerDateApiClient;
import com.acn.holidayapi.dto.DeduplicatedHolidayDto;
import com.acn.holidayapi.dto.HolidayResponseDto;
import com.acn.holidayapi.model.Holiday;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT) // ✅ Add this to allow unused stubs
class HolidayServiceTest {

    @Mock
    private NagerDateApiClient nagerDateApiClient;

    @Mock
    private CountryValidationService countryValidationService;

    private HolidayService holidayService;

    @BeforeEach
    void setUp() {
        holidayService = new HolidayService(nagerDateApiClient, countryValidationService);

        // ✅ This stub is now allowed to be unused in some tests
        doNothing().when(countryValidationService).validateCountryCode(anyString());
    }

    @Test
    void testGetLastThreeHolidays() {
        // Arrange
        String countryCode = "US";
        LocalDate today = LocalDate.now();

        List<Holiday> holidays = Arrays.asList(
                createHoliday("New Year's Day", today.minusDays(10), countryCode),
                createHoliday("Independence Day", today.minusDays(20), countryCode),
                createHoliday("Thanksgiving", today.minusDays(30), countryCode),
                createHoliday("Christmas", today.minusDays(40), countryCode));

        when(nagerDateApiClient.getPublicHolidays(anyInt(), eq(countryCode)))
                .thenReturn(holidays);

        // Act
        List<HolidayResponseDto> result = holidayService.getLastThreeHolidays(countryCode);

        // Assert
        assertNotNull(result);
        assertEquals(3, result.size());
        assertEquals("New Year's Day", result.get(0).getLocalName());
        assertEquals("Independence Day", result.get(1).getLocalName());
        assertEquals("Thanksgiving", result.get(2).getLocalName());

        verify(countryValidationService).validateCountryCode(countryCode);
        verify(nagerDateApiClient, atLeastOnce()).getPublicHolidays(anyInt(), eq(countryCode));
    }

    @Test
    void testGetNonWeekendHolidayCounts() {
        // Arrange
        int year = 2024;
        List<String> countryCodes = Arrays.asList("US", "CA");

        List<Holiday> usHolidays = Arrays.asList(
                createHoliday("Holiday 1", LocalDate.of(2024, 1, 1), "US"), // Monday
                createHoliday("Holiday 2", LocalDate.of(2024, 1, 6), "US"), // Saturday
                createHoliday("Holiday 3", LocalDate.of(2024, 1, 15), "US") // Monday
        );

        List<Holiday> caHolidays = Arrays.asList(
                createHoliday("Holiday 1", LocalDate.of(2024, 1, 1), "CA"), // Monday
                createHoliday("Holiday 2", LocalDate.of(2024, 7, 1), "CA") // Monday
        );

        when(nagerDateApiClient.getPublicHolidays(year, "US")).thenReturn(usHolidays);
        when(nagerDateApiClient.getPublicHolidays(year, "CA")).thenReturn(caHolidays);

        // Act
        Map<String, Long> result = holidayService.getNonWeekendHolidayCounts(year, countryCodes);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(2L, result.get("US"));
        assertEquals(2L, result.get("CA"));

        verify(nagerDateApiClient).getPublicHolidays(year, "US");
        verify(nagerDateApiClient).getPublicHolidays(year, "CA");
    }

    @Test
    void testGetDeduplicatedHolidays() {
        // Arrange
        int year = 2024;
        String country1 = "US";
        String country2 = "CA";

        LocalDate commonDate1 = LocalDate.of(2024, 1, 1);
        LocalDate commonDate2 = LocalDate.of(2024, 12, 25);
        LocalDate uniqueDate = LocalDate.of(2024, 7, 4);

        List<Holiday> usHolidays = Arrays.asList(
                createHoliday("New Year's Day", commonDate1, country1),
                createHoliday("Independence Day", uniqueDate, country1),
                createHoliday("Christmas Day", commonDate2, country1));

        List<Holiday> caHolidays = Arrays.asList(
                createHoliday("New Year's Day", commonDate1, country2),
                createHoliday("Christmas Day", commonDate2, country2));

        when(nagerDateApiClient.getPublicHolidays(year, country1)).thenReturn(usHolidays);
        when(nagerDateApiClient.getPublicHolidays(year, country2)).thenReturn(caHolidays);

        // Act
        List<DeduplicatedHolidayDto> result = holidayService.getDeduplicatedHolidays(year, country1, country2);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());

        assertEquals(commonDate1, result.get(0).getDate());
        assertTrue(result.get(0).getLocalNames().contains("New Year's Day"));

        assertEquals(commonDate2, result.get(1).getDate());
        assertTrue(result.get(1).getLocalNames().contains("Christmas Day"));

        verify(nagerDateApiClient).getPublicHolidays(year, country1);
        verify(nagerDateApiClient).getPublicHolidays(year, country2);
    }

    @Test
    void testGetLastThreeHolidays_EmptyResult() {
        // Arrange
        String countryCode = "XX";
        when(nagerDateApiClient.getPublicHolidays(anyInt(), eq(countryCode)))
                .thenReturn(Arrays.asList());

        // Act
        List<HolidayResponseDto> result = holidayService.getLastThreeHolidays(countryCode);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    private Holiday createHoliday(String name, LocalDate date, String countryCode) {
        Holiday holiday = new Holiday();
        holiday.setName(name);
        holiday.setLocalName(name);
        holiday.setDate(date);
        holiday.setCountryCode(countryCode);
        return holiday;
    }

    @Test
    void testGetLastThreeHolidays_OnlyFutureHolidays() {
        // Arrange
        String countryCode = "US";
        LocalDate today = LocalDate.now();
        List<Holiday> futureHolidays = Arrays.asList(
                createHoliday("Future Holiday 1", today.plusDays(10), countryCode),
                createHoliday("Future Holiday 2", today.plusDays(20), countryCode));

        when(nagerDateApiClient.getPublicHolidays(anyInt(), eq(countryCode)))
                .thenReturn(futureHolidays);

        // Act
        List<HolidayResponseDto> result = holidayService.getLastThreeHolidays(countryCode);

        // Assert
        assertTrue(result.isEmpty(), "Should return empty list when all holidays are in the future");
    }

    @Test
    void testGetDeduplicatedHolidays_NoCommonHolidays() {
        // Arrange
        int year = 2024;
        List<Holiday> usHolidays = Arrays.asList(
                createHoliday("Independence Day", LocalDate.of(2024, 7, 4), "US"));
        List<Holiday> caHolidays = Arrays.asList(
                createHoliday("Canada Day", LocalDate.of(2024, 7, 1), "CA"));

        when(nagerDateApiClient.getPublicHolidays(year, "US")).thenReturn(usHolidays);
        when(nagerDateApiClient.getPublicHolidays(year, "CA")).thenReturn(caHolidays);

        // Act
        List<DeduplicatedHolidayDto> result = holidayService.getDeduplicatedHolidays(year, "US", "CA");

        // Assert
        assertTrue(result.isEmpty(), "Should return empty list when no common holidays");
    }
}