package com.example.highhillsstudio.HighHillsStudio.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.boot.autoconfigure.web.WebProperties;

@Entity
@Table(name = "user_genders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserGender {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String name; // Male, Female, Other

}
