package com.example.webshopping.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Log4j2
public class OrderRequestDTO {

    private String recipientName;
    private String recipientPhone;
    private String deliveryAddress;
    private String deliveryMessage;


}
