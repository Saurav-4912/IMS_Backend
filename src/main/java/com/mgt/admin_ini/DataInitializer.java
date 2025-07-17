package com.mgt.admin_ini;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.mgt.model.Role;
import com.mgt.model.Status;
import com.mgt.model.User;
import com.mgt.repository.UserRepo;

@Component
public class DataInitializer implements CommandLineRunner{

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {

        System.out.println("Admin creadintial is username : admin and password: admin@123");
       
      if(userRepo.count() == 0) {

        String encodedPassword = passwordEncoder.encode("admin@123");
            // Initialize the database with default users
            userRepo.save(new User("Admin","admin",encodedPassword,Role.ADMIN,Status.ACTIVE));
            System.out.println("Admin credintial is username : admin and password: admin@123");
      }
    }
}
