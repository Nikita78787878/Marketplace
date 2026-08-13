package org.example.marketplace.service.cache;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


/**
 * Тот же кеш товара, но через Spring Cache Abstraction (@Cacheable/@CacheEvict) —
 * то есть через AOP-прокси, а не руками.
 *
 * Сравнивать с ProductServiceCache, где то же самое написано вручную. Разница:
 *   - кода в разы меньше: нет RedisTemplate, нет сборки ключа, нет событий и слушателя;
 *   - но потеряна обработка ошибок — при недоступном Redis запрос падает с 500,
 *     тогда как ручная версия деградирует и молча идёт в БД (лечится своим CacheErrorHandler);
 *   - ключи выглядят иначе: Spring строит их как "имяКеша::ключ", то есть product::3,
 *     а ручная версия пишет product:3. Два независимых набора в одном Redis.
 *
 * Настройка сериализации и TTL — не здесь, а в бине CacheManager (CacheConfigurationAOP).
 * Аннотации сами по себе ничего не настраивают.
 */
@Service
@AllArgsConstructor
@Slf4j
public class ProductServiceCacheAOP {
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final TagRepository tagRepository;
    private final ProductMapper mapper;

    /**
     * @Cacheable: есть значение в кеше — возвращается сразу, тело метода НЕ выполняется вообще.
     * Нет — метод вызывается, а его результат кладётся в кеш. Это тот же cache-aside,
     * только шаги «спросить кеш» и «положить в кеш» выполняет прокси, а не ты.
     *
     * key = "#id" — SpEL по имени параметра метода. Без него Spring собрал бы ключ из ВСЕХ
     * аргументов, что для методов с несколькими параметрами даёт неожиданные ключи.
     *
     * @Transactional остаётся: на промахе метод всё-таки идёт в БД, а маппер дёргает
     * ленивые tags/category при выключенном OSIV.
     *
     * Порядок аннотаций в исходнике ни на что не влияет — какой прокси окажется снаружи,
     * определяется приоритетами advisor'ов, а они по умолчанию равны.
     */
    @Cacheable(value = "product", key = "#id")
    @Transactional(readOnly = true)
    public ProductResponse getProductCacheAndQuery(Long id) {
        Product entity = productRepository.findByIdWithCategoryAndTags(id).orElseThrow(() -> new NotFoundException("Product " + id + " not found"));
        ProductResponse response = mapper.entityToDto(entity);

        return response;
    }

    /**
     * @CacheEvict удаляет ключ из кеша. Никакого publishEvent и слушателя, как в ручной версии,
     * здесь не нужно — за отсрочку до коммита отвечает transactionAware() в CacheManager.
     *
     * Без transactionAware() эвикция произошла бы при возврате из метода, то есть ДО коммита
     * (коммит делает внешний транзакционный прокси), и параллельный GET успел бы прогреть кеш
     * ещё не закоммиченными данными.
     *
     * beforeInvocation по умолчанию false — если метод бросит исключение, кеш не тронется.
     * Это правильно: не удалось обновить товар, значит и кеш чистить не за чем.
     */
    @CacheEvict(value = "product", key = "#id")
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

    // Инвалидация нужна и при удалении: иначе удалённый товар продолжал бы отдаваться
    // из кеша до истечения TTL — клиент видел бы то, чего в базе уже нет.
    @CacheEvict(value = "product", key = "#id")
    @Transactional
    public void delete(Long id) {
        // Без явной проверки Spring Data 3+ на несуществующий id молча ничего не делает,
        // и клиент получает 200 на удаление того, чего нет.
        if(!productRepository.existsById(id)) throw new NotFoundException("Product " + id + " not found");
        productRepository.deleteById(id);

    }

}
