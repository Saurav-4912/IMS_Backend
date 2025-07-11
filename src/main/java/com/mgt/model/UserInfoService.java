package com.mgt.model;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.mgt.repository.UserRepo;
import java.util.Collections;

@Service
public class UserInfoService implements UserDetailsService {

    @Autowired
    private UserRepo repository;

    @Lazy
    @Autowired
    private PasswordEncoder encoder;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = repository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
        
        return new org.springframework.security.core.userdetails.User(
                user.getEmail(), // assuming email is used as username
                user.getPassword(),
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()))
        );
    }

    public String addUser(User userInfo) {
        if (repository.existsByEmail(userInfo.getEmail())) {
            return "Error: Email already exists!";
        }

        userInfo.setPassword(encoder.encode(userInfo.getPassword()));
    
        repository.save(userInfo);
        return "User Added Successfully";
    }
}
