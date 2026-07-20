package org.example.marketplace.mapper;

import org.example.marketplace.dto.CreateProductRequest;
import org.example.marketplace.dto.ProductResponse;
import org.example.marketplace.entity.Category;
import org.example.marketplace.entity.Product;
import org.example.marketplace.repository.CategoryRepository;
import org.springframework.stereotype.Component;

@Component
public class ProductMapper {
    private final CategoryRepository categoryRepository;

    public ProductMapper(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    public ProductResponse entityToDto(Product product){
        return new ProductResponse(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getStockQuantity()
        );
    }

    public Product dtoToEntity(CreateProductRequest product){
        Category category = categoryRepository.findById(product.categoryId()).orElseThrow(() -> new RuntimeException("Category not found"));

        return new Product(
                product.name(),
                product.description(),
                product.price(),
                product.stockQuantity(),
                category
        );
    }
}
