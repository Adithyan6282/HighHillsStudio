package com.example.highhillsstudio.HighHillsStudio.security;

import com.example.highhillsstudio.HighHillsStudio.repository.AdminRepository;
import com.example.highhillsstudio.HighHillsStudio.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;
    private final AdminRepository adminRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        // Check Admin first
        return adminRepository.findByUsername(username)
                .map(CustomUserDetails::new)
                .orElseGet(() -> userRepository.findByEmail(username)
                        .map(CustomUserDetails::new)
                        .orElseThrow(() -> new UsernameNotFoundException("User/Admin not found")));
    }


}
