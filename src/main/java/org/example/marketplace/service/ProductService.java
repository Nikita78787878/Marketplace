package org.example.marketplace.service;

import org.example.marketplace.dto.CreateProductRequest;
import org.example.marketplace.dto.ProductResponse;
import org.example.marketplace.entity.Product;
import org.example.marketplace.mapper.ProductMapper;
import org.example.marketplace.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ProductService {
    private final ProductRepository repository;
    private final ProductMapper mapper;

    public ProductService(ProductRepository repository, ProductMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> getAllProduct() {
        return repository.findAll()
                .stream()
                .map(mapper::entityToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ProductResponse getProduct(Long id) {
        Product entity = repository.findById(id).orElseThrow(() -> new RuntimeException("not found" + id)); // 'getById(ID)' is deprecated что тогда вызывать?
        return mapper.entityToDto(entity);
    }

    public ProductResponse addProduct(CreateProductRequest product) {
        Product entity = mapper.dtoToEntity(product);
        Product saveEntity = repository.save(entity);
        return mapper.entityToDto(saveEntity);
    }

    public Optional<Product> updateProduct(Long id) {
        return repository.findById(id); // тут подумать надо
    }

    public void delete(Long id) {
        repository.deleteById(id);
    }
}
