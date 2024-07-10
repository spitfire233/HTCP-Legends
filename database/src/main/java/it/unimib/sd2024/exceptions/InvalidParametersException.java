package it.unimib.sd2024.exceptions;

public class InvalidParametersException extends RuntimeException {
    public InvalidParametersException() {
        super();
    }

    public InvalidParametersException(String message) {
        super(message);
    }
}
