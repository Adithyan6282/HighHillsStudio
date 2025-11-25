package com.example.highhillsstudio.HighHillsStudio.entity;

import jakarta.persistence.*;
import jdk.jfr.Enabled;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Referral {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String token; // unique token for referral URL
    private boolean used = false; // whether the referral has been redeemed

    @ManyToOne
    @JoinColumn(name = "inviter_id")
    private User inviter; // the existing user who shares the referral

    @OneToOne
    @JoinColumn(name = "invitee_id")
    private User invitee; // the new user who registers

}
