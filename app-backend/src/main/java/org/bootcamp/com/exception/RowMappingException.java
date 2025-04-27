package org.bootcamp.com.exception;

public class RowMappingException extends RuntimeException {
    public RowMappingException(Exception e) {
        super(e);
    }
}
