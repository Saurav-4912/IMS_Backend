package com.mgt.controller;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import com.mgt.jwtServices.JwtService;
import com.mgt.model.User;
import com.mgt.repository.ProductRepo;
import com.mgt.repository.UserRepo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.mgt.model.Product;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:4200")
public class ProductController {

	// ./mvnw spring-boot:run

	@Autowired
	private UserRepo userRepo;

	@Autowired
	private ProductRepo productRepo;

	@Autowired
	private JwtService jwtService;

	private final String uploadDir = "uploads/products/";

	@PostMapping(value = "/addProduct", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	@PreAuthorize("hasRole('USER')")
	public ResponseEntity<?> createProductWithImage(
			@RequestHeader(value = "Authorization", required = false) String authorizationHeader,
			@RequestParam("name") String name,
			@RequestParam(value = "price", required = false) Float price,
			@RequestParam(value = "category", required = false) String category,
			@RequestParam(value = "quantity", required = false) Integer quantity,
			@RequestParam(value = "description", required = false) String description,
			@RequestParam(value = "gstType", required = false) String gstType,
			@RequestParam(value = "gstRate", required = false) Float gstRate,
			@RequestParam("image") MultipartFile imageFile) {

		try {
			// Validate Authorization Header
			if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
						.body("Missing or invalid Authorization header");
			}

			// Extract User ID from JWT Token
			String token = authorizationHeader.substring(7);
			Long userId = jwtService.extractUserId(token); // Assumes your jwtService has this method

			if (userId == null) {
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid JWT token");
			}

			// Fetch Authenticated User
			User user = userRepo.findById(userId)
					.orElseThrow(() -> new RuntimeException("User not found"));

			// Validate Image
			if (imageFile == null || imageFile.isEmpty()) {
				return ResponseEntity.badRequest().body("Image file is required");
			}

			// Save Image File
			Files.createDirectories(Paths.get(uploadDir)); // Make sure uploadDir is defined in your class
			String filename = UUID.randomUUID().toString() + "_" + imageFile.getOriginalFilename();
			Path filepath = Paths.get(uploadDir, filename);
			Files.copy(imageFile.getInputStream(), filepath, StandardCopyOption.REPLACE_EXISTING);

			Product product = new Product();
			product.setProductName(name);
			product.setProduct_price(price);
			product.setProduct_category(category);
			product.setProduct_available_stock_quantity(quantity);
			product.setProduct_description(description);
			product.setGst_type(gstType);
			product.setGst_rate(gstRate);
			product.setProduct_image(filepath.toString());
			product.setUser(user); // associate user

			Product savedProduct = productRepo.save(product);

			return ResponseEntity.ok(savedProduct);

		} catch (Exception e) {
			e.printStackTrace(); // Replace with a logger in production
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body("Error while creating product: " + e.getMessage());
		}
	}

	@GetMapping("/showProduct")
	@PreAuthorize("hasRole('USER')")
	public ResponseEntity<?> getProductsByUser(
			@RequestHeader(value = "Authorization", required = false) String authorizationHeader) {
		try {
			// Validate Authorization Header
			if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
						.body("Missing or invalid Authorization header");
			}

			String token = authorizationHeader.substring(7);
			Long userId = jwtService.extractUserId(token);

			if (userId == null) {
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid JWT token");
			}

			List<Product> products = productRepo.findByUserId(userId);

			return ResponseEntity.ok(products);

		} catch (Exception e) {
			e.printStackTrace();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body("Error fetching products for user: " + e.getMessage());
		}

	}

	@GetMapping("/getImage/{productId}")
	public ResponseEntity<?> getProductImage(@PathVariable int productId) {
		try {

			Product product = productRepo.findById(productId)
					.orElseThrow(() -> new RuntimeException("Product not found"));

			String imagePath = product.getProduct_image();
			Path imageFilePath = Paths.get(imagePath);

			if (!Files.exists(imageFilePath)) {
				return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Image not found");
			}

			byte[] imageBytes = Files.readAllBytes(imageFilePath);

			String contentType = Files.probeContentType(imageFilePath);

			return ResponseEntity.ok()
					.contentType(MediaType.parseMediaType(contentType))
					.body(imageBytes);

		} catch (Exception e) {
			e.printStackTrace();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body("Error fetching image: " + e.getMessage());
		}
	}

	@PutMapping(value = "/updateProduct/{productId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	@PreAuthorize("hasRole('USER')")
	public ResponseEntity<?> updateProduct(
			@RequestHeader(value = "Authorization", required = false) String authorizationHeader,
			@PathVariable("productId") int productId,
			@RequestParam("name") String name,
			@RequestParam(value = "price", required = false) Float price,
			@RequestParam(value = "category", required = false) String category,
			@RequestParam(value = "quantity", required = false) Integer quantity,
			@RequestParam(value = "description", required = false) String description,
			@RequestParam(value = "gstType", required = false) String gstType,
			@RequestParam(value = "gstRate", required = false) Float gstRate,
			@RequestParam(value = "image", required = false) MultipartFile imageFile) {

		try {
			if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
						.body("Missing or invalid Authorization header");
			}

			String token = authorizationHeader.substring(7);
			Long userId = jwtService.extractUserId(token);

			if (userId == null) {
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid JWT token");
			}

			Product product = productRepo.findById(productId)
					.orElseThrow(() -> new RuntimeException("Product not found"));

			// Only allow update if product belongs to the user
			if (!product.getUser().getId().equals(userId)) {
				return ResponseEntity.status(HttpStatus.FORBIDDEN)
						.body("You do not have permission to update this product");
			}

			product.setProductName(name);
			product.setProduct_price(price);
			product.setProduct_category(category);
			product.setProduct_available_stock_quantity(quantity);
			product.setProduct_description(description);
			product.setGst_type(gstType);
			product.setGst_rate(gstRate);

			if (imageFile != null && !imageFile.isEmpty()) {
				Files.createDirectories(Paths.get(uploadDir));
				String filename = UUID.randomUUID().toString() + "_" + imageFile.getOriginalFilename();
				Path filepath = Paths.get(uploadDir, filename);
				Files.copy(imageFile.getInputStream(), filepath, StandardCopyOption.REPLACE_EXISTING);
				product.setProduct_image(filepath.toString());
			}

			productRepo.save(product);

			return ResponseEntity.ok(Map.of("status", "error", "message", "Product updated sucssesfully"));

		} catch (Exception e) {
			e.printStackTrace();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body("Error updating product: " + e.getMessage());
		}
	}

	@DeleteMapping("/deleteProduct/{productId}")
	@PreAuthorize("hasRole('USER')")
	public ResponseEntity<?> deleteProduct(
			@RequestHeader(value = "Authorization", required = false) String authorizationHeader,
			@PathVariable("productId") int productId) {
		try {
			// Validate Authorization Header
			if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
						.body(Map.of("status", "error", "message", "Missing or invalid Authorization header"));
			}

			// Extract User ID
			String token = authorizationHeader.substring(7);
			Long userId = jwtService.extractUserId(token);

			if (userId == null) {
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
						.body(Map.of("status", "error", "message", "Invalid JWT token"));
			}

			// Find Product
			Product product = productRepo.findById(productId)
					.orElseThrow(() -> new RuntimeException("Product not found"));

			// Delete Image from File System
			String imagePath = product.getProduct_image();
			if (imagePath != null) {
				Path imageFilePath = Paths.get(imagePath);
				Files.deleteIfExists(imageFilePath);
			}

			// Delete Product
			productRepo.delete(product);

			return ResponseEntity.ok(Map.of(
					"status", "success",
					"message", "Product deleted successfully"));

		} catch (Exception e) {
			e.printStackTrace();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(Map.of("status", "error", "message", "Error deleting product: " + e.getMessage()));
		}
	}

	// Testing Method
	@GetMapping("/by-name/{name}")
	public Optional<Product> getProductByName(@PathVariable("name") String name) {
		return productRepo.findByProductName(name);
	}

}
