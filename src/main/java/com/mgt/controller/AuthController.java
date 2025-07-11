package com.mgt.controller;

import java.util.Collections;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.mgt.serviceimpl.EmailService;
import com.mgt.serviceimpl.OtpService;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:4200")
public class AuthController {

    @Autowired
    private EmailService emailService;

    @Autowired
    private OtpService otpService;

 

    @PostMapping("/sendOtp")
    public ResponseEntity<?> sendOtp(@RequestParam String email) {

        System.out.println("send otp function is called  "+email);

       String otp = otpService.generatedOtp(email);

        emailService.sendAuthEmail(email, "Your Login OTP ", "Use this OTP to log in your email "+otp);
        return  ResponseEntity.ok(Collections.singletonMap("message","OTP sent to email"));

    }

    @PostMapping("/verifyOtp")
    public ResponseEntity<?> verifyOtp(@RequestParam String email, @RequestParam String otp) {

        boolean isValid = otpService.verifyOtp(email, otp);

        if (isValid) {
            return ResponseEntity.ok(Collections.singletonMap("message","Login scuccesful"));
        } else {
            return ResponseEntity.badRequest().body("Invalid or expired OTP");
        }
    }

}
