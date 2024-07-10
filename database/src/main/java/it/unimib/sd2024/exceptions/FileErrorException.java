package it.unimib.sd2024.exceptions;

public class FileErrorException extends Exception{
    public FileErrorException(String message){
        super(message);
    }
    public FileErrorException(){
        super();
    }
}
