package com.acn.holidayapi.service;

import com.acn.holidayapi.client.NagerDateApiClient;
import com.acn.holidayapi.dto.DeduplicatedHolidayDto;
import com.acn.holidayapi.dto.HolidayResponseDto;
import com.acn.holidayapi.model.Holiday;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

class HolidayServiceTest {
    @Mock
    private NagerDateApiClient nagerDateApiClient;

    @InjectMocks
    private HolidayService holidayService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetLastThreeHolidays() {
        String countryCode = "US";
        int year = LocalDate.now().getYear();
        List<Holiday> holidays = List.of(
                createHoliday(LocalDate.now().minusDays(1), "Name1", countryCode),
                createHoliday(LocalDate.now().minusDays(2), "Name2", countryCode),
                createHoliday(LocalDate.now().minusDays(3), "Name3", countryCode),
                createHoliday(LocalDate.now().plusDays(1), "Future", countryCode)
        );
        when(nagerDateApiClient.getPublicHolidays(year, countryCode)).thenReturn(holidays);
        List<HolidayResponseDto> result = holidayService.getLastThreeHolidays(countryCode);
        assertEquals(3, result.size());
        assertEquals("Name1", result.get(0).getLocalName());
    }

    @Test
    void testGetNonWeekendHolidayCounts() {
        String countryCode = "US";
        int year = 2024;
        List<Holiday> holidays = List.of(
                createHoliday(LocalDate.of(2024, 5, 1), "Name1", countryCode), // Wednesday
                createHoliday(LocalDate.of(2024, 5, 4), "Name2", countryCode), // Saturday
                createHoliday(LocalDate.of(2024, 5, 5), "Name3", countryCode)  // Sunday
        );
        when(nagerDateApiClient.getPublicHolidays(year, countryCode)).thenReturn(holidays);
        Map<String, Long> result = holidayService.getNonWeekendHolidayCounts(year, List.of(countryCode));
        assertEquals(1, result.get(countryCode));
    }

    @Test
    void testGetDeduplicatedHolidays() {
        int year = 2024;
        String code1 = "US", code2 = "DE";
        Holiday h1 = createHoliday(LocalDate.of(2024, 1, 1), "New Year US", code1);
        Holiday h2 = createHoliday(LocalDate.of(2024, 1, 1), "Neujahr DE", code2);
        when(nagerDateApiClient.getPublicHolidays(year, code1)).thenReturn(List.of(h1));
        when(nagerDateApiClient.getPublicHolidays(year, code2)).thenReturn(List.of(h2));
        List<DeduplicatedHolidayDto> result = holidayService.getDeduplicatedHolidays(year, code1, code2);
        assertEquals(1, result.size());
        assertTrue(result.get(0).getLocalNames().contains("New Year US"));
        assertTrue(result.get(0).getLocalNames().contains("Neujahr DE"));
    }

    private Holiday createHoliday(LocalDate date, String localName, String countryCode) {
        Holiday h = new Holiday();
        h.setDate(date);
        h.setLocalName(localName);
        h.setCountryCode(countryCode);
        return h;
    }
}
