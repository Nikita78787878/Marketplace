package org.example.marketplace.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.util.List;

@Entity
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String description;
    private BigDecimal price;
    private Integer stockQuantity ;

    @ManyToOne(fetch = FetchType.LAZY) // по умолчанию жадный
    @JoinColumn(name = "category_id")
    private Category category;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "product_tag",
        joinColumns = @JoinColumn(name = "product_id"),
        inverseJoinColumns = @JoinColumn(name = "tag_id"))
    private List<Tag> tags;

    public Product() {
    }

    public Product( String name, String description, BigDecimal price, Integer stockQuantity, Category category) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.stockQuantity = stockQuantity;
        this.category = category;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public Integer getStockQuantity() {
        return stockQuantity;
    }

    public Category getCategory() {
        return category;
    }

    public List<Tag> getTags() {
        return tags;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Product product)) return false;
        return id != null && id.equals(product.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode(); // Константа
    }
    /**
     * хитрость
     * равильно понял: константный hashCode → все объекты в одной корзине HashSet → поиск внутри неё O(n), а не O(1).
     *
     * Почему миримся: корректность важнее скорости. Альтернатива (hashCode по изменяемым полям) не «медленнее» — она ломает HashSet вообще: объект меняет цену → меняет бакет → теряется навсегда. А entity редко держат пачками в HashSet, так что потеря O(1) на практике незаметна. Выбираем «медленнее, но правильно».
     *
     * 4 Разбор паттерна построчно (раз подзакопался)
     * public boolean equals(Object o) {
     *     if (this == o) return true;                          // (1)
     *     if (!(o instanceof Product product)) return false;   // (2)
     *     return id != null && id.equals(product.id);          // (3)
     * }
     * public int hashCode() {
     *     return getClass().hashCode();                        // (4)
     * }
     * (1) Та же ссылка → точно равны. Заодно: два несохранённых объекта равны, только если это буквально один и тот же объект.
     * (2) Проверка на null и тип. instanceof, а не getClass() — потому что Hibernate подсовывает прокси-наследника,
     * и getClass() вернул бы Product$HibernateProxy, сравнение бы падало. instanceof это переживает.
     * (3) Равны, только если у этого объекта есть реальный id (!= null, т.е. уже сохранён) И id совпадают. Если id == null (не сохранён) → false.
     * Значит несохранённый объект равен только сам себе (через пункт 1). Два разных новых объекта не схлопнутся.
     * (4) Константа. Главное: при переходе id: null → 5 хеш не меняется, объект остаётся в том же бакете → HashSet его не теряет.
     * Связка, которая всё объясняет: equals меняет поведение после сохранения (был null → стал id), а вот hashCode остаётся неизменным.
     * Если бы hashCode зависел от id, объект после сохранения «переехал» бы в другой бакет и пропал из сета.
     * Константный hashCode — именно тот трюк, что решает твой изначальный страх «потеряем объект в мапе».
     */

}
