package pl.pse.pku.declarationtype;

import java.util.List;

public record SaveScheduleRequest(List<ScheduleEntryDto> entries) {}
