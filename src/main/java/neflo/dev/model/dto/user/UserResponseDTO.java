package neflo.dev.model.dto.user;

import java.util.UUID;

public record UserResponseDTO(
        UUID id,
        String email,
        String name,
        String nickname,
        String pfp
) {
}
