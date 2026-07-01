package org.example.marketplace.service;

import org.example.marketplace.dto.ProductDto;
import org.example.marketplace.entity.Product;
import org.example.marketplace.mapper.ProductMapper;
import org.example.marketplace.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ProductService {
    private final ProductRepository repository;
    private final ProductMapper mapper;

    public ProductService(ProductRepository repository, ProductMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    public List<Product> getAllProduct() {
        return repository.findAll();
    }

    @Transactional(readOnly = true)
    public ProductDto getProduct(Long id) {
        Product entity = repository.getById(id); // 'getById(ID)' is deprecated что тогда вызывать?
        return mapper.entityToDto(entity);
    }

    public Product addProduct(Product product) {
        return repository.save(product);
    }

    public Product updateProduct(Long id) {
        return repository.getById(id); // тут подумать надо
    }

    public void delete(Long id) {
        repository.deleteById(id);
    }
}
