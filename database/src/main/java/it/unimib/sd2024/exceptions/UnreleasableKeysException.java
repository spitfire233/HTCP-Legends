package it.unimib.sd2024.exceptions;

public class UnreleasableKeysException extends RuntimeException {
    public UnreleasableKeysException() {
        super();
    }

    public UnreleasableKeysException(String message) {
        super(message);
    }
}
