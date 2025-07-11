package com.mgt.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mgt.model.OtpVerification;

public interface OtpVerificationRepository extends JpaRepository<OtpVerification, Long>{

 Optional<OtpVerification>  findByEmailAndOtp(String email, String otp);

 void deleteByEmail(String email);

}
