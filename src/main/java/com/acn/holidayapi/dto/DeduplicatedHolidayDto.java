package com.acn.holidayapi.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeduplicatedHolidayDto {
    private LocalDate date;
    private List<String> localNames; // Both countries' local names
}
