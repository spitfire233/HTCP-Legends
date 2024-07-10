package it.unimib.sd2024.exceptions;

public class R2dbFileNotFoundException extends RuntimeException{
    public R2dbFileNotFoundException() {
        super();
    }

    public R2dbFileNotFoundException(String message) {
        super(message);
    }
}
