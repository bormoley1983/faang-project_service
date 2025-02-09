package faang.school.projectservice.dto.donation;

import faang.school.projectservice.dto.client.Currency;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@AllArgsConstructor
@Builder
@Data
public class DonationFilterDto {
    private LocalDate datePattern;
    private Currency currencyPattern;
    private BigDecimal maxAmountPattern;
    private BigDecimal minAmountPattern;
}
