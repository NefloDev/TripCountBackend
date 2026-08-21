package neflo.dev.model.dto.trip;

import java.time.LocalDate;

public record TripRequestDTO(
        String driver,
        LocalDate date,
        Integer durationMinutes,
        String origin,
        String destination,
        String notes
) {
}
