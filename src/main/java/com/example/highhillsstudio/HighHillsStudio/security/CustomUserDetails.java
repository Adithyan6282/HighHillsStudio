package com.example.highhillsstudio.HighHillsStudio.security;

import com.example.highhillsstudio.HighHillsStudio.entity.Admin;
import com.example.highhillsstudio.HighHillsStudio.entity.User;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;

@Getter
public class CustomUserDetails implements UserDetails {


    private String username;
    private String password;
    private boolean enabled;
    private List<GrantedAuthority> authorities;

    // User constructor
    public CustomUserDetails(User user) {
        this.username = user.getEmail(); // login by email
        this.password = user.getPassword();
        this.enabled = user.isEnabled() && !user.isBlocked();
        // role must be exactly USER
        this.authorities = List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().toUpperCase()));
    }

    // Admin constructor
    public CustomUserDetails(Admin admin) {
        this.username = admin.getUsername();
        this.password = admin.getPassword();
        this.enabled = true;
        this.authorities = List.of(new SimpleGrantedAuthority("ROLE_ADMIN"));
    }

    @Override public boolean isAccountNonExpired() { return true; }
    @Override public boolean isAccountNonLocked() { return enabled; }
    @Override public boolean isCredentialsNonExpired() { return true; }
    @Override public boolean isEnabled() { return enabled; }

}





//    private String username;
//    private String password;
//    private boolean enabled;
//    private List<GrantedAuthority> authorities;
//
//    public CustomUserDetails(User user) {
//        this.username = user.getEmail();
//        this.password = user.getPassword();
//        this.enabled = user.isEnabled() && !user.isBlocked();
//        this.authorities = List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole()));
//    }
//
//    public CustomUserDetails(Admin admin) {
//        this.username = admin.getUsername();
//        this.password = admin.getPassword();
//        this.enabled = true;
//        this.authorities = List.of(new SimpleGrantedAuthority("ROLE_ADMIN"));
//    }
//
//    @Override public boolean isAccountNonExpired() { return true; }
//    @Override public boolean isAccountNonLocked() { return enabled; }
//    @Override public boolean isCredentialsNonExpired() { return true; }
//    @Override public boolean isEnabled() { return enabled; }













