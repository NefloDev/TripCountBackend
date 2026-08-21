package neflo.dev.model.dto.group;

import java.util.UUID;

public record GroupMemberDTO(
        UUID id,
        String nickname
) {
}
