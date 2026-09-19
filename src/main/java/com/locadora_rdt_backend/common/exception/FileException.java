package com.locadora_rdt_backend.common.exception;

public class FileException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public FileException(String message) {
        super(message);
    }

    public FileException(String message, Throwable cause) {
        super(message, cause);
    }
}

