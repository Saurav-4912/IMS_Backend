package com.mgt.serviceimpl;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mgt.model.OtpVerification;
import com.mgt.repository.OtpVerificationRepository;

@Service
public class OtpService {

    @Autowired
    private OtpVerificationRepository otpRepository;

    @Transactional
    public String generatedOtp(String email) {

        // Generate a 6-digit OTP (from 000000 to 999999)
        String otp = String.format("%06d", new Random().nextInt(1_000_000));

        // Set expiry time (5 minutes from now)
        LocalDateTime expiry = LocalDateTime.now().plusMinutes(5);

        // Remove any existing OTP entries for this email
        otpRepository.deleteByEmail(email);

        // Create and save new OTP entry
        OtpVerification otpVerification = new OtpVerification();
        otpVerification.setEmail(email);
        otpVerification.setOtp(otp);
        otpVerification.setExpirationTime(expiry);

        otpRepository.save(otpVerification);

        System.out.println("OTP is generated: " + otp);
        return otp;
    }

     @Transactional
    public boolean verifyOtp(String email, String otp) {

        Optional<OtpVerification> match = otpRepository.findByEmailAndOtp(email, otp);

        if (match.isPresent() && match.get().getExpirationTime().isAfter(LocalDateTime.now())) {

            otpRepository.deleteByEmail(email);

            return true;

        }
        return false;
    }
}
