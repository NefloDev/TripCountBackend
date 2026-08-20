package neflo.dev.exceptions;

public class DatabaseException extends CustomRuntimeException {

    public DatabaseException(String errorCode, String message) {
        super(errorCode, message);
    }

    public DatabaseException(String errorCode) {
        super(errorCode);
    }
}
