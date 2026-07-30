package org.example.marketplace.service;

import org.example.marketplace.dto.category.CategoryRequest;
import org.example.marketplace.dto.category.CategoryResponse;
import org.example.marketplace.entity.Category;
import org.example.marketplace.exception.NotFoundException;
import org.example.marketplace.mapper.CategoryMapper;
import org.example.marketplace.repository.CategoryRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.List;

@Service
public class CategoryService {
    private final CategoryRepository repository;
    private final CategoryMapper mapper;

    public CategoryService(CategoryRepository repository, CategoryMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Transactional(readOnly = true)
    public List<CategoryResponse> getAllCategory() {
        return repository.findAll().stream().map(mapper::entityToDto).toList();
    }

    @Transactional(readOnly = true)
    public CategoryResponse getCategory(Long id) {
        Category category =  repository.findById(id).orElseThrow(() -> new NotFoundException("Category " + id + " not found"));
        return mapper.entityToDto(category);
    }

    @Transactional
    @ResponseStatus(HttpStatus.CREATED)
    public CategoryResponse addCategory(CategoryRequest product) {
        Category category = repository.save(mapper.dtoToEntity(product));
        return mapper.entityToDto(category);
    }

    @Transactional
    public CategoryResponse updateCategory(Long id, CategoryRequest category) {
        Category old = repository.findById(id).orElseThrow(() -> new NotFoundException("Category " + id + " not found"));
        old.setName(category.name());
        old.setDescription(category.description());

        return mapper.entityToDto(old);
    }

    @Transactional
    public void delete(Long id) {
        if(!repository.existsById(id)) throw new NotFoundException("Category " + id + " not found");
        repository.deleteById(id);
    }
}
