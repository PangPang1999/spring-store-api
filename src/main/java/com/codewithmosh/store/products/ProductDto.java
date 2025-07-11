package com.codewithmosh.store.products;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProductDto {
    private Long id;
    private String name;
    private BigDecimal price;
    private String description;
    private Integer quantity;
    private Long categoryId;
}
