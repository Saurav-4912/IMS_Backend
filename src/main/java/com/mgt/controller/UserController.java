package com.mgt.controller;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.*;

import com.mgt.jwtServices.JwtService;
import com.mgt.model.AuthRequest;
import com.mgt.model.Status;
import com.mgt.model.User;
import com.mgt.model.UserInfoService;
import com.mgt.repository.UserRepo;


@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:4200")
public class UserController {

    @Autowired
    private UserInfoService service;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserRepo userRepo;

    
   

    @GetMapping("/welcome")
    public String welcome() {
        return "Welcome this endpoint is not secure";
    }

    @GetMapping("/testUser")
    @PreAuthorize("hasRole('USER')")
    public String getUserController() {
        return "I am user controller";
    }

    @GetMapping("/testAdmin")
    @PreAuthorize("hasRole('ADMIN')")
    public String getAdminController() {
        return "I am admin controller";
    }
    

    @PostMapping("/register")
    public ResponseEntity<?> addNewUser(@RequestBody User userInfo) {
        String result = service.addUser(userInfo);

        if (result.equals("Error: Username already exists!")) {

            return ResponseEntity.ok(Collections.singletonMap("message", "Duplicate entory"));
        }

       

        return ResponseEntity.ok(Collections.singletonMap("message", "User created successfully"));

    }

    // Removed the role checks here as they are already managed in SecurityConfig

    @PostMapping("/login")
    public ResponseEntity<String> authenticateAndGetToken(@RequestBody AuthRequest authRequest) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(authRequest.getUsername(), authRequest.getPassword()));

            if (authentication.isAuthenticated()) {
                Optional<User> optionalUser = userRepo.findByEmail(authRequest.getUsername());

                if (optionalUser.isPresent()) {
                    User user = optionalUser.get();

                    if (user.getStatus() == Status.ACTIVE) {
                        String token = jwtService.generateToken(authRequest.getUsername());
                        return ResponseEntity.ok(token);
                    } else {
                        return ResponseEntity.status(HttpStatus.FORBIDDEN).body("User is not active");
                    }
                } else {
                    return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
                }
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials");
            }
        } catch (AuthenticationException ex) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Authentication failed: " + ex.getMessage());
        }
    }

    // Endpoint accessible to users with ROLE_USER
    @GetMapping("/user/userProfile")
    public String userProfile() {
        return "Welcome to the USER profile!";
    }

    // Endpoint accessible to users with ROLE_ADMIN
    @GetMapping("/admin/adminProfile")
    public String adminProfile() {
        return "Welcome to the ADMIN profile!";
    }

    @GetMapping("/testLanding")
    public String afterLogin(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.substring(7); // Remove "Bearer " prefix
        Long userId = jwtService.extractUserId(token);
        return "Welcome to login user " + userId;
    }

    @GetMapping("/registeredUser")
    public List<User> getMethodName() {

        List<User> user = userRepo.findAll();
        return (List<User>) user;
    }

    @PutMapping("/approve/{id}")
    public ResponseEntity<String> approveUser(@PathVariable long id) {
        Optional<User> optionalUser = userRepo.findById(id);

        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            user.setStatus(Status.ACTIVE);
            userRepo.save(user);
            return ResponseEntity.ok("User approved successfully.");
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found.");
        }
    }

}