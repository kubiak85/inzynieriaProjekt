package com.engineering.shop.products;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.data.rest.core.annotation.RestResource;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProductsRepo extends JpaRepository<Product, Integer> {
    Optional<Product> getById(Integer id);

    Iterable<Product> findByName(String name);

    Iterable<Product> findByNameContainingIgnoreCase(String name);

    Iterable<Product> findByMainCategoryId(Integer categoryId);

    Iterable<ProductHeaderProjection> findAllByNameContainingIgnoreCaseAndActiveIsTrue(String name);
}
