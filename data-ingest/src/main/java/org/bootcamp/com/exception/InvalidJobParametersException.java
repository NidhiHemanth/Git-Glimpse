package org.bootcamp.com.exception;

public class InvalidJobParametersException extends RuntimeException {
    public InvalidJobParametersException(String message) {
        super(message);
    }
    public InvalidJobParametersException(Exception e) {
        super(e);
    }
}