package neflo.dev.model.dto.user;

public record UserResponseDTO(
        String email,
        String name,
        String nickname,
        String pfp
) {
}
