package com.mgt.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.mgt.model.Role;
import com.mgt.model.User;



@Repository
public interface UserRepo extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

   
    Optional<User> findById(long userId);

    boolean existsByEmail(String email);


    List<User> findByRole(Role role);


    
}
