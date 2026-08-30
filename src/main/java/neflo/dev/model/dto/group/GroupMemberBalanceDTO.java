package neflo.dev.model.dto.group;

public record GroupMemberBalanceDTO(
        String nickname,
        int timeBalance,
        int kmBalance
) {
}
