package neflo.dev.model.dto.group;

import java.util.Optional;

public record GroupRequestDTO(
        String name,
        Optional<String> pfp
) {
}
