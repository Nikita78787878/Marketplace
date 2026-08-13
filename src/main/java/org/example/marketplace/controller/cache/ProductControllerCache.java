package org.example.marketplace.controller.cache;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.example.marketplace.dto.product.ProductResponse;
import org.example.marketplace.dto.product.UpdateProductRequest;
import org.example.marketplace.service.cache.ProductServiceCache;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Товары c кешем")
@RestController
@RequestMapping("/api/v1/products/cache")
@AllArgsConstructor
public class ProductControllerCache {
    private final ProductServiceCache service;



    @Operation(summary = "Получить товар по id with cache")
    @ApiResponse(responseCode = "200", description = "Товар найден")
    @ApiResponse(responseCode = "404", description = "Товар не найден")
    @GetMapping("/{id}")
    public ProductResponse getProductCache(@PathVariable Long id){
        return service.getProductCacheAndQuery(id);
    }

    @PutMapping("/{id}")
    public ProductResponse updateProduct(@RequestBody @Valid UpdateProductRequest product,
                                         @PathVariable Long id){
        return  service.updateProduct(id, product);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteProduct(@PathVariable Long id){
        service.delete(id);
    }


}
