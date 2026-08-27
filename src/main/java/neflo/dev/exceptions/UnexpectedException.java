package neflo.dev.exceptions;

public class UnexpectedException extends CustomRuntimeException {

    public UnexpectedException(String errorCode, String message, Throwable cause) {
        super(errorCode, message, cause);
    }

}
