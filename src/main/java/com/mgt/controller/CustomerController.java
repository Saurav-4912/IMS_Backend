package com.mgt.controller;

import com.mgt.jwtServices.JwtService;
import com.mgt.model.Customer;
import com.mgt.model.CustomerProduct;
import com.mgt.model.Product;
import com.mgt.model.User;
import com.mgt.repository.CustomerRepo;
import com.mgt.repository.ProductRepo;
import com.mgt.repository.UserRepo;
import com.mgt.serviceimpl.CustomerServiceImpl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:4200") // allow Angular frontend
public class CustomerController {

    @Autowired
    private CustomerServiceImpl customerService;

    @Autowired
    private CustomerRepo customerRepo;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private ProductRepo productRepo;

    @PostMapping("/addBill")
    public ResponseEntity<?> saveCustomer(
            @RequestBody Customer customer,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        System.out.println("Received Authorization Header: " + authHeader);

        try {
            // Validate Authorization Header
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Collections.singletonMap("message", "Missing or invalid Authorization header"));
            }

            // Extract User ID from JWT Token
            String token = authHeader.substring(7);
            Long userId = jwtService.extractUserId(token); // Ensure this method is correct!

            if (userId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Collections.singletonMap("message", "Invalid JWT token"));
            }

            // Fetch Authenticated User
            User user = userRepo.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // Associate user with customer
            customer.setUser(user);

            // Save customer and customer products
            customerService.saveCustomer(customer);

            // Handle subarray: Update stock quantities
            if (customer.getCustomerProductList() != null) {
                for (CustomerProduct customerProduct : customer.getCustomerProductList()) {
                    System.out.println(
                            "Product ID: " + customerProduct.getProductId() +
                            ", Name: " + customerProduct.getName() +
                            ", Quantity: " + customerProduct.getQuantity());
                    // Find matching product in user's products
                    Product matchingProduct = user.getProducts().stream()
                            .filter(p -> p.getProductName().equals(customerProduct.getName()))
                            .findFirst()
                            .orElse(null);

                    if (matchingProduct != null) {
                        int newStock = matchingProduct.getProduct_available_stock_quantity() - customerProduct.getQuantity();
                        if (newStock < 0) {
                            throw new RuntimeException("Insufficient stock for product: " + matchingProduct.getProductName());
                        }
                        matchingProduct.setProduct_available_stock_quantity(newStock);
                        productRepo.save(matchingProduct);
                        System.out.println("Updated stock for " + matchingProduct.getProductName() + ": " + newStock);
                    } else {
                        System.out.println("Product not found in user's products: " + customerProduct.getName());
                        // Optionally throw an error here
                    }
                }
            }

            return ResponseEntity.ok(Collections.singletonMap("message", "Invoice created successfully"));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.singletonMap("message", "Error saving invoice: " + e.getMessage()));
        }
    }

    @GetMapping("/fetchAllBillsByUser")
    public ResponseEntity<?> getBillsByUserId(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Collections.singletonMap("message", "Missing or invalid Authorization header"));
            }

            String token = authHeader.substring(7);
            Long userId = jwtService.extractUserId(token);

            if (userId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Collections.singletonMap("message", "Invalid JWT token"));
            }

            List<Customer> bills = customerRepo.findByUserId(userId);
            return ResponseEntity.ok(bills);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.singletonMap("message", "Error fetching bills: " + e.getMessage()));
        }
    }

    // ⚠ Fix: add {id} in path to match @PathVariable
    @GetMapping("/customers/{id}")
    public Customer getCustomerById(@PathVariable int id) {
        return customerService.getCustomerById(id);
    }

    @GetMapping("/count")
    public Long countInvoice() {
        return customerRepo.count();
    }
}
