package com.acn.holidayapi.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class HolidayResponseDto {
    private LocalDate date;
    private String localName;
    private String name;
    private String countryCode;
}
