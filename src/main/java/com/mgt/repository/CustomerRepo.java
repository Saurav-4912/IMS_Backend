package com.mgt.repository;

import com.mgt.model.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;


@Repository
public interface CustomerRepo extends JpaRepository<Customer, Integer> {

    List<Customer> findByUserId(Long userId);

}
