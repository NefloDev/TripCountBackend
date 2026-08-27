package neflo.dev.model.dto;

import neflo.dev.model.dto.group.GroupInsightsRequest;

public record YearMonth(
        int year,
        int month
) {

    public static YearMonth fromGroupInsightsRequest(GroupInsightsRequest request) {
        return new YearMonth(request.year(), request.month());
    }

}
