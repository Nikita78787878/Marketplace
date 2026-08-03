package org.example.marketplace.dto.product;

import org.example.marketplace.dto.category.CategoryResponse;
import org.example.marketplace.dto.tag.TagResponse;

import java.math.BigDecimal;
import java.util.List;

public record ProductResponse(Long id,
                              String name,
                              String description,
                              BigDecimal price,
                              Integer stockQuantity,
                              CategoryResponse category,
                              List<TagResponse> tags) {
}
