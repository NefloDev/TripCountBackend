package neflo.dev.exceptions;

public class UnexpectedException extends CustomRuntimeException {

    public UnexpectedException(String errorCode, String message) {
        super(errorCode, message);
    }

    public UnexpectedException(String errorCode) {
        super(errorCode);
    }

}
