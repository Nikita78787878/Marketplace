package org.example.marketplace.dto;

import java.math.BigDecimal;

public record ProductDto(String name,
                         String description,
                         BigDecimal price,
                         Integer stockQuantity) {
}
