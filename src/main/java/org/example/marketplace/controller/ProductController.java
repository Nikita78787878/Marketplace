package org.example.marketplace.controller;

import jakarta.validation.Valid;
import org.example.marketplace.dto.CreateProductRequest;
import org.example.marketplace.dto.ProductResponse;
import org.example.marketplace.entity.Product;
import org.example.marketplace.service.ProductService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

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
    public ProductResponse addProduct(@Valid @RequestBody CreateProductRequest product){
        return  service.addProduct(product);
    }

    @PutMapping("/{id}")
    public Optional<Product> updateProduct(@PathVariable Long id){
        return  service.updateProduct(id);
    }

    @DeleteMapping("/{id}")
    public String deleteProduct(@PathVariable Long id){
        service.delete(id);
        return "ok";
    }


}
