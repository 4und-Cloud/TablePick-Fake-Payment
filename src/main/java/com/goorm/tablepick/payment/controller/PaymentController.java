package com.goorm.tablepick.payment.controller;

import com.goorm.tablepick.PgClient;
import com.goorm.tablepick.payment.dto.PaymentRequestDto;
import com.goorm.tablepick.payment.dto.PaymentResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PgClient pgClient;

    @PostMapping
    public ResponseEntity<PaymentResponseDto> register(@RequestBody PaymentRequestDto request) {
        // 외부 PG사 REST API 호출
        PaymentResponseDto responseDto = pgClient.callPgApi(request); // WebClient 사용

        return ResponseEntity.ok(responseDto);
    }
}
