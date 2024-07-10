package it.unimib.sd2024.exceptions;

public class UnknownCommandException extends RuntimeException {
    public UnknownCommandException() {
        super();
    }

    public UnknownCommandException(String message) {
        super(message);
    }
}
