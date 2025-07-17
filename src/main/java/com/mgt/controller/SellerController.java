package com.mgt.controller;

import com.mgt.jwtServices.JwtService;
import com.mgt.model.Seller;
import com.mgt.model.User;
import com.mgt.repository.SellerRepo;
import com.mgt.repository.UserRepo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
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

    private final String uploadDir = "uploads/seller/";

    @PostMapping(value = "/addSellerImg", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> createProductWithImage(
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader,
            @RequestParam("name") String name,
            @RequestParam(value = "email", required = false) String email,
            @RequestParam(value = "grossSale", required = false) Float grossSale,
            @RequestParam(value = "earning", required = false) Float earning,
            @RequestParam("image") MultipartFile imageFile) {

        try {
            if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Missing or invalid Authorization header");
            }

            String token = authorizationHeader.substring(7);
            Long userId = jwtService.extractUserId(token);
            if (userId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid JWT token");
            }

            User user = userRepo.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));

            if (imageFile == null || imageFile.isEmpty()) {
                return ResponseEntity.badRequest().body("Image file is required");
            }

            // Create directory if not exists
            Files.createDirectories(Paths.get(uploadDir));

            // Unique filename
            String filename = imageFile.getOriginalFilename();
            Path filepath = Paths.get(uploadDir, filename);
            Files.copy(imageFile.getInputStream(), filepath, StandardCopyOption.REPLACE_EXISTING);

            Seller seller = new Seller();
            seller.setName(name);
            seller.setEmail(email);
            seller.setGrossSale(grossSale);
            seller.setEarning(earning);
            seller.setImagePath(imageFile.getOriginalFilename()); // store only filename, not full path
            seller.setUser(user);

            Seller savedSeller = sellerRepo.save(seller);
            return ResponseEntity.ok(savedSeller);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error while creating seller: " + e.getMessage());
        }
    }

    // Serve images
    @GetMapping("/seller/serveImage/{filename}")
    public ResponseEntity<Resource> getImage(@PathVariable String filename) {

        try {
            Path imagePath = Paths.get(uploadDir).resolve(filename).normalize();
            Resource resource = new UrlResource(imagePath.toUri());

            if (!resource.exists()) {
                return ResponseEntity.notFound().build();
            }

            // Auto detect file type
            String contentType = Files.probeContentType(imagePath);
            if (contentType == null) {
                contentType = "application/octet-stream";
            }

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .body(resource);

        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(null);
        }
    }

    @PostMapping("/addSeller")
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

            User user = userRepo.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));

            Seller seller = new Seller();
            seller.setName(sellerRequest.getName());
            seller.setEmail(sellerRequest.getEmail());
            seller.setGrossSale(sellerRequest.getGrossSale());
            seller.setEarning(sellerRequest.getEarning());
            seller.setUser(user);

            sellerRepo.save(seller);
            return ResponseEntity.ok(Map.of("message", "New seller added"));

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
