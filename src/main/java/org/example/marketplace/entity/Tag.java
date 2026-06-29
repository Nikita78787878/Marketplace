package org.example.marketplace.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import java.util.List;

@Entity
public class Tag {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @ManyToMany(mappedBy = "tags", fetch = FetchType.LAZY)
    private List<Product> products;

    public String getName() {
        return name;
    }

    public List<Product> getProducts() {
        return products;
    }
}
