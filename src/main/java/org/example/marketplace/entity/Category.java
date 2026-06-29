package org.example.marketplace.entity;

import jakarta.persistence.*;

import java.util.List;
import java.util.Objects;

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

    @Override
    public boolean equals(Object o){
        if(o == this) return true;
        if(!(o instanceof Category)) return false;
        Category category = (Category) o;
        return Objects.equals(name, category.getName());
    }

    @Override
    public int hashCode(){
        return Objects.hash(name);
    }
}
