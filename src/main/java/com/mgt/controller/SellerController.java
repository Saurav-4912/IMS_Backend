package com.mgt.controller;

import com.mgt.jwtServices.JwtService;
import com.mgt.model.Seller;
import com.mgt.model.User;
import com.mgt.repository.SellerRepo;
import com.mgt.repository.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:4200")
public class SellerController {

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private SellerRepo sellerRepo;

    @PostMapping("/add")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> addSeller(
            @RequestHeader("Authorization") String authorizationHeader,
            @RequestBody Seller sellerRequest) {

        try {
            if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Missing or invalid token");
            }

            String token = authorizationHeader.substring(7);
            Long userId = jwtService.extractUserId(token);

            if (userId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid token");
            }

            // Get authenticated user
            User user = userRepo.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // Link user with seller
            Seller seller = new Seller();
            seller.setName(sellerRequest.getName());
            seller.setEmail(sellerRequest.getEmail());
            seller.setGrossSale(sellerRequest.getGrossSale());
            seller.setEarning(sellerRequest.getEarning());
            seller.setUser(user);

            Seller saved = sellerRepo.save(seller);
            return ResponseEntity.ok(saved);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error adding seller: " + e.getMessage());
        }
    }


    @GetMapping("/my-sellers")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> getMySellers(@RequestHeader("Authorization") String authorizationHeader) {
        try {
            if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Missing or invalid token");
            }

            String token = authorizationHeader.substring(7);
            Long userId = jwtService.extractUserId(token);

            if (userId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid token");
            }

            List<Seller> sellers = sellerRepo.findByUserId(userId);
            return ResponseEntity.ok(sellers);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error fetching sellers: " + e.getMessage());
        }
    }

    @DeleteMapping("/delete/{id}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> deleteSeller(
            @RequestHeader("Authorization") String authorizationHeader,
            @PathVariable("id") Long sellerId) {

        try {
            if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Missing or invalid token");
            }

            String token = authorizationHeader.substring(7);
            Long userId = jwtService.extractUserId(token);

            if (userId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid token");
            }

            Seller seller = sellerRepo.findById(sellerId)
                    .orElseThrow(() -> new RuntimeException("Seller not found"));

            // Check ownership
            if (!seller.getUser().getId().equals(userId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body("You are not authorized to delete this seller");
            }

            sellerRepo.deleteById(sellerId);
            return ResponseEntity.ok(Map.of("message", "Seller deleted successfully"));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error deleting seller: " + e.getMessage());
        }
    }

}
