package it.unimib.sd2024.exceptions;
public class TimeOverlapException extends RuntimeException {
    public TimeOverlapException() {
        super();
    }

    public TimeOverlapException(String message) {
        super(message);
    }
}
