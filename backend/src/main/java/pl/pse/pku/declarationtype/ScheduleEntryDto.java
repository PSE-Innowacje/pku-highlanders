package pl.pse.pku.declarationtype;

public record ScheduleEntryDto(
    Long id,
    String position,
    int day,
    int hour,
    String dayType
) {}
