package neflo.dev.model.dto;

import java.time.LocalDate;

public record TripDTO(
        String driver,
        LocalDate date,
        Integer durationMinutes,
        String origin,
        String destination,
        String notes
) {
}
