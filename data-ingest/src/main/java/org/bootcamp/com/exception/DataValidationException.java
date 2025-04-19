package org.bootcamp.com.exception;

public class DataValidationException extends RuntimeException {
    public DataValidationException(String s) {
        super(s);
    }
    public DataValidationException(Throwable throwable) {
        super(throwable);
    }
}
