package com.mgt.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.mgt.model.Product;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepo extends JpaRepository<Product, Integer> {

    List<Product> findByUserId(Long userId);
    Optional<Product> findByProductName(String productName);

}
