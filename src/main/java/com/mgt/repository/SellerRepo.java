package com.mgt.repository;


import com.mgt.model.Seller;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SellerRepo extends JpaRepository<Seller, Long> {
    List<Seller> findByUserId(Long userId);
}
