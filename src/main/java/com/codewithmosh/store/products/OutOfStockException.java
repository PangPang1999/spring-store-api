package com.codewithmosh.store.products;

public class OutOfStockException extends RuntimeException {
    public OutOfStockException() {
        super("out of stock");
    }
}

