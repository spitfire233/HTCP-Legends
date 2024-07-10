package it.unimib.sd2024.exceptions;

public class TableAlreadyExistsException  extends Exception{
    public TableAlreadyExistsException() {
        super();
    }

    public TableAlreadyExistsException(String message) {
        super(message);
    }
}
