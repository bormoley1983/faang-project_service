package faang.school.projectservice.dto.campaign;

import com.fasterxml.jackson.annotation.JsonFormat;
import faang.school.projectservice.dto.client.Currency;
import faang.school.projectservice.model.CampaignStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class CampaignDto {

    private Long id;

    private String title;

    private String description;

    private BigDecimal goalAmount;

    private BigDecimal amountRaised;

    private Currency currency;

    private Long projectId;

    private CampaignStatus status;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    private Long idCreatedBy;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;

    private Long idUpdatedBy;
}
