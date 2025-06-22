package com.goorm.tablepick.payment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// PG사 실패 콜백 시 데이터 매핑용 DTO
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PgCallbackFailDto {
    private Long reservationId; // PG사가 우리에게 다시 넘겨줄 예약 ID
    private String errorMessage; // PG사에서 전달하는 실패 메시지
    // 실제 PG사 연동 시 필요한 추가 필드
}