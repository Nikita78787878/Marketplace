package org.example.marketplace;

import jakarta.persistence.EntityManagerFactory;
import org.example.marketplace.entity.Category;
import org.example.marketplace.entity.Product;
import org.example.marketplace.entity.Tag;
import org.example.marketplace.repository.CategoryRepository;
import org.example.marketplace.repository.ProductRepository;
import org.example.marketplace.repository.TagRepository;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;

import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * УРОВЕНЬ 3: ИНТЕГРАЦИОННЫЙ ТЕСТ.
 *
 * Поднимается ВЕСЬ контекст приложения и настоящая PostgreSQL в Docker-контейнере.
 * Ничего не подменяется: реальный контроллер -> реальный сервис -> реальный
 * Hibernate -> реальный SQL -> реальная база. Liquibase накатывает миграции
 * на чистую базу, то есть заодно проверяется, что они вообще работают.
 *
 * Почему Testcontainers, а не H2: H2 — другая СУБД. Она иначе понимает секвенсы,
 * типы и синтаксис, поэтому тест на H2 может пройти там, где прод на Postgres
 * упадёт. Смысл интеграционного теста в том, чтобы окружение совпадало с боевым.
 *
 * Требуется запущенный Docker.
 */
@SpringBootTest(properties = {
        // плейсхолдеры из application.yaml должны чем-то разрешиться;
        // реальные параметры подключения подставит @ServiceConnection
        "DB_URL=unused",
        "DB_USERNAME=unused",
        "DB_PASSWORD=unused",
        // включаем сбор статистики Hibernate — без этого не посчитать запросы
        "spring.jpa.properties.hibernate.generate_statistics=true"
})
@AutoConfigureMockMvc
@Testcontainers
class ProductIntegrationTest {

    /**
     * @ServiceConnection сам пропишет url, username и password от контейнера.
     * Раньше для этого писали @DynamicPropertySource руками.
     * static — чтобы контейнер поднялся один раз на весь класс, а не на каждый тест.
     */
    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired private MockMvc mockMvc;
    @Autowired private ProductRepository productRepository;
    @Autowired private CategoryRepository categoryRepository;
    @Autowired private TagRepository tagRepository;
    @Autowired private EntityManagerFactory entityManagerFactory;

    @BeforeEach
    void setUp() {
        productRepository.deleteAll();
        categoryRepository.deleteAll();
        tagRepository.deleteAll();

        Category category = categoryRepository.save(new Category("Смартфоны", "Мобильные телефоны"));
        List<Tag> tags = tagRepository.saveAll(List.of(new Tag("Новинка"), new Tag("Хит продаж")));

        for (int i = 1; i <= 15; i++) {
            productRepository.save(new Product(
                    "Товар " + i,
                    "Описание " + i,
                    new BigDecimal("1000.00"),
                    i,
                    category,
                    tags
            ));
        }
    }

    @Test
    void getProducts_отдаётЗапрошенныйРазмерСтраницыИОбщееКоличество() throws Exception {
        mockMvc.perform(get("/api/v1/products?page=0&size=10&sort=id,asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(10))
                .andExpect(jsonPath("$.page.totalElements").value(15))
                .andExpect(jsonPath("$.page.totalPages").value(2))
                // категория и теги реально доехали, а не остались пустыми
                .andExpect(jsonPath("$.content[0].category.name").value("Смартфоны"))
                .andExpect(jsonPath("$.content[0].tags.length()").value(2));
    }

    @Test
    void getProducts_несуществующийId_возвращает404() throws Exception {
        mockMvc.perform(get("/api/v1/products/999999"))
                .andExpect(status().isNotFound());
    }

    /**
     * САМЫЙ ЦЕННЫЙ ТЕСТ ИЗ ТРЁХ.
     *
     * Считает, сколько SQL-запросов реально ушло в базу. Ожидаем ровно 3:
     *   1) товары + категория одним join fetch, с LIMIT/OFFSET
     *   2) count для totalElements
     *   3) теги всей страницы одним батчем (default_batch_fetch_size)
     *
     * Важно: число НЕ зависит от размера страницы. Если кто-то уберёт join fetch
     * или batch_fetch_size, вернётся N+1 и тест упадёт — а без него регрессию
     * заметили бы только на проде под нагрузкой.
     *
     * Если тест упадёт — в сообщении будет фактическое число, а сами запросы
     * видно в логе (org.hibernate.SQL: DEBUG).
     */
    @Test
    void getProducts_укладываетсяВТриЗапросаНезависимоОтРазмераСтраницы() throws Exception {
        Statistics stats = entityManagerFactory.unwrap(SessionFactory.class).getStatistics();
        stats.clear();

        mockMvc.perform(get("/api/v1/products?page=0&size=10&sort=id,asc"))
                .andExpect(status().isOk());

        assertThat(stats.getPrepareStatementCount())
                .as("страница должна укладываться в 3 запроса, иначе вернулся N+1")
                .isEqualTo(3);
    }
}
