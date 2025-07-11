package com.mgt.controller;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mgt.model.Status;
import com.mgt.model.User;
import com.mgt.serviceimpl.UserServiceImpl;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "http://localhost:4200")
public class AdminController {

	@Autowired
	private UserServiceImpl userServiceImpl;

	// View All Users
	@GetMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
	public List<User> getAllUser() {

        return userServiceImpl.getAllUser();
	}

    //Active user

    @PutMapping("/approve/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> approveUser(@PathVariable("id") Long id) {
        User user = userServiceImpl.findById(id);
        
        if (user == null) {
            return ResponseEntity.status(404).body(Collections.singletonMap("message", "User not found"));
        }

        user.setStatus(Status.ACTIVE); // Update user status to ACTIVE
        userServiceImpl.addUser(user); // Save updated user

        return ResponseEntity.ok(Collections.singletonMap("message", "User can login"));
    }

    // Admin reject endpoint
    @PutMapping("/reject/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> rejectUser(@PathVariable("id") Long id) {
        User user = userServiceImpl.findById(id);
        if (user == null) {
            Map<String, Object> response = new HashMap<>();
            response.put("message", "User not found");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }

        user.setStatus(Status.REJECTED); // Change status to REJECTED
        userServiceImpl.addUser(user);

        Map<String, Object> response = new HashMap<>();
        response.put("message", "User rejected.");
        return ResponseEntity.ok(response);
    }
}