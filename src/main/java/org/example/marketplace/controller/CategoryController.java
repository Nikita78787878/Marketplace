package org.example.marketplace.controller;

import org.example.marketplace.entity.Category;
import org.example.marketplace.entity.Product;
import org.example.marketplace.service.CategoryService;
import org.example.marketplace.service.ProductService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/categories")
public class CategoryController {
    private final CategoryService service;

    public CategoryController(CategoryService service) {
        this.service = service;
    }

    @GetMapping()
    public List<Category> getAllCategory(){
        return service.getAllProduct();
    }

    @GetMapping("/{id}")
    public Category getCategory(@PathVariable Long id){
        return service.getProduct(id);
    }

    @PostMapping
    public Category addCategory(Category category){
        return  service.addProduct(category);
    }

    @PutMapping("/{id}")
    public Category updateCategory(@PathVariable Long id){
        return  service.updateProduct(id);
    }

    @DeleteMapping("/{id}")
    public String deleteCategory(@PathVariable Long id){
        service.delete(id);
        return "ok";
    }


}
