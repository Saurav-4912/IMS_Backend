package com.mgt.controller;

import com.mgt.jwtServices.JwtService;
import com.mgt.model.Review;
import com.mgt.model.User;
import com.mgt.repository.ReviewRepo;
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
public class ReviewController {

    @Autowired
    private JwtService jwtService;

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private ReviewRepo reviewRepo;

    private final String uploadDir = "uploads/review/";

    @PostMapping(value = "/addReviewImg", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> createProductWithImage(
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader,
            @RequestParam(value="reviewerName") String reviewerName,
            @RequestParam(value = "comment", required = false) String comment,
            @RequestParam(value = "rating", required = false) int rating,
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

            Review review = new Review();
            review.setReviewerName(reviewerName);
            review.setComment(comment);
            review.setRating(rating);
            review.setImagePath(imageFile.getOriginalFilename()); // store only filename, not full path
            review.setUser(user);

            Review savedReview = reviewRepo.save(review);
            return ResponseEntity.ok(savedReview);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error while creating seller: " + e.getMessage());
        }
    }

     // Serve images
    @GetMapping("/review/serveImage/{filename}")
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

    // 1. Add Review
    @PostMapping("/addReview")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> addReview(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody Review reviewRequest) {

        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Missing or invalid token");
            }

            String token = authHeader.substring(7);
            Long userId = jwtService.extractUserId(token);

            if (userId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid token");
            }

            User user = userRepo.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            Review review = new Review();
            review.setReviewerName(reviewRequest.getReviewerName());
            review.setComment(reviewRequest.getComment());
            review.setRating(reviewRequest.getRating());
            review.setUser(user);

            reviewRepo.save(review);
            return ResponseEntity.ok(Map.of("message", "Review added successfully"));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error adding review: " + e.getMessage());
        }
    }

    // 2. Get My All Reviews
    @GetMapping("/my-reviews")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> getMyReviews(@RequestHeader("Authorization") String authHeader) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Missing or invalid token");
            }

            String token = authHeader.substring(7);
            Long userId = jwtService.extractUserId(token);

            if (userId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid token");
            }

            List<Review> reviews = reviewRepo.findByUserId(userId);
            return ResponseEntity.ok(reviews);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error fetching reviews: " + e.getMessage());
        }
    }

    //  3. Delete Review
    @DeleteMapping("/deleteReview/{id}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> deleteReview(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable("id") Long reviewId) {

        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Missing or invalid token");
            }

            String token = authHeader.substring(7);
            Long userId = jwtService.extractUserId(token);

            if (userId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid token");
            }

            Review review = reviewRepo.findById(reviewId)
                    .orElseThrow(() -> new RuntimeException("Review not found"));

            // Check ownership
            if (!review.getUser().getId().equals(userId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body("You are not authorized to delete this review");
            }

            reviewRepo.deleteById(reviewId);
            return ResponseEntity.ok(Map.of("message", "Review deleted successfully"));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error deleting review: " + e.getMessage());
        }
    }
}