package com.example.highhillsstudio.HighHillsStudio.dto.user;


import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDTO {

    private String fullname;
    private String email;
    private String phone;
    private String gender;

}
