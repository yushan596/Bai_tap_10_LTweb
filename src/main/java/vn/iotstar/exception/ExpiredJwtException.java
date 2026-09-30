package vn.iotstar.exception;

/** Token da het han. */
public class ExpiredJwtException extends RuntimeException {
    public ExpiredJwtException(String message) { super(message); }
}
