package com.example.highhillsstudio.HighHillsStudio.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;


import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @Column(name = "name", nullable = false, length = 50)
    @NotBlank(message = "Name is required")
    @Size(max = 50, message = "Name cannot exceed 50 characters")
    private String fullName;

    @Email(message = "Enter a valid email address")
    @NotBlank(message = "Email is required")
    @Column(unique = true, nullable = false, length = 100)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(length = 15, unique = true)
    private String phone;

    @Column(nullable = false)
    private boolean enabled = false; // false until OTP Verified


    @ManyToOne
    @JoinColumn(name = "user_gender_id")
    private UserGender userGender;



//    @Column(nullable = false)
//    private String role = "USER";
    @Column(nullable = false)
    private String role;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt ;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt ;

    @Column(nullable = false)
    private boolean blocked = false;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Otp> otps = new ArrayList<>();

    @Column(name = "reset_password_token", length = 255, nullable = true)  // test
    private String resetPasswordToken;

    @Column(name = "reset_password_expiry", nullable = true)    // test
    private LocalDateTime resetPasswordExpiry;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UserAddress> addresses = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UserOrder> orders = new ArrayList<>();

    private String profileImage;

    // test
    @Column(name = "email_change_token")
    private String emailChangeToken;

    @Column(name = "email_change_expiry")
    private LocalDateTime emailChangeExpiry;

    @Column(name = "pending_email")
    private String pendingEmail; // store the new email temporarily


    @PrePersist
    public void prePersist() {
        if(role == null) {
            role = "USER";
        }
        if(!enabled) {
            enabled = false;
        }
        if(!blocked) {
            blocked = false;
        }
    }

}

