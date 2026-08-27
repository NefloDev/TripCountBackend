package neflo.dev.model.dto.group;

import java.util.Map;

public record GroupInsights(
        int year,
        Integer month,
        Map<String, Integer> driverDrivingTime
) {
}
