package org.bootcamp.com.exception;

public class DataPersistenceException extends RuntimeException {
    public DataPersistenceException(Exception e) {
        super(e);
    }
    public DataPersistenceException(String s) {
        super(s);
    }
}
