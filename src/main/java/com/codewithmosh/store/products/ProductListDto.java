package com.codewithmosh.store.products;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class ProductListDto {
    private Long id;
    private String name;
    private BigDecimal price;
    private String description;
    private Boolean inStock;
    private Long categoryId;
    private String categoryName;
}
