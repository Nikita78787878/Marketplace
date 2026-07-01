package org.example.marketplace.repository;

import org.example.marketplace.entity.Product;
import org.example.marketplace.entity.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TagRepository extends JpaRepository<Tag, Long> {





}
