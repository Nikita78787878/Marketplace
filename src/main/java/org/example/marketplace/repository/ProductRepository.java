package org.example.marketplace.repository;

import org.example.marketplace.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

// TODO (следующая сессия): добавить findAll с JOIN FETCH category + tags, чтобы убить N+1 в getAllProduct
@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {




}
