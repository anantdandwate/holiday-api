package com.acn.holidayapi.model;

import lombok.Data;
import java.time.LocalDate;
import java.util.List;

@Data
public class Holiday {
    private LocalDate date;
    private String localName;
    private String name;
    private String countryCode;
    private boolean fixed; // Deprecated in Nager.Date API but kept for compatibility
    private boolean global;
    private List<String> counties;
    private Integer launchYear;// Deprecated in Nager.Date API but kept for compatibility
    private List<String> types;
}
