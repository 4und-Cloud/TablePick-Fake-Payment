package com.goorm.tablepick.payment.service;

import com.goorm.tablepick.payment.event.model.PaymentRequestEvent;
import com.goorm.tablepick.payment.event.model.PaymentRedirectEvent; // 추가
import com.goorm.tablepick.payment.event.model.PaymentFailedEvent;
import com.goorm.tablepick.payment.event.model.PaymentSuccessEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class KafkaPaymentProducer {
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void sendPaymentRequest(PaymentRequestEvent event) {
        kafkaTemplate.send("payment-request-topic", event.getReservationId().toString(), event)
                .whenComplete((result, ex) -> {
                    if (ex == null) {
                        log.info("[Payment Domain] 결제 요청 이벤트 전송 완료: [{}], 오프셋: [{}]", event, result.getRecordMetadata().offset());
                    } else {
                        log.error("[Payment Domain] 결제 요청 이벤트 전송 실패: [{}], 오류: {}", event, ex.getMessage());
                    }
                });
    }

    public void sendPaymentRedirectEvent(PaymentRedirectEvent event) { // 새로 추가된 메서드
        kafkaTemplate.send("payment-redirect-topic", event.getReservationId().toString(), event)
                .whenComplete((result, ex) -> {
                    if (ex == null) {
                        log.info("[Payment Domain] 결제 리다이렉트 이벤트 전송 완료: [{}], 오프셋: [{}]", event, result.getRecordMetadata().offset());
                    } else {
                        log.error("[Payment Domain] 결제 리다이렉트 이벤트 전송 실패: [{}], 오류: {}", event, ex.getMessage());
                    }
                });
    }

    public void sendPaymentSuccessEvent(PaymentSuccessEvent event) {
        kafkaTemplate.send("payment-success-topic", event.getReservationId().toString(), event)
                .whenComplete((result, ex) -> {
                    if (ex == null) {
                        log.info("[Payment Domain] 결제 성공 이벤트 전송 완료: [{}], 오프셋: [{}]", event, result.getRecordMetadata().offset());
                    } else {
                        log.error("[Payment Domain] 결제 성공 이벤트 전송 실패: [{}], 오류: {}", event, ex.getMessage());
                    }
                });
    }

    public void sendPaymentFailedEvent(PaymentFailedEvent event) {
        kafkaTemplate.send("payment-failed-topic", event.getReservationId().toString(), event)
                .whenComplete((result, ex) -> {
                    if (ex == null) {
                        log.info("[Payment Domain] 결제 실패 이벤트 전송 완료: [{}], 오프셋: [{}]", event, result.getRecordMetadata().offset());
                    } else {
                        log.error("[Payment Domain] 결제 실패 이벤트 전송 실패: [{}], 오류: {}", event, ex.getMessage());
                    }
                });
    }
}