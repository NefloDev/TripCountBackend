package neflo.dev.model.dto;

public record LoginResponse(
        String token,
        long expiresOn
) {
}
