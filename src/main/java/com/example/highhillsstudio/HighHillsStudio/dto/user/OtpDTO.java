package com.example.highhillsstudio.HighHillsStudio.dto.user;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OtpDTO {

    @NotBlank(message = "Email is required")
    private String email;

//    @NotBlank(message = "OTP code is required")
    private String otpCode; // user enter otp

}
