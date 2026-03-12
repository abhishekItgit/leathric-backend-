package com.leathric.exception;

/**
 * Thrown when a storage provider operation fails.
 */
public class StorageOperationException extends RuntimeException {
    public StorageOperationException(String message) {
        super(message);
    }

    public StorageOperationException(String message, Throwable cause) {
        super(message, cause);
    }
}
