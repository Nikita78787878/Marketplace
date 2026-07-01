package org.example.marketplace.controller;

import org.example.marketplace.dto.ProductDto;
import org.example.marketplace.entity.Product;
import org.example.marketplace.service.ProductService;
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
    public List<Product> getAllProduct(){
        return service.getAllProduct();
    }

    @GetMapping("/{id}")
    public ProductDto getProduct(@PathVariable Long id){
        return service.getProduct(id);
    }

    @PostMapping
    public Product addProduct(Product product){
        return  service.addProduct(product);
    }

    @PutMapping("/{id}")
    public Product updateProduct(@PathVariable Long id){
        return  service.updateProduct(id);
    }

    @DeleteMapping("/{id}")
    public String deleteProduct(@PathVariable Long id){
        service.delete(id);
        return "ok";
    }


}
