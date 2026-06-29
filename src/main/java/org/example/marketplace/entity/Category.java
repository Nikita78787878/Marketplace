package org.example.marketplace.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import java.util.List;

@Entity
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // типы справшивают вроде интересно какие надо
    private Long id;

    private String name;
    private String description;

    @OneToMany(mappedBy = "category", fetch = FetchType.LAZY) // хотя по дефолту лейзи, но пишут всегда бест практик
    private List<Product> products;

    public Category() {
    }

    public Category(String name, String description) {
        this.name = name;
        this.description = description;

    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public List<Product> getProducts() {
        return products;
    }
}
