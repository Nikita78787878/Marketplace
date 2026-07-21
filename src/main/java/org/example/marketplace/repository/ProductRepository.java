package org.example.marketplace.repository;

import org.example.marketplace.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

// TODO (следующая сессия): добавить findAll с JOIN FETCH category + tags, чтобы убить N+1 в getAllProduct
@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    @Query("select distinct p from Product p left join fetch p.tags") // решаем проблему N+1 подгружаем сразу все теги
    List<Product> findAllWithTags();




}
