package faang.school.projectservice.service.payment;

import faang.school.projectservice.client.feign.PaymentServiceClient;
import faang.school.projectservice.dto.client.Currency;
import faang.school.projectservice.dto.client.PaymentRequest;
import faang.school.projectservice.dto.client.PaymentResponse;
import faang.school.projectservice.exception.payment.PaymentFailedException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Slf4j
@RequiredArgsConstructor
@Service
public class PaymentService {
    private final PaymentServiceClient paymentServiceClient;

    public PaymentResponse makePayment(long paymentNumber, BigDecimal amount, Currency currency) {
        log.info("Initiating payment for amount {} {}", amount, currency);

        PaymentRequest paymentRequest = new PaymentRequest(paymentNumber, amount, currency);

        ResponseEntity<PaymentResponse> paymentResponse = paymentServiceClient.sendPayment(paymentRequest);
        log.debug("Received payment response: {}", paymentResponse);

        if (paymentResponse.getStatusCode() != HttpStatus.OK || paymentResponse.getBody() == null
                || paymentResponse.getBody().status() != faang.school.projectservice.dto.client.PaymentStatus.SUCCESS
                || paymentResponse.getBody().paymentNumber() != paymentNumber) {
            log.error("Payment failed. Status: {}, Response Body: {}",
                    paymentResponse.getStatusCode(),
                    paymentResponse.getBody());
            throw new PaymentFailedException("Payment failed for amount " + amount + " " + currency);
        }

        log.info("Payment processed successfully. Payment number: {}", paymentNumber);
        return paymentResponse.getBody();
    }
}
