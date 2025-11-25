package com.example.highhillsstudio.HighHillsStudio.dto.response;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApiResponse<T> {

    private String status; // "success" or "error"
    private String message;
    private T data;
}
