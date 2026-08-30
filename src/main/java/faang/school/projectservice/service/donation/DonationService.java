package faang.school.projectservice.service.donation;

import faang.school.projectservice.config.context.user.UserContext;
import faang.school.projectservice.dto.client.PaymentResponse;
import faang.school.projectservice.dto.donation.DonationFilterDto;
import faang.school.projectservice.filter.donation.DonationFilter;
import faang.school.projectservice.model.Donation;
import faang.school.projectservice.repository.CampaignRepository;
import faang.school.projectservice.repository.DonationRepository;
import faang.school.projectservice.service.payment.PaymentService;
import faang.school.projectservice.service.user.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Stream;

@Slf4j
@RequiredArgsConstructor
@Service
public class DonationService {
    private final PaymentService paymentService;
    private final DonationIntentService donationIntentService;
    private final DonationRepository donationRepository;
    private final CampaignRepository campaignRepository;
    private final UserService userService;
    private final UserContext userContext;
    private final List<DonationFilter> donationFilters;

    public Donation createDonation(Donation donation, java.util.UUID idempotencyKey) {
        long userId = getUserId();
        if (idempotencyKey == null) {
            throw new IllegalArgumentException("Idempotency-Key is required");
        }
        Donation intent = donationIntentService.createOrLoadIntent(donation, userId, idempotencyKey);
        if (intent.getStatus() == faang.school.projectservice.model.DonationStatus.COMPLETED) {
            return intent;
        }
        PaymentResponse paymentResponse = paymentService.makePayment(
                intent.getPaymentNumber(), intent.getAmount(), intent.getCurrency());
        return donationIntentService.completeIntent(intent.getId(), paymentResponse);
    }

    @Transactional(readOnly = true)
    public Donation getDonationById(long donationId) {
        long userId = getUserId();
        log.debug("Fetching donation ID {} for user ID {}", donationId, userId);

        Donation donation = donationRepository.findById(donationId)
                .orElseThrow(() -> {
                    log.warn("Donation not found by ID: {}", donationId);
                    return new NoSuchElementException("Donation not found");
                });

        if (donation.getUserId() != userId) {
            log.warn("Access denied: User ID {} tried to access donation ID {}", userId, donationId);
            throw new IllegalStateException("Access to donation denied");
        }

        return donation;
    }

    @Transactional(readOnly = true)
    public Page<Donation> getAllUserDonations(DonationFilterDto dtoFilters, Pageable pageable) {
        long userId = getUserId();
        DonationFilterDto filters = dtoFilters == null ? DonationFilterDto.builder().build() : dtoFilters;
        log.info("Fetching all donations for user ID {} with filters: {}", userId, dtoFilters);
        return donationRepository.searchByUserId(userId, filters.getDatePattern(), filters.getCurrencyPattern(),
                filters.getMinAmountPattern(), filters.getMaxAmountPattern(), pageable);
    }

    private Stream<Donation> filterDonations(Stream<Donation> donations, DonationFilterDto dtoFilters) {
        List<DonationFilter> applicableFilters = donationFilters.stream()
                .filter(donationFilter -> donationFilter.isApplicable(dtoFilters))
                .toList();

        log.debug("Applying {} filters to donations", applicableFilters.size());
        return donations.filter(donation ->
                        applicableFilters.stream()
                                .allMatch(donationFilter ->
                                        donationFilter.apply(donation, dtoFilters)))
                .sorted(Comparator.comparing(Donation::getDonationTime).reversed());
    }

    private long getUserId() {
        long userId = userContext.getUserId();
        log.debug("Retrieving user ID from context: {}", userId);
        userService.getUser(userId);
        return userId;
    }
}
