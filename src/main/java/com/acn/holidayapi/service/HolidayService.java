package com.acn.holidayapi.service;

import com.acn.holidayapi.client.NagerDateApiClient;
import com.acn.holidayapi.dto.DeduplicatedHolidayDto;
import com.acn.holidayapi.dto.HolidayResponseDto;
import com.acn.holidayapi.model.Holiday;
import com.acn.holidayapi.util.Constants;

import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class HolidayService {
    private final NagerDateApiClient nagerDateApiClient;
    private final CountryValidationService countryValidationService;

    // 1. Given a country, return the last celebrated 3 holidays (date and name)
    public List<HolidayResponseDto> getLastThreeHolidays(String countryCode) {
        countryValidationService.validateCountryCode(countryCode);
        LocalDate today = LocalDate.now();
        List<Holiday> allPastHolidays = new ArrayList<>();

        // Check current year and up to 2 previous years
        for (int yearOffset = 0; yearOffset <= Constants.MAX_YEARS_TO_CHECK; yearOffset++) {
            int year = today.getYear() - yearOffset;

            log.debug("Fetching holidays for {} in year {}", countryCode, year);
            List<Holiday> holidays = nagerDateApiClient.getPublicHolidays(year, countryCode);

            if (holidays != null) {
                holidays.stream()
                        .filter(h -> h != null && h.getDate() != null)
                        .filter(h -> !h.getDate().isAfter(today))
                        .forEach(allPastHolidays::add);
            }

            // Stop early if we have enough
            if (allPastHolidays.size() >= Constants.LAST_HOLIDAYS_COUNT) {
                break;
            }
        }

        // Sort by date descending and take top 3
        return allPastHolidays.stream()
                .sorted(Comparator.comparing(Holiday::getDate).reversed())
                .limit(Constants.LAST_HOLIDAYS_COUNT)
                .map(h -> new HolidayResponseDto(h.getDate(), h.getLocalName(), h.getName(), h.getCountryCode()))
                .collect(Collectors.toList());
    }

    // 2. Given a year and country codes, for each country return a number of public
    // holidays not falling on weekends (sort in descending order)
    public Map<String, Long> getNonWeekendHolidayCounts(int year, List<String> countryCodes) {
        List<String> invalidCodes = countryCodes.stream()
                .filter(code -> !countryValidationService.isSupported(code))
                .collect(Collectors.toList());

        if (!invalidCodes.isEmpty()) {
            throw new UnsupportedOperationException("Invalid countries: " + invalidCodes);
        }
        Map<String, Long> result = new HashMap<>();
        for (String code : countryCodes) {
            List<Holiday> holidays = nagerDateApiClient.getPublicHolidays(year, code);
            long count = holidays.stream()
                    .filter(h -> {
                        DayOfWeek day = h.getDate().getDayOfWeek();
                        return day != DayOfWeek.SATURDAY && day != DayOfWeek.SUNDAY;
                    })
                    .count();
            result.put(code, count);
        }
        // Sort by count descending
        return result.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, e2) -> e1,
                        LinkedHashMap::new));
    }

    // 3. Given a year and 2 country codes, return deduplicated list of dates
    // celebrated in both countries (date + local names)
    public List<DeduplicatedHolidayDto> getDeduplicatedHolidays(int year, String countryCode1, String countryCode2) {
        List<Holiday> holidays1 = nagerDateApiClient.getPublicHolidays(year, countryCode1);
        List<Holiday> holidays2 = nagerDateApiClient.getPublicHolidays(year, countryCode2);

        // Map date -> holiday for country 1
        Map<LocalDate, Holiday> holidayMap1 = holidays1.stream()
                .collect(Collectors.toMap(Holiday::getDate, h -> h, (h1, h2) -> h1));

        // Find common dates and collect local names
        return holidays2.stream()
                .filter(h2 -> holidayMap1.containsKey(h2.getDate()))
                .map(h2 -> {
                    Holiday h1 = holidayMap1.get(h2.getDate());
                    List<String> names = new ArrayList<>();
                    names.add(h1.getLocalName());
                    if (!h2.getLocalName().equals(h1.getLocalName())) {
                        names.add(h2.getLocalName());
                    }
                    return new DeduplicatedHolidayDto(h2.getDate(), names);
                })
                .sorted(Comparator.comparing(DeduplicatedHolidayDto::getDate))
                .collect(Collectors.toList());
    }
}
