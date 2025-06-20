package com.goorm.tablepick;

import com.goorm.tablepick.payment.dto.PaymentRequestDto;
import com.goorm.tablepick.payment.dto.PaymentResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
@RequiredArgsConstructor
public class PgClient {

    private final WebClient webClient;

    public PaymentResponseDto callPgApi(PaymentRequestDto request) {
        return webClient.post()
                .uri("http://localhost:8083/api/pg/approve")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(PaymentResponseDto.class)
                .block(); // 동기 처리
    }
}
