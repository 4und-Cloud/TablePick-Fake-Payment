package com.goorm.tablepick.payment.event.consumer;

import com.goorm.tablepick.PgClient;
import com.goorm.tablepick.payment.dto.PaymentRequestDto;
import com.goorm.tablepick.payment.dto.PaymentResponseDto;
import com.goorm.tablepick.payment.event.model.PaymentFailedEvent;
import com.goorm.tablepick.payment.event.model.PaymentRedirectEvent;
import com.goorm.tablepick.payment.event.model.PaymentRequestEvent;
import com.goorm.tablepick.payment.service.KafkaPaymentProducer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class KafkaPaymentConsumer {
    private final PgClient pgClient; // 외부 PG사 API 호출 클라이언트
    private final KafkaPaymentProducer kafkaPaymentProducer; // 이벤트 발행을 위해 주입

    @KafkaListener(topics = "payment-request-topic", groupId = "payment-group")
    public void handlePaymentRequestEvent(PaymentRequestEvent event) {
        log.info("[Payment Domain] 결제 요청 이벤트 수신: {}", event);

        // 1. PG사에 보낼 요청 DTO 생성
        PaymentRequestDto pgRequest = PaymentRequestDto.builder()
                .reservationId(event.getReservationId())
                .memberId(event.getMemberId())
                .amount(event.getAmount())
                // PG사에 필요한 다른 정보 추가 (ex. 상품명, 결제 성공/실패 콜백 URL 등)
                .build();

        // 2. 외부 PG사 API 호출
        PaymentResponseDto pgResponse = pgClient.callPgApi(pgRequest);

        // 3. PG사 응답 처리 및 이벤트 발행
        if (pgResponse.isSuccess()) {
            // 결제 URL 반환 성공 (아직 실제 결제가 완료된 것은 아님)
            PaymentRedirectEvent redirectEvent = PaymentRedirectEvent.builder()
                    .reservationId(event.getReservationId())
                    .paymentUrl(pgResponse.getPaymentUrl()) // PG사에서 받은 결제 URL
                    .tid(pgResponse.getTid()) // PG사 거래 ID
                    .build();
            kafkaPaymentProducer.sendPaymentRedirectEvent(redirectEvent);
            log.info("[Payment Domain] 결제 리다이렉트 이벤트 발행 완료. 예약 ID: {}, URL: {}", event.getReservationId(),
                    pgResponse.getPaymentUrl());

            // 이후 PG사의 콜백(Webhook)을 통해 실제 결제 성공/실패 여부를 받아서 PaymentSuccessEvent/PaymentFailedEvent를 발행
            // 이 부분은 PG사 콜백을 처리하는 별도의 Controller/Service에서 구현되어야 합니다.
            // 예를 들어, /api/payments/pg-callback 엔드포인트에서 PG사로부터 성공/실패 알림을 받으면
            // 해당 로직 내에서 PaymentSuccessEvent 또는 PaymentFailedEvent를 발행하는 것입니다.

        } else {
            // PG사 연동 과정에서 실패 (예: 요청 파라미터 오류 등)
            log.error("[Payment Domain] 외부 PG사 연동 실패. 예약 ID: {}, 오류: {}", event.getReservationId(),
                    pgResponse.getErrorMessage());
            PaymentFailedEvent failedEvent = PaymentFailedEvent.builder()
                    .reservationId(event.getReservationId())
                    .errorMessage("PG사 연동 오류: " + pgResponse.getErrorMessage())
                    .build();
            kafkaPaymentProducer.sendPaymentFailedEvent(failedEvent); // 이 경우 예약은 즉시 실패 처리
        }
    }
}