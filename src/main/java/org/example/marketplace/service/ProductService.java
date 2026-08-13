package org.example.marketplace.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.marketplace.dto.product.CreateProductRequest;
import org.example.marketplace.dto.product.ProductResponse;
import org.example.marketplace.dto.product.UpdateProductRequest;
import org.example.marketplace.entity.Category;
import org.example.marketplace.entity.Product;
import org.example.marketplace.entity.Tag;
import org.example.marketplace.exception.NotFoundException;
import org.example.marketplace.mapper.ProductMapper;
import org.example.marketplace.repository.CategoryRepository;
import org.example.marketplace.repository.ProductRepository;
import org.example.marketplace.repository.TagRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedModel;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
@Slf4j
public class ProductService {
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final TagRepository tagRepository;
    private final ProductMapper mapper;


    @Transactional(readOnly = true)
    public List<ProductResponse> getAllProduct() {
        return productRepository.findAllWithCategoryAndTags()
                .stream()
                .map(mapper::entityToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ProductResponse getProduct(Long id) {
        // findById возвращает Optional — безопаснее getById (deprecated): не упадём с NPE, сами решаем что делать при отсутствии
        Product entity = productRepository.findById(id).orElseThrow(() -> new NotFoundException("Product " + id + " not found"));
        return mapper.entityToDto(entity);
    }


    @Transactional
    public ProductResponse addProduct(CreateProductRequest product) {

        // сущности достаём в сервисе (в транзакции) и передаём в маппер готовыми — маппер в БД не ходит
        List<Tag> tag = tagRepository.findAllById(product.tags());
        Category category = categoryRepository.findById(product.categoryId()).orElseThrow(() -> new NotFoundException("Нету категории"));

        Product entity = mapper.dtoToEntity(product, category, tag);
        Product saveEntity = productRepository.save(entity);
        return mapper.entityToDto(saveEntity);
    }

    @Transactional
    public ProductResponse updateProduct(Long id,
                                         UpdateProductRequest productRequest) {
        Product oldProduct = productRepository.findById(id).orElseThrow(() -> new NotFoundException("Нету в бд такого товара"));

        oldProduct.setName(productRequest.name());
        oldProduct.setDescription(productRequest.description());
        oldProduct.setPrice(productRequest.price());
        oldProduct.setStockQuantity(productRequest.stockQuantity());

        Category category = categoryRepository.findById(productRequest.categoryId()).orElseThrow(() -> new NotFoundException("Нету категории"));
        oldProduct.setCategory(category);

        List<Tag> tag = tagRepository.findAllById(productRequest.tags());
        oldProduct.setTags(tag);

        return mapper.entityToDto(oldProduct);
    }

    /**
     * В Spring Data 3+ deleteById для несуществующего id ничего не делает и не падает.
     * Клиент удаляет id=99999 → получает 200 OK. На проде: либо if (!repository.existsById(id)) throw new NotFoundException(...),
     * либо findById().orElseThrow() + delete(). И @Transactional сюда же
     */
    @Transactional
    public void delete(Long id) {
        if(!productRepository.existsById(id)) throw new NotFoundException("Product " + id + " not found");
        productRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public PagedModel<ProductResponse> getPageProductsBad(Pageable pageble) {
        Page<Product> pageProduct = productRepository.findAll(pageble); // под капотом понимает даже писать не надо
        Page<ProductResponse> pageProductDto = pageProduct.map(mapper::entityToDto);
        return new PagedModel<>(pageProductDto);

    }

    @Transactional(readOnly = true)
    public Page<ProductResponse> getPageProductsGood(Pageable pageble) {
        Page<Product> pageProductCategory = productRepository.findPageWithCategory(pageble);
        Page<ProductResponse> pageProductDto = pageProductCategory.map(mapper::entityToDto);
        return pageProductDto;
    }
}
