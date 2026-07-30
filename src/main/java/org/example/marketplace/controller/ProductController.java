package org.example.marketplace.controller;

import jakarta.validation.Valid;
import org.example.marketplace.dto.product.CreateProductRequest;
import org.example.marketplace.dto.product.ProductResponse;
import org.example.marketplace.dto.product.UpdateProductRequest;
import org.example.marketplace.service.ProductService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/products")
public class ProductController {
    private final ProductService service;

    public ProductController(ProductService productService) {
        this.service = productService;
    }

    @GetMapping
    public List<ProductResponse> getAllProduct(){
        return service.getAllProduct();
    }

    @GetMapping("/{id}")
    public ProductResponse getProduct(@PathVariable Long id){
        return service.getProduct(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProductResponse addProduct(@Valid @RequestBody CreateProductRequest product){
        return  service.addProduct(product);
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
