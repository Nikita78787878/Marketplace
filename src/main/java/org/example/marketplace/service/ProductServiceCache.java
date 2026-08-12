package org.example.marketplace.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.marketplace.cache.ProductCacheKeys;
import org.example.marketplace.dto.product.ProductResponse;
import org.example.marketplace.dto.product.UpdateProductRequest;
import org.example.marketplace.entity.Category;
import org.example.marketplace.entity.Product;
import org.example.marketplace.entity.Tag;
import org.example.marketplace.event.ProductChangedEvent;
import org.example.marketplace.exception.NotFoundException;
import org.example.marketplace.mapper.ProductMapper;
import org.example.marketplace.repository.CategoryRepository;
import org.example.marketplace.repository.ProductRepository;
import org.example.marketplace.repository.TagRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.List;

/**
 * Товары с кешированием в Redis, написанным вручную — паттерн cache-aside.
 *
 * Читаем: сначала кеш, при промахе — БД, результат кладём обратно в кеш.
 * Пишем: в кеш не пишем ничего, а удаляем ключ после коммита (см. ProductCacheEvictor).
 *
 * Кешируем DTO, а не entity. Entity после десериализации из Redis окажется вне Hibernate
 * Session, и первое же обращение к ленивым tags/category упадёт с LazyInitializationException
 * (OSIV в проекте выключен). Плюс правило проекта: наружу отдаём только DTO.
 *
 * Рядом лежит ProductService с некешированной версией той же ручки — специально оставлен
 * для сравнения логов: /api/v1/products/{id} против /api/v1/products/cache/{id}.
 */
@Service
@AllArgsConstructor
@Slf4j
public class ProductServiceCache {
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final TagRepository tagRepository;
    private final ProductMapper mapper;

    // Через него сервис сообщает «товар изменился», не зная про существование Redis.
    // Событие ловит ProductCacheEvictor и чистит кеш — но только после коммита транзакции.
    private final ApplicationEventPublisher eventPublisher;

    private final RedisTemplate<String, ProductResponse> redisTemplate;

    // TTL нужен не только чтобы ограничить память. Он ограничивает время жизни рассинхрона
    // с базой: инвалидация может не доехать (Redis лежал) или проиграть гонку читателю,
    // и тогда TTL гарантирует, что протухшее значение умрёт максимум через минуту.
    private static final Duration CACHE_TTL = Duration.ofMinutes(1);



    // Транзакция нужна на cache miss: маппер дёргает ленивые tags/category, OSIV выключен.
    // На cache hit транзакция открывается вхолостую, но JDBC-коннекшн Hibernate берёт лениво — пул не страдает.
    //
    // Разнести на «публичный метод без транзакции + приватный с транзакцией» нельзя:
    // @Transactional работает через прокси, а вызов this.method() идёт мимо него и аннотация
    // молча игнорируется (self-invocation). Для этого пришлось бы выносить загрузку в отдельный бин.
    @Transactional(readOnly = true)
    public ProductResponse getProductCacheAndQuery(Long id) {
        String key = ProductCacheKeys.key(id);

        // Шаг 1: спрашиваем Redis.
        // Ошибку чтения глушим намеренно (graceful degradation): недоступный кеш — не повод
        // ронять запрос. cached останется null, и метод пойдёт в БД как при обычном промахе.
        // Без этого try/catch упавший Redis превращал бы каждый GET в 500.
        ProductResponse cached = null;
        try {
            cached = redisTemplate.opsForValue().get(key);
        } catch (Exception e) {
            log.error("Не удалось прочитать кеш, key={}", key, e);
        }

        if(cached != null){
            log.debug("Cache hit, key={}", key);
            return cached;
        }

        // Шаг 2: промах — идём в БД.
        // findByIdWithCategoryAndTags, а не findById: внутри JOIN FETCH обеих связей.
        // С обычным findById маппер догружал бы category и tags отдельными запросами — 3 SQL вместо 1.
        log.debug("Cache miss, key={}", key);
        Product entity = productRepository.findByIdWithCategoryAndTags(id).orElseThrow(() -> new NotFoundException("Product " + id + " not found"));

        // Шаг 3: кладём в кеш.
        // Маппер вызываем один раз и переиспользуем результат — в Redis и клиенту должно уйти
        // буквально одно и то же значение, а не два независимо собранных объекта.
        ProductResponse response = mapper.entityToDto(entity);
        try {
            redisTemplate.opsForValue().set(key, response, CACHE_TTL);
        } catch (Exception e) {
            // Данные клиенту уже готовы — не сумели закешировать, и ладно, просто отдаём их.
            log.error("Не удалось записать в кеш, key={}", key, e);
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

        // Кеш здесь НЕ трогаем напрямую: redisTemplate.delete(key) выполнился бы ДО коммита
        // (коммит делает транзакционный прокси уже после выхода из метода) и открыл бы окно
        // для гонки. publishEvent откладывает инвалидацию до AFTER_COMMIT — см. ProductCacheEvictor.
        eventPublisher.publishEvent(new ProductChangedEvent(id));

        return mapper.entityToDto(oldProduct);
    }

    @Transactional
    public void delete(Long id) {
        // Без явной проверки Spring Data 3+ на несуществующий id молча ничего не делает,
        // и клиент получает 200 на удаление того, чего нет.
        if(!productRepository.existsById(id)) throw new NotFoundException("Product " + id + " not found");
        productRepository.deleteById(id);

        // Инвалидация нужна и при удалении: иначе удалённый товар продолжал бы отдаваться
        // из кеша до истечения TTL — клиент видел бы то, чего в базе уже нет.
        eventPublisher.publishEvent(new ProductChangedEvent(id));
    }

}
