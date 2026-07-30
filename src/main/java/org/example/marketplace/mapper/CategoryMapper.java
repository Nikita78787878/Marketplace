package org.example.marketplace.mapper;

import org.example.marketplace.dto.category.CategoryRequest;
import org.example.marketplace.dto.category.CategoryResponse;
import org.example.marketplace.entity.Category;
import org.springframework.stereotype.Component;

@Component
public class CategoryMapper {

    public CategoryResponse entityToDto(Category category) {
        return new CategoryResponse(category.getId(), category.getName(),
                category.getDescription());
    }

    public Category dtoToEntity(CategoryRequest category) {
        return new Category(category.name(), category.description());
    }
}
