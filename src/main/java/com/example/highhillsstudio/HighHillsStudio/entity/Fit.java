package com.example.highhillsstudio.HighHillsStudio.entity;

import com.fasterxml.jackson.annotation.JacksonInject;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Entity
@Table(name = "fits")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Fit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @Column(nullable = false, length = 30)
    private String size; // S, M, L, XL

    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    @JsonIgnore
    private Product product;

    @OneToOne(mappedBy = "fit", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private Stock stock;


    @Column(nullable = false)
    private Boolean isActive = true;
}
