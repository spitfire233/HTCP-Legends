package it.unimib.sd2024.exceptions;
public class UndefinedKeyException extends RuntimeException {
    public UndefinedKeyException() {
        super();
    }

    public UndefinedKeyException(String message) {
        super(message);
    }
}
