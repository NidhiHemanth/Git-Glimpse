package org.bootcamp.com.exception;

public class JobExecutionFailedException extends RuntimeException {
    public JobExecutionFailedException(String message) {
        super(message);
    }
}