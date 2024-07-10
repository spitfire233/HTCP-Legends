package it.unimib.sd2024.exceptions;

public class KeyAlreadyBoundException extends RuntimeException {
    public KeyAlreadyBoundException() {
        super();
    }

    public KeyAlreadyBoundException(String message) {
        super(message);
    }
}
