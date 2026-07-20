package org.example.marketplace.service;

import org.example.marketplace.dto.CreateProductRequest;
import org.example.marketplace.dto.ProductResponse;
import org.example.marketplace.dto.UpdateProductRequest;
import org.example.marketplace.entity.Category;
import org.example.marketplace.entity.Product;
import org.example.marketplace.entity.Tag;
import org.example.marketplace.mapper.ProductMapper;
import org.example.marketplace.repository.CategoryRepository;
import org.example.marketplace.repository.ProductRepository;
import org.example.marketplace.repository.TagRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductService {
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final TagRepository tagRepository;
    private final ProductMapper mapper;

    public ProductService(ProductRepository productRepository, CategoryRepository categoryRepository, TagRepository tagRepository, ProductMapper mapper) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.tagRepository = tagRepository;
        this.mapper = mapper;
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> getAllProduct() {
        return productRepository.findAll()
                .stream()
                .map(mapper::entityToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ProductResponse getProduct(Long id) {
        // findById возвращает Optional — безопаснее getById (deprecated): не упадём с NPE, сами решаем что делать при отсутствии
        Product entity = productRepository.findById(id).orElseThrow(() -> new RuntimeException("not found" + id));
        return mapper.entityToDto(entity);
    }

    public ProductResponse addProduct(CreateProductRequest product) {

        // сущности достаём в сервисе (в транзакции) и передаём в маппер готовыми — маппер в БД не ходит
        List<Tag> tag = tagRepository.findAllById(product.tags());
        Category category = categoryRepository.findById(product.categoryId()).orElseThrow(() -> new RuntimeException("Нету категории"));

        Product entity = mapper.dtoToEntity(product, category, tag);
        Product saveEntity = productRepository.save(entity);
        return mapper.entityToDto(saveEntity);
    }

    @Transactional
    public ProductResponse updateProduct(Long id,
                                         UpdateProductRequest productRequest) {
        Product oldProduct = productRepository.findById(id).orElseThrow(() -> new RuntimeException("Нету в бд такого товара"));

        oldProduct.setName(productRequest.name());
        oldProduct.setDescription(productRequest.description());
        oldProduct.setPrice(productRequest.price());
        oldProduct.setStockQuantity(productRequest.stockQuantity());

        Category category = categoryRepository.findById(productRequest.categoryId()).orElseThrow(() -> new RuntimeException("Нету категории"));
        oldProduct.setCategory(category);

        List<Tag> tag = tagRepository.findAllById(productRequest.tags());
        oldProduct.setTags(tag);

        return mapper.entityToDto(oldProduct);
    }

    public void delete(Long id) {
        productRepository.deleteById(id);
    }
}
