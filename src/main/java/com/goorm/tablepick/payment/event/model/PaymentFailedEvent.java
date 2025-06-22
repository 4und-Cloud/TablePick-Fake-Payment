package com.goorm.tablepick.payment.event.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentFailedEvent {
    private Long reservationId;
    private String errorMessage; // 실패 시 오류 메시지
}
