package co.icesi.UniPlan.dto;

import java.util.List;

public class CreateEventRequestDTO {

    private String title;
    private String description;
    private String type;
    private String location;

    private String startDate;
    private String endDate;

    private Integer maxSlots;

    private String sportType;
    private String tournamentType;

    private Integer teamsQuantity;

    private Integer totalHours;

    private List<String> tournamentFormat;

    private EventDetailsDTO eventDetails;
}
