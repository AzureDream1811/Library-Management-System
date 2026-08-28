package com.azure.libraryms.common.exception;

public class EntityAlreadyExistsException extends RuntimeException {
    public EntityAlreadyExistsException(String entityName, String field, String value) {
        super(String.format("%s already exists with %s: %s", entityName, field, value));
    }
    
}
