package org.example.marketplace.repository;

import org.example.marketplace.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Здесь две стратегии загрузки связей, и выбор между ними зависит от одного:
 * есть пагинация или нет.
 *
 * Ключевое правило (главный вывод Фазы 0):
 *   - @ManyToOne / @OneToOne  — джойн НЕ размножает строки, LIMIT работает → фетчим join fetch.
 *   - @OneToMany / @ManyToMany — джойн размножает строки, LIMIT ломается   → джойном НЕ фетчим,
 *     догружаем батчами (hibernate.default_batch_fetch_size в application.yaml).
 */
@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    /**
     * Отдаёт ВСЕ товары сразу, без пагинации. Только для отладки — на реальном
     * каталоге такая ручка кладёт и приложение, и фронт.
     *
     * Пагинации нет, значит ломать LIMIT нечем — можно фетчить обе связи одним
     * запросом. Итог: 1 запрос вместо 151 (1 на товары + 150 на категории).
     *
     * Почему обе связи разрешено джойнить вместе: коллекция здесь одна (tags),
     * category — это @ManyToOne. Если бы коллекций было две и обе объявлены как
     * List, Hibernate упал бы с MultipleBagFetchException: 10 отзывов x 3 тега
     * дают 30 строк на товар, и разложить их обратно в две независимые коллекции
     * невозможно. Лечится заменой List на Set, либо батчами.
     *
     * distinct — из-за коллекции: товар с 3 тегами приезжает 3 строками.
     * (В Hibernate 6 дубли корневых сущностей отсеиваются и без него, но в SQL
     * он всё равно уходит и заставляет базу сортировать результат.)
     */
    @Query("select distinct p from Product p left join fetch p.category left join fetch p.tags")
    List<Product> findAllWithCategoryAndTags();

    /**
     * Постраничная выдача — то, что реально должно использоваться в каталоге.
     *
     * Джойном фетчим ТОЛЬКО category (@ManyToOne): один товар остаётся одной
     * строкой, поэтому LIMIT отрезает ровно то количество товаров, что просили.
     *
     * Теги здесь джойном НЕ фетчим. Товар с 3 тегами превратился бы в 3 строки,
     * и LIMIT 10 отрезал бы 10 строк, то есть 3-4 товара, причём последнему
     * достались бы не все теги. Hibernate это понимает и поступает единственно
     * возможным способом: выбрасывает LIMIT из SQL, тянет ВСЮ таблицу в память
     * и режет страницу там (предупреждение HHH90003004). На 150 записях незаметно,
     * на миллионе — OutOfMemory.
     *
     * Вместо этого теги догружаются отдельным запросом, но пачкой на всю страницу:
     * default_batch_fetch_size: 50 в application.yaml превращает 10 запросов
     * (по одному на товар) в один — "where product_id = any (?)".
     *
     * Итого на вызов ровно 3 запроса, и это число НЕ растёт с размером страницы:
     *   1) товары + категории с LIMIT/OFFSET
     *   2) count для totalElements
     *   3) теги всей страницы одним батчем
     *
     * distinct не нужен: джойн с @ManyToOne дублей не создаёт.
     *
     * countQuery прописан явно. Без него Spring Data сочиняет count сам, переписывая
     * JPQL текстом — это работает на простых запросах, но с fetch join коллекции даёт
     * count по строкам джойна (300 вместо 150): фронт нарисует вдвое больше страниц,
     * и последние окажутся пустыми. Плюс явный count не тащит ненужный джойн с category.
     */
    @Query(value = "select p from Product p left join fetch p.category",
           countQuery = "select count(p) from Product p")
    Page<Product> findPageWithCategory(Pageable pageable);

}
