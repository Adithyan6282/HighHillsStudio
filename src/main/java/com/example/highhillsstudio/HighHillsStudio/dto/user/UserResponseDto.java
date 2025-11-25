package com.example.highhillsstudio.HighHillsStudio.dto.user;


import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponseDto {

    private String fullName;
    private String email;
    private String phone;
    private String gender;
    private boolean enabled;

}
