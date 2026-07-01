package org.example.marketplace.mapper;

import org.example.marketplace.dto.ProductDto;
import org.example.marketplace.entity.Product;
import org.springframework.stereotype.Component;

@Component
public class ProductMapper {

    public ProductDto entityToDto(Product product){
        ProductDto dto = new ProductDto(
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getStockQuantity()
        );

        return dto;
    }
}
