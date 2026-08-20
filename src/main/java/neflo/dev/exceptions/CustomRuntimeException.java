package neflo.dev.exceptions;

import lombok.Getter;

@Getter
public class CustomRuntimeException extends RuntimeException {

    private final String errorCode;

    public CustomRuntimeException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public CustomRuntimeException(String errorCode) {
        super("");
        this.errorCode = errorCode;
    }
}
