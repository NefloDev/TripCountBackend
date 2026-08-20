package neflo.dev.model.dto.user;

public record UserDTO(
        String email,
        String password,
        String name,
        String nickname,
        byte[] pfp
) {
}
