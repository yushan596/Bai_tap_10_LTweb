package vn.iotstar.exception;

/** Token sai dinh dang / sai chu ky / sai thuat toan. */
public class InvalidJwtException extends RuntimeException {
    public InvalidJwtException(String message) { super(message); }
    public InvalidJwtException(String message, Throwable cause) { super(message, cause); }
}
