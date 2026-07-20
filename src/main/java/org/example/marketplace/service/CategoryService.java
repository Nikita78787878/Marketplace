package org.example.marketplace.service;

import org.example.marketplace.entity.Category;
import org.example.marketplace.entity.Product;
import org.example.marketplace.repository.CategoryRepository;
import org.example.marketplace.repository.ProductRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoryService {
    private final CategoryRepository repository;

    public CategoryService(CategoryRepository repository) {
        this.repository = repository;
    }

    public List<Category> getAllProduct() {
        return repository.findAll();
    }

    public Category getProduct(Long id) {
        return repository.findById(id).orElseThrow(() -> new RuntimeException("нету данного продукта в БД"));
    }

    public Category addProduct(Category product) {
        return repository.save(product);
    }

    public Category updateProduct(Long id) {
        return repository.findById(id).orElseThrow(() -> new RuntimeException("Нету данного продукта в бд"));
    }

    public void delete(Long id) {
        repository.deleteById(id);
    }
}
