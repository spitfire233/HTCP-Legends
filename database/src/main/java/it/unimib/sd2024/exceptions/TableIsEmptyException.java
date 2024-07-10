package it.unimib.sd2024.exceptions;

public class TableIsEmptyException extends Exception{
    public TableIsEmptyException() {
        super();
    }

    public TableIsEmptyException(String message) {
        super(message);
    }
}
