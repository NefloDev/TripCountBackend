package neflo.dev.exceptions;

public class NoEntitiesFoundException extends CustomRuntimeException {

    public NoEntitiesFoundException(String errorCode, String message) {
        super(errorCode, message);
    }

    public NoEntitiesFoundException(String errorCode) {
        super(errorCode);
    }
}
