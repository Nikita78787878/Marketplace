package org.example.marketplace.dto.product;

import java.math.BigDecimal;
import java.util.List;

public record ProductResponse(Long id,
                              String name,
                              String description,
                              BigDecimal price,
                              Integer stockQuantity,
                              Long category,
                              List<Long> tags) {
}
