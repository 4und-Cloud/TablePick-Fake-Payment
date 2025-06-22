package com.goorm.tablepick.payment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// PG사 성공 콜백 시 데이터 매핑용 DTO
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PgCallbackSuccessDto {
    private Long reservationId; // PG사가 우리에게 다시 넘겨줄 예약 ID
    private String paymentId; // PG사에서 발급한 최종 결제 ID
    private Long amount;      // 결제 금액
    // 실제 PG사 연동 시 필요한 추가 필드 (예: approvalTime, cardType 등)
}
