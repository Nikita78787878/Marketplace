package org.example.marketplace.entity;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "category_seq")
    @SequenceGenerator(name = "category_seq", sequenceName = "category_seq", allocationSize = 50)
    private Long id;

    private String name;
    private String description;

    @OneToMany(mappedBy = "category", fetch = FetchType.LAZY) // хотя по дефолту лейзи, но пишут всегда бест практик
    private List<Product> products = new ArrayList<>();

    public Category() {
    }

    public Category(String name, String description) {
        this.name = name;
        this.description = description;

    }

    public Long getId() {
        return id;
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

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public boolean equals(Object o){
        if(o == this) return true;
        if(!(o instanceof Category)) return false;
        Category category = (Category) o;
        return id != null && id.equals(category.id);
    }

    @Override
    public int hashCode(){
        return getClass().hashCode();
    }
}
