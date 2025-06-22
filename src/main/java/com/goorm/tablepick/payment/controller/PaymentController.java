package com.goorm.tablepick.payment.controller;

import com.goorm.tablepick.PgClient;
import com.goorm.tablepick.payment.dto.PaymentRequestDto;
import com.goorm.tablepick.payment.dto.PaymentResponseDto;
import com.goorm.tablepick.payment.dto.PgCallbackFailDto;
import com.goorm.tablepick.payment.dto.PgCallbackSuccessDto;
import com.goorm.tablepick.payment.event.model.PaymentFailedEvent;
import com.goorm.tablepick.payment.event.model.PaymentRequestEvent;
import com.goorm.tablepick.payment.event.model.PaymentSuccessEvent;
import com.goorm.tablepick.payment.service.KafkaPaymentProducer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@Slf4j
public class PaymentController {

    private final KafkaPaymentProducer kafkaPaymentProducer;
    private final PgClient pgClient;

    // 클라이언트로부터 결제 요청을 받는 초기 엔드포인트
    // 실제 SAGA 패턴에서는 예약 도메인이 임시 예약을 생성하고 이 이벤트를 Kafka로 발행합니다.
    // 결제 도메인은 이 이벤트를 Kafka 리스너를 통해 수신하고 PG사 통신을 시작합니다.
    // 따라서 이 엔드포인트는 실제 서비스에서 필요 없을 수 있습니다.
    // 만약 클라이언트가 직접 결제 도메인에 결제를 요청하는 시나리오라면 유지.
//    @PostMapping("/request")
//    public ResponseEntity<String> requestPayment(@RequestBody PaymentRequestEvent request) {
//        log.info("[Payment Domain] 클라이언트로부터 결제 요청 수신 (초기): {}", request);
//        kafkaPaymentProducer.sendPaymentRequest(request); // Kafka로 결제 요청 이벤트 발행
//        return ResponseEntity.ok("Payment request initiated. Please wait for redirect URL or status update.");
//    }

    @PostMapping
    public ResponseEntity<PaymentResponseDto> processPaymentSync(@RequestBody PaymentRequestDto request) {
        // 예약 도메인으로부터 결제 요청을 받으면, Fake PG 서버로 바로 전달하고 응답을 받습니다.
        PaymentResponseDto responseDto = pgClient.callPgApi(request); // Fake PG 서버 호출
        return ResponseEntity.ok(responseDto);
    }

    // PG사에서 결제 완료 후 호출하는 콜백 엔드포인트
    // 이 메서드는 외부 PG사가 설정된 콜백 URL (예: http://localhost:8080/api/payments/pg-callback/success) 로 POST 요청을 보낼 때 호출됩니다.
    @PostMapping("/pg-callback/success")
    public ResponseEntity<String> handlePgSuccessCallback(@RequestBody PgCallbackSuccessDto callbackDto) {
        log.info("[Payment Domain] PG사 결제 성공 콜백 수신: {}", callbackDto);


        // 콜백 데이터에서 필요한 정보 추출
        Long reservationId = callbackDto.getReservationId();
        String paymentId = callbackDto.getPaymentId(); // PG사에서 발급한 최종 결제 ID
        Long amount = callbackDto.getAmount();

        // Kafka를 통해 예약 도메인에 결제 성공 이벤트 전송
        PaymentSuccessEvent successEvent = PaymentSuccessEvent.builder()
                .reservationId(reservationId)
                .paymentId(paymentId)
                .amount(amount)
                .build();
        kafkaPaymentProducer.sendPaymentSuccessEvent(successEvent);

        return ResponseEntity.ok("SUCCESS"); // PG사에게 성공적으로 콜백을 받았음을 알림
    }

    // PG사에서 결제 실패 후 호출하는 콜백 엔드포인트
    // 이 메서드는 외부 PG사가 설정된 콜백 URL (예: http://localhost:8080/api/payments/pg-callback/fail) 로 POST 요청을 보낼 때 호출됩니다.
    @PostMapping("/pg-callback/fail")
    public ResponseEntity<String> handlePgFailCallback(@RequestBody PgCallbackFailDto callbackDto) {
        log.warn("[Payment Domain] PG사 결제 실패 콜백 수신: {}", callbackDto);


        // 콜백 데이터에서 필요한 정보 추출
        Long reservationId = callbackDto.getReservationId();
        String errorMessage = callbackDto.getErrorMessage();

        // Kafka를 통해 예약 도메인에 결제 실패 이벤트 전송
        PaymentFailedEvent failedEvent = PaymentFailedEvent.builder()
                .reservationId(reservationId)
                .errorMessage(errorMessage)
                .build();
        kafkaPaymentProducer.sendPaymentFailedEvent(failedEvent);

        return ResponseEntity.ok("FAIL"); // PG사에게 실패 콜백을 받았음을 알림
    }
}
