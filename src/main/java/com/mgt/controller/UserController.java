package com.mgt.controller;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
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
public ResponseEntity<?> authenticateAndGetToken(@RequestBody AuthRequest authRequest) {
    try {
        // Authenticate user
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(authRequest.getUsername(), authRequest.getPassword()));

        // Find user by email
        Optional<User> optionalUser = userRepo.findByEmail(authRequest.getUsername());

        if (optionalUser.isPresent()) {
                String token = jwtService.generateToken(authRequest.getUsername());
                return ResponseEntity.ok(token);
          
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }

    } catch (AuthenticationException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Authentication failed: " + ex.getMessage());
    }
}

 @GetMapping("/getUserById")
    public ResponseEntity<?> getUserById(@RequestHeader("Authorization") String authorizationHeader) {
        try {
            // Step 1: Validate JWT token presence
            if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Collections.singletonMap("error", "Missing or invalid Authorization header"));
            }

            // Step 2: Extract token and userId
            String token = authorizationHeader.substring(7);
            Long userId = jwtService.extractUserId(token);  // You should implement this method
            if (userId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Collections.singletonMap("error", "Invalid JWT token"));
            }

            // Step 3: Retrieve user from DB
            Optional<User> optionalUser = userRepo.findById(userId);
            if (optionalUser.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Collections.singletonMap("error", "User not found"));
            }

            User user = optionalUser.get();

            // Step 4: Prepare response with role
            Map<String, Object> response = new HashMap<>();
            response.put("id", user.getId());
            response.put("FullName", user.getFull_name());
            response.put("storeType", user.getStore_type());
            response.put("role", user.getRole());
            response.put("status", user.getStatus());
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.singletonMap("error", "An error occurred: " + e.getMessage()));
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