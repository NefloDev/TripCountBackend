package neflo.dev.exceptions;

public class ValidationException extends CustomRuntimeException {

    public ValidationException(String errorCode, String message) {
        super(errorCode, message);
    }

}
