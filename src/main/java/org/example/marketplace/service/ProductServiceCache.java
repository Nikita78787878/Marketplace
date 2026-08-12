package org.example.marketplace.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.marketplace.dto.product.ProductResponse;
import org.example.marketplace.dto.product.UpdateProductRequest;
import org.example.marketplace.cache.ProductCacheKeys;
import org.example.marketplace.event.ProductChangedEvent;
import org.example.marketplace.entity.Category;
import org.example.marketplace.entity.Product;
import org.example.marketplace.entity.Tag;
import org.example.marketplace.exception.NotFoundException;
import org.example.marketplace.mapper.ProductMapper;
import org.example.marketplace.repository.CategoryRepository;
import org.example.marketplace.repository.ProductRepository;
import org.example.marketplace.repository.TagRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.List;

@Service
@AllArgsConstructor
@Slf4j
public class ProductServiceCache {
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final TagRepository tagRepository;
    private final ProductMapper mapper;
    private final ApplicationEventPublisher eventPublisher; // нужен для кеша

    private final RedisTemplate<String, ProductResponse> redisTemplate;
    private static final Duration CACHE_TTL = Duration.ofMinutes(1);



    // Транзакция нужна на cache miss: маппер дёргает ленивые tags/category, OSIV выключен.
    // На cache hit транзакция открывается вхолостую, но JDBC-коннекшн Hibernate берёт лениво — пул не страдает.
    @Transactional(readOnly = true)
    public ProductResponse getProductCacheAndQuery(Long id) {
        String key = ProductCacheKeys.key(id);

        ProductResponse cached = redisTemplate.opsForValue()
                .get(key);

        if(cached != null){
            log.info("Получили из кеша"); // Времено для отладки
            return cached;
        }

        Product entity = productRepository.findByIdWithCategoryAndTags(id).orElseThrow(() -> new NotFoundException("Product " + id + " not found"));

        ProductResponse response = mapper.entityToDto(entity);
        try {
            redisTemplate.opsForValue().set(key, response, CACHE_TTL);
            log.info("Пришлось ложить в кеш");
        } catch (Exception e) {
            log.error("не получилось прочитать из кеша");
        }


        return response;
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

        eventPublisher.publishEvent(new ProductChangedEvent(id));

        return mapper.entityToDto(oldProduct);
    }

    @Transactional
    public void delete(Long id) {
        if(!productRepository.existsById(id)) throw new NotFoundException("Product " + id + " not found");
        productRepository.deleteById(id);
        eventPublisher.publishEvent(new ProductChangedEvent(id));
    }


    @Transactional(readOnly = true)
    public Page<ProductResponse> getPageProducts(Pageable pageble) {
        Page<Product> pageProductCategory = productRepository.findPageWithCategory(pageble);
        Page<ProductResponse> pageProductDto = pageProductCategory.map(mapper::entityToDto);
        return pageProductDto;
    }
}
