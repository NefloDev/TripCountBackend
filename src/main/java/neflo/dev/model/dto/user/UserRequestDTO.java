package neflo.dev.model.dto.user;

import java.util.UUID;

public record UserRequestDTO(
        String email,
        UUID uuid
) {
}
