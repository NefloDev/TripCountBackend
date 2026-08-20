package neflo.dev.model.dto.user;

public record UserUpdateRequestDTO(
        String password,
        String name,
        String nickname,
        byte[] pfp
) {
}
