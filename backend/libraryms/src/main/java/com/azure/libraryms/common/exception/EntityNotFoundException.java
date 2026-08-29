package com.azure.libraryms.common.exception;

public class EntityNotFoundException extends RuntimeException {

    public EntityNotFoundException(String entityName, String field, String value) {
        super(String.format("%s not found with %s: %s", entityName, field, value));
    }
    
}
