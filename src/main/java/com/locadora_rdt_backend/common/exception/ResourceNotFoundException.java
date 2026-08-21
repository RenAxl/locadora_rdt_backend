package com.locadora_rdt_backend.common.exception;

public class ResourceNotFoundException extends RuntimeException{
    private static final long serialVersionUID = 1L;

    public ResourceNotFoundException (String msg) {
        super(msg);
    }

}
