package com.globallogic.test.etl;

public class TsvValidationException extends RuntimeException {

    public TsvValidationException() {
        super();
    }

    public TsvValidationException(String message) {
        super(message);
    }
}
