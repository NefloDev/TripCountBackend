package neflo.dev.model.dto.user;

import java.util.Optional;

public record UserDTO(
        String email,
        Optional<String> password,
        String name,
        String nickname,
        Optional<String> pfp
) {
}
