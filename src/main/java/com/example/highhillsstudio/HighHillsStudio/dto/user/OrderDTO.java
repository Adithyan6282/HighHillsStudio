package com.example.highhillsstudio.HighHillsStudio.dto.user;

import com.example.highhillsstudio.HighHillsStudio.entity.UserAddress;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderDTO {

    private String orderCode;
    private String status;
    private BigDecimal totalAmount;

    private String userName;
    private String userEmail;

    private LocalDateTime placedAt;


    private List<OrderItemDTO> items;
    private UserAddress address;

    // test
    private BigDecimal finalAmount;

}
