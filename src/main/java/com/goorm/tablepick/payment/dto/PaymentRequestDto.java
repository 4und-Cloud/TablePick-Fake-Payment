package com.goorm.tablepick.payment.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PaymentRequestDto {
    private Long reservationId;
    private Long userId;
    private int amount;
}
