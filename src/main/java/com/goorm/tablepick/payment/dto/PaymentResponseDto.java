package com.goorm.tablepick.payment.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PaymentResponseDto {
    private boolean success;
    private String paymentId;
    private String errorMessage;
}