package com.codewithmosh.store.payments;

public class StockRollbackException extends RuntimeException {
    public StockRollbackException(String message, Throwable cause) {
        super(message, cause);
    }

    public StockRollbackException(String message) {
        super(message);
    }
}
