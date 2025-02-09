package faang.school.projectservice.dto.donation;

import faang.school.projectservice.dto.client.Currency;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class DonationDto {
    private Long id;

    private Long paymentNumber;

    @NotNull
    @DecimalMin(value = "1.0", inclusive = false, message = "Amount must be greater than 1.0")
    private BigDecimal amount;

    private LocalDateTime donationTime = LocalDateTime.now();

    @NotNull
    private long campaignId;

    @NotNull
    private Currency currency;
}
