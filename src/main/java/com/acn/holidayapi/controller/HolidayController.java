package com.acn.holidayapi.controller;

import com.acn.holidayapi.dto.DeduplicatedHolidayDto;
import com.acn.holidayapi.dto.HolidayResponseDto;
import com.acn.holidayapi.service.HolidayService;
import com.acn.holidayapi.util.Constants;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import lombok.extern.slf4j.Slf4j;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/holidays")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Holiday API", description = "Endpoints for retrieving holiday information")
public class HolidayController {
    private final HolidayService holidayService;

    // 1. Given a country, return the last celebrated 3 holidays (date and name)
    @Operation(summary = "Get last 3 holidays for a country", description = "Returns the last 3 holidays for the specified country")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved holidays")
    @GetMapping("/last3/{countryCode}")
    public ResponseEntity<List<HolidayResponseDto>> getLastThreeHolidays(
            @Parameter(description = "ISO country code (e.g., US, GB, DE)", example = "US") @PathVariable("countryCode") @NotBlank(message = "Country code cannot be blank") @Size(min = Constants.MIN_COUNTRY_CODE_LENGTH, max = Constants.MAX_COUNTRY_CODE_LENGTH, message = "Country code must be 2 characters") String countryCode) {
        log.info("Received request for last 3 holidays for country: {}", countryCode);
        List<HolidayResponseDto> holidays = holidayService.getLastThreeHolidays(countryCode);
        log.info("Returning {} holidays for country: {}", holidays.size(), countryCode);
        return ResponseEntity.ok(holidays);
    }

    // 2. Given a year and country codes, for each country return a number of public
    // holidays not falling on weekends (sort in descending order)
    @Operation(summary = "Get non-weekend holiday counts", description = "Returns the number of public holidays not falling on weekends for each specified country")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved holiday counts")
    @GetMapping("/non-weekend-count")
    public ResponseEntity<Map<String, Long>> getNonWeekendHolidayCounts(
            @Parameter(description = "Year", example = "2024") @RequestParam("year") @Min(value = 1900, message = "Year must be at least 1900") int year,
            @Parameter(description = "List of country codes", example = "[\"US\", \"CA\"]") @RequestParam("countryCodes") @NotNull(message = "Country codes list cannot be null") List<@NotBlank(message = "Country code cannot be blank") @Size(min = Constants.MIN_COUNTRY_CODE_LENGTH, max = Constants.MAX_COUNTRY_CODE_LENGTH, message = "Country code must be 2 characters") String> countryCodes) {
        Map<String, Long> result = holidayService.getNonWeekendHolidayCounts(year, countryCodes);
        return ResponseEntity.ok(result);
    }

    // 3. Given a year and 2 country codes, return deduplicated list of dates
    // celebrated in both countries (date + local names)
    @Operation(summary = "Get deduplicated holidays", description = "Returns a deduplicated list of dates celebrated in both specified countries")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved deduplicated holidays")
    @GetMapping("/deduplicated")
    public ResponseEntity<List<DeduplicatedHolidayDto>> getDeduplicatedHolidays(
            @Parameter(description = "Year", example = "2024") @RequestParam("year") int year,
            @Parameter(description = "First country code", example = "US") @RequestParam("countryCode1") @NotBlank(message = "Country code cannot be blank") @Size(min = Constants.MIN_COUNTRY_CODE_LENGTH, max = Constants.MAX_COUNTRY_CODE_LENGTH, message = "Country code must be 2 characters") String countryCode1,
            @Parameter(description = "Second country code", example = "CA") @RequestParam("countryCode2") @NotBlank(message = "Country code cannot be blank") @Size(min = Constants.MIN_COUNTRY_CODE_LENGTH, max = Constants.MAX_COUNTRY_CODE_LENGTH, message = "Country code must be 2 characters") String countryCode2) {
        List<DeduplicatedHolidayDto> result = holidayService.getDeduplicatedHolidays(year, countryCode1, countryCode2);
        return ResponseEntity.ok(result);
    }
}
