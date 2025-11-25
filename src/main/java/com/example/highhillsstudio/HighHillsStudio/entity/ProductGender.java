package com.example.highhillsstudio.HighHillsStudio.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "product_genders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductGender {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String name; // Men, Women, Unisex

}
