package com.goorm.tablepick.payment.event.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentRequestEvent {
    private Long reservationId;
    private Long memberId;
    private Long amount;
}
