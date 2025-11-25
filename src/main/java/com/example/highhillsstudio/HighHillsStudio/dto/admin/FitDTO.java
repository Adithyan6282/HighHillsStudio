package com.example.highhillsstudio.HighHillsStudio.dto.admin;


import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FitDTO {

    private Long id;
    private String size;
    private Integer quantity; // Stock quantity

}
