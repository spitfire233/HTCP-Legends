package it.unimib.sd2024.exceptions;

public class R2dbErrorException extends RuntimeException{
    public R2dbErrorException() {
        super();
    }

    public R2dbErrorException(String message) {
        super(message);
    }
}
