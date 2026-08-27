package neflo.dev.model.dto.group;

import neflo.dev.model.dto.GroupInsightsPeriodTypes;

public record GroupInsightsRequest(
        GroupInsightsPeriodTypes periodType,
        Integer year,
        Integer month
) {
}
