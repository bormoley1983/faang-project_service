package faang.school.projectservice.service.donation;

import faang.school.projectservice.dto.client.PaymentResponse;
import faang.school.projectservice.dto.client.PaymentStatus;
import faang.school.projectservice.model.Campaign;
import faang.school.projectservice.model.CampaignStatus;
import faang.school.projectservice.model.Donation;
import faang.school.projectservice.model.DonationStatus;
import faang.school.projectservice.repository.CampaignRepository;
import faang.school.projectservice.repository.DonationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DonationIntentService {

    private final DonationRepository donationRepository;
    private final CampaignRepository campaignRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Donation createOrLoadIntent(Donation request, long userId, UUID idempotencyKey) {
        Donation existing = donationRepository.findByIdempotencyKey(idempotencyKey).orElse(null);
        if (existing != null) {
            validateSameRequest(existing, request, userId);
            return existing;
        }

        long campaignId = request.getCampaign().getId();
        Campaign campaign = campaignRepository.findById(campaignId)
                .orElseThrow(() -> new NoSuchElementException("Campaign not found"));
        if (campaign.getStatus() != CampaignStatus.ACTIVE) {
            throw new faang.school.projectservice.exception.payment.CampaignNotActiveException(
                    "Campaign is not active");
        }

        Donation intent = Donation.builder()
                .idempotencyKey(idempotencyKey)
                .paymentNumber(stablePaymentNumber(idempotencyKey))
                .status(DonationStatus.PENDING)
                .amount(request.getAmount())
                .currency(request.getCurrency())
                .campaign(campaign)
                .userId(userId)
                .donationTime(LocalDateTime.now())
                .build();
        return donationRepository.save(intent);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Donation completeIntent(long donationId, PaymentResponse response) {
        Donation donation = donationRepository.findByIdForUpdate(donationId)
                .orElseThrow(() -> new NoSuchElementException("Donation intent not found"));
        if (donation.getStatus() == DonationStatus.COMPLETED) {
            return donation;
        }
        if (response == null || response.status() != PaymentStatus.SUCCESS
                || response.paymentNumber() != donation.getPaymentNumber()) {
            throw new IllegalStateException("Payment response does not match donation intent");
        }

        Campaign campaign = campaignRepository.findByIdForUpdate(donation.getCampaign().getId())
                .orElseThrow(() -> new NoSuchElementException("Campaign not found"));
        BigDecimal amountRaised = campaign.getAmountRaised() == null
                ? BigDecimal.ZERO : campaign.getAmountRaised();
        campaign.setAmountRaised(amountRaised.add(donation.getAmount()));
        if (campaign.getGoal() != null && campaign.getAmountRaised().compareTo(campaign.getGoal()) >= 0) {
            campaign.setStatus(CampaignStatus.COMPLETED);
        }
        donation.setStatus(DonationStatus.COMPLETED);
        campaignRepository.save(campaign);
        return donationRepository.save(donation);
    }

    private void validateSameRequest(Donation existing, Donation request, long userId) {
        if (!Objects.equals(existing.getUserId(), userId)
                || !Objects.equals(existing.getCampaign().getId(), request.getCampaign().getId())
                || existing.getAmount().compareTo(request.getAmount()) != 0
                || existing.getCurrency() != request.getCurrency()) {
            throw new IllegalArgumentException("Idempotency key was already used for another donation");
        }
    }

    private long stablePaymentNumber(UUID idempotencyKey) {
        long value = (idempotencyKey.getMostSignificantBits() ^ idempotencyKey.getLeastSignificantBits())
                & Long.MAX_VALUE;
        return value == 0 ? 1 : value;
    }
}
