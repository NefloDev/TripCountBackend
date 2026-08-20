package neflo.dev.exceptions;

public class AuthenticationException extends CustomRuntimeException {

    public AuthenticationException(String errorCode, String message) {
        super(errorCode, message);
    }

    public AuthenticationException(String errorCode) {
        super(errorCode);
    }
}
