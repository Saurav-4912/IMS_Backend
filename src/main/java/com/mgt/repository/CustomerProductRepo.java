package com.mgt.repository;

import com.mgt.model.CustomerProduct;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CustomerProductRepo extends JpaRepository<CustomerProduct , Integer> {
}
