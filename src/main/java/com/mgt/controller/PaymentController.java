package com.mgt.controller;

import java.util.Collections;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.mgt.jwtServices.JwtService;
import com.mgt.model.Status;
import com.mgt.repository.UserRepo;

@RestController
@RequestMapping("/api/payments")
@CrossOrigin(origins = "http://localhost:4200")
public class PaymentController {

    @Autowired
    private JwtService jwtService;

    @Autowired
    private UserRepo userRepo;

    @PostMapping("/process")
    public ResponseEntity<?> processPayment(@RequestBody PaymentRequest paymentRequest, 
                                            @RequestHeader("Authorization") String authHeader) {
        String paymentToken = paymentRequest.getToken();
        System.out.println("Received Google Pay token: " + paymentToken);

        try {
            // Validate Authorization header
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Collections.singletonMap("message", "Missing or invalid Authorization header"));
            }

            // Extract user ID from JWT
            String token = authHeader.substring(7);
            Long userId = jwtService.extractUserId(token);

            if (userId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Collections.singletonMap("message", "Invalid JWT token"));
            }

            userRepo.findById(userId).ifPresent(user -> {
                System.out.println("User found: " + user.getEmail());
                user.setStatus(Status.ACTIVE);
                userRepo.save(user);
            });

            // Here, you'd forward paymentToken to your payment processor
            return ResponseEntity.ok(Collections.singletonMap("message", "Payment processed successfully"));

        } catch (Exception e) {
            e.printStackTrace(); // useful for debugging
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.singletonMap("message", "Error processing payment: " + e.getMessage()));
        }
    }

    public static class PaymentRequest {
        private String token;

        public String getToken() { return token; }
        public void setToken(String token) { this.token = token; }
    }
}
