package com.example.highhillsstudio.HighHillsStudio.security;

import com.example.highhillsstudio.HighHillsStudio.entity.User;
import com.example.highhillsstudio.HighHillsStudio.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);

        String  email = oAuth2User.getAttribute("email");
        String name = oAuth2User.getAttribute("name");

        // check if user exists
        Optional<User> existingUser = userRepository.findByEmail(email);

        if (existingUser.isEmpty()) {
            // New user -> treat as signup
            User newUser = User.builder()
                    .fullName(name)
                    .email(email)
                    .enabled(true) // already verified (Google)
                    .role("USER")
                    .password("") // not required for OAuth
                    .build();
            userRepository.save(newUser);
        }

        // Use DefaultOAuth2User so Spring Security knows about the authorities
        return new DefaultOAuth2User(
                List.of(new SimpleGrantedAuthority("ROLE_USER")),
                oAuth2User.getAttributes(),
                "email" // key to use for Principal name
        );
    }
}
