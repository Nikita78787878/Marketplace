package org.example.marketplace.mapper;

import org.example.marketplace.dto.product.CreateProductRequest;
import org.example.marketplace.dto.product.ProductResponse;
import org.example.marketplace.entity.Category;
import org.example.marketplace.entity.Product;
import org.example.marketplace.entity.Tag;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ProductMapper {
    private final CategoryMapper categoryMapper;
    private final TagMapper tagMapper;

    public ProductMapper(CategoryMapper categoryMapper, TagMapper tagMapper) {
        this.categoryMapper = categoryMapper;
        this.tagMapper = tagMapper;
    }

    public ProductResponse entityToDto(Product product){
        return new ProductResponse(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getStockQuantity(),
                categoryMapper.entityToDto(product.getCategory()),
                product.getTags().stream().map(tagMapper::entityToDto).toList()
        );
    }

    public Product dtoToEntity(CreateProductRequest product, Category category, List<Tag> tags){

        return new Product(
                product.name(),
                product.description(),
                product.price(),
                product.stockQuantity(),
                category,
                tags
        );
    }
}
