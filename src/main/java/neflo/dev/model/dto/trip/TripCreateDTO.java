package neflo.dev.model.dto.trip;

import java.time.LocalDate;
import java.util.UUID;

public record TripCreateDTO(
        UUID driver,
        LocalDate date,
        Integer durationMinutes,
        String origin,
        String destination,
        String notes
) {
}
