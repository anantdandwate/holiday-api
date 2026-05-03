package com.acn.holidayapi.controller;

import com.acn.holidayapi.HolidayApiApplication;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = HolidayApiApplication.class)
@AutoConfigureMockMvc
class HolidayControllerIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @Test
    void testGetLastThreeHolidaysEndpoint() throws Exception {
        mockMvc.perform(get("/api/holidays/last3/US"))
                .andExpect(status().isOk());
    }

    @Test
    void testGetNonWeekendHolidayCountsEndpoint() throws Exception {
        mockMvc.perform(get("/api/holidays/non-weekend-count")
                .param("year", "2024")
                .param("countryCodes", "US"))
                .andExpect(status().isOk());
    }

    @Test
    void testGetDeduplicatedHolidaysEndpoint() throws Exception {
        mockMvc.perform(get("/api/holidays/deduplicated")
                .param("year", "2024")
                .param("countryCode1", "US")
                .param("countryCode2", "DE"))
                .andExpect(status().isOk());
    }
}
