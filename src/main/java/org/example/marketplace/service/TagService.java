package org.example.marketplace.service;

import org.example.marketplace.entity.Product;
import org.example.marketplace.entity.Tag;
import org.example.marketplace.repository.ProductRepository;
import org.example.marketplace.repository.TagRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TagService {
    private final TagRepository repository;

    public TagService(TagRepository repository) {
        this.repository = repository;
    }

    public List<Tag> getAllProduct() {
        return repository.findAll();
    }

    public Tag getProduct(Long id) {
        return repository.getById(id); // 'getById(ID)' is deprecated что тогда вызывать?
    }

    public Tag addProduct(Tag product) {
        return repository.save(product);
    }

    public Tag updateProduct(Long id) {
        return repository.getById(id); // тут подумать надо
    }

    public void delete(Long id) {
        repository.deleteById(id);
    }
}
