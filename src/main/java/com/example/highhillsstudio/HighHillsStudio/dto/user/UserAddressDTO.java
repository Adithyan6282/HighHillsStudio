package com.example.highhillsstudio.HighHillsStudio.dto.user;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserAddressDTO {

    private Long id;
    private String line1;
    private String line2;
    private String city;
    private String state;
    private String country;
    private String pincode;
    private String phone;
    private boolean isDefault;

}
