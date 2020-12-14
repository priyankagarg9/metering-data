package com.meter.energy.consumption.exception;

public class ApplicationException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public ApplicationException() {
        super();
    }

    public ApplicationException(final Throwable cause) {
        super(cause);
    }

    public ApplicationException(final String message) {
        super(message);
    }
}
