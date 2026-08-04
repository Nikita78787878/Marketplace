package org.example.marketplace.mapper;

import org.example.marketplace.dto.product.ProductResponse;
import org.example.marketplace.entity.Category;
import org.example.marketplace.entity.Product;
import org.example.marketplace.entity.Tag;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * УРОВЕНЬ 1: ЮНИТ-ТЕСТ.
 *
 * Никакого Spring, никакой базы, никаких моков. Просто создаём объект
 * руками через new и проверяем чистую логику преобразования.
 *
 * Признак юнит-теста: нет ни одной аннотации Spring, тест стартует
 * за миллисекунды. Если для теста нужно поднять контекст — это уже не юнит.
 *
 * Что проверяем: маппер правильно перекладывает поля и не теряет вложенные
 * объекты. Что НЕ проверяем: работает ли HTTP, доходит ли запрос до базы,
 * корректен ли SQL. Для этого есть уровни 2 и 3.
 */
class ProductMapperTest {

    // зависимости собираем руками — в юнит-тесте Spring их не внедряет
    private final ProductMapper mapper = new ProductMapper(new CategoryMapper(), new TagMapper());

    @Test
    void entityToDto_переноситВсеПоляВключаяКатегориюИТеги() {
        Category category = new Category("Смартфоны", "Мобильные телефоны");
        Tag tag = new Tag("Новинка");
        Product product = new Product(
                "iPhone 15",
                "Apple smartphone",
                new BigDecimal("99999.99"),
                10,
                category,
                List.of(tag)
        );

        ProductResponse dto = mapper.entityToDto(product);

        assertThat(dto.name()).isEqualTo("iPhone 15");
        assertThat(dto.price()).isEqualByComparingTo("99999.99");
        assertThat(dto.stockQuantity()).isEqualTo(10);

        // главное, ради чего переделывали DTO: вложенные объекты, а не голые id
        assertThat(dto.category().name()).isEqualTo("Смартфоны");
        assertThat(dto.tags()).hasSize(1);
        assertThat(dto.tags().get(0).name()).isEqualTo("Новинка");
    }
}
