package it.unimib.sd2024.exceptions;

public class TimeRangeException extends RuntimeException {
    public TimeRangeException() {
        super();
    }

    public TimeRangeException(String message) {
        super(message);
    }
}
