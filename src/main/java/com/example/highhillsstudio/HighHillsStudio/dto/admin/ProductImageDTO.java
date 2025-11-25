package com.example.highhillsstudio.HighHillsStudio.dto.admin;


import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductImageDTO {


    private Long id;
    private String img;
    private Boolean isMain;
}
