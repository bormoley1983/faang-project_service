package faang.school.projectservice.service.donation;

import faang.school.projectservice.dto.client.Currency;
import faang.school.projectservice.dto.client.PaymentResponse;
import faang.school.projectservice.dto.client.PaymentStatus;
import faang.school.projectservice.exception.payment.CampaignNotActiveException;
import faang.school.projectservice.model.Campaign;
import faang.school.projectservice.model.CampaignStatus;
import faang.school.projectservice.model.Donation;
import faang.school.projectservice.model.DonationStatus;
import faang.school.projectservice.repository.CampaignRepository;
import faang.school.projectservice.repository.DonationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DonationIntentServiceTest {

    @InjectMocks
    private DonationIntentService donationIntentService;

    @Mock
    private DonationRepository donationRepository;

    @Mock
    private CampaignRepository campaignRepository;

    private Campaign activeCampaign(long id) {
        return Campaign.builder()
                .id(id)
                .status(CampaignStatus.ACTIVE)
                .build();
    }

    private Donation request(long campaignId, BigDecimal amount, Currency currency) {
        return Donation.builder()
                .campaign(activeCampaign(campaignId))
                .amount(amount)
                .currency(currency)
                .build();
    }

    @Test
    void createOrLoadIntent_whenNoExistingAndCampaignActive_savesPendingIntent() {
        // Arrange
        UUID key = UUID.randomUUID();
        Donation request = request(1L, new BigDecimal("10.00"), Currency.USD);
        when(donationRepository.findByIdempotencyKey(key)).thenReturn(Optional.empty());
        when(campaignRepository.findById(1L)).thenReturn(Optional.of(activeCampaign(1L)));
        when(donationRepository.save(any(Donation.class))).thenAnswer(inv -> inv.getArgument(0));

        // Act
        Donation actual = donationIntentService.createOrLoadIntent(request, 42L, key);

        // Assert
        assertThat(actual.getIdempotencyKey()).isEqualTo(key);
        assertThat(actual.getStatus()).isEqualTo(DonationStatus.PENDING);
        assertThat(actual.getUserId()).isEqualTo(42L);
        assertThat(actual.getAmount()).isEqualByComparingTo("10.00");
        assertThat(actual.getCurrency()).isEqualTo(Currency.USD);
        assertThat(actual.getPaymentNumber()).isPositive();
        verify(donationRepository).save(any(Donation.class));
    }

    @Test
    void createOrLoadIntent_whenExistingSameRequest_returnsExistingWithoutSave() {
        // Arrange
        UUID key = UUID.randomUUID();
        Donation existing = Donation.builder()
                .id(7L)
                .idempotencyKey(key)
                .userId(42L)
                .amount(new BigDecimal("10.00"))
                .currency(Currency.USD)
                .campaign(activeCampaign(1L))
                .status(DonationStatus.PENDING)
                .build();
        when(donationRepository.findByIdempotencyKey(key)).thenReturn(Optional.of(existing));

        // Act
        Donation actual = donationIntentService.createOrLoadIntent(request(1L, new BigDecimal("10.00"), Currency.USD), 42L, key);

        // Assert
        assertThat(actual).isSameAs(existing);
        verify(donationRepository, never()).save(any(Donation.class));
    }

    @Test
    void createOrLoadIntent_whenExistingDifferentUser_throwsAndDoesNotSave() {
        // Arrange
        UUID key = UUID.randomUUID();
        Donation existing = Donation.builder()
                .id(7L)
                .userId(42L)
                .amount(new BigDecimal("10.00"))
                .currency(Currency.USD)
                .campaign(activeCampaign(1L))
                .build();
        when(donationRepository.findByIdempotencyKey(key)).thenReturn(Optional.of(existing));

        // Act / Assert
        assertThatThrownBy(() -> donationIntentService.createOrLoadIntent(request(1L, new BigDecimal("10.00"), Currency.USD), 43L, key))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("already used");
        verify(donationRepository, never()).save(any(Donation.class));
    }

    @Test
    void createOrLoadIntent_whenExistingDifferentCampaign_throws() {
        // Arrange
        UUID key = UUID.randomUUID();
        Donation existing = Donation.builder()
                .id(7L)
                .userId(42L)
                .amount(new BigDecimal("10.00"))
                .currency(Currency.USD)
                .campaign(activeCampaign(1L))
                .build();
        when(donationRepository.findByIdempotencyKey(key)).thenReturn(Optional.of(existing));

        // Act / Assert
        assertThatThrownBy(() -> donationIntentService.createOrLoadIntent(request(2L, new BigDecimal("10.00"), Currency.USD), 42L, key))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void createOrLoadIntent_whenExistingDifferentAmount_throws() {
        // Arrange
        UUID key = UUID.randomUUID();
        Donation existing = Donation.builder()
                .id(7L)
                .userId(42L)
                .amount(new BigDecimal("10.00"))
                .currency(Currency.USD)
                .campaign(activeCampaign(1L))
                .build();
        when(donationRepository.findByIdempotencyKey(key)).thenReturn(Optional.of(existing));

        // Act / Assert
        assertThatThrownBy(() -> donationIntentService.createOrLoadIntent(request(1L, new BigDecimal("20.00"), Currency.USD), 42L, key))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void createOrLoadIntent_whenExistingDifferentCurrency_throws() {
        // Arrange
        UUID key = UUID.randomUUID();
        Donation existing = Donation.builder()
                .id(7L)
                .userId(42L)
                .amount(new BigDecimal("10.00"))
                .currency(Currency.USD)
                .campaign(activeCampaign(1L))
                .build();
        when(donationRepository.findByIdempotencyKey(key)).thenReturn(Optional.of(existing));

        // Act / Assert
        assertThatThrownBy(() -> donationIntentService.createOrLoadIntent(request(1L, new BigDecimal("10.00"), Currency.EUR), 42L, key))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void createOrLoadIntent_whenCampaignMissing_throwsNotFound() {
        // Arrange
        UUID key = UUID.randomUUID();
        when(donationRepository.findByIdempotencyKey(key)).thenReturn(Optional.empty());
        when(campaignRepository.findById(1L)).thenReturn(Optional.empty());

        // Act / Assert
        assertThatThrownBy(() -> donationIntentService.createOrLoadIntent(request(1L, new BigDecimal("10.00"), Currency.USD), 42L, key))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessageContaining("Campaign not found");
        verify(donationRepository, never()).save(any(Donation.class));
    }

    @Test
    void createOrLoadIntent_whenCampaignNotActive_throwsAndDoesNotSave() {
        // Arrange
        UUID key = UUID.randomUUID();
        Campaign canceled = Campaign.builder().id(1L).status(CampaignStatus.CANCELED).build();
        when(donationRepository.findByIdempotencyKey(key)).thenReturn(Optional.empty());
        when(campaignRepository.findById(1L)).thenReturn(Optional.of(canceled));

        // Act / Assert
        assertThatThrownBy(() -> donationIntentService.createOrLoadIntent(request(1L, new BigDecimal("10.00"), Currency.USD), 42L, key))
                .isInstanceOf(CampaignNotActiveException.class);
        verify(donationRepository, never()).save(any(Donation.class));
    }

    @Test
    void completeIntent_whenAlreadyCompleted_returnsDonationWithoutCampaignWrite() {
        // Arrange
        Donation donation = Donation.builder()
                .id(7L)
                .status(DonationStatus.COMPLETED)
                .paymentNumber(99L)
                .amount(new BigDecimal("10.00"))
                .campaign(activeCampaign(1L))
                .build();
        when(donationRepository.findByIdForUpdate(7L)).thenReturn(Optional.of(donation));

        // Act
        Donation actual = donationIntentService.completeIntent(7L, paymentResponse(99L));

        // Assert
        assertThat(actual).isSameAs(donation);
        verify(campaignRepository, never()).findByIdForUpdate(any());
        verify(campaignRepository, never()).save(any(Campaign.class));
    }

    @Test
    void completeIntent_whenSuccessAndGoalReached_marksCampaignCompleted() {
        // Arrange
        Campaign campaign = activeCampaign(1L);
        campaign.setAmountRaised(new BigDecimal("90.00"));
        campaign.setGoal(new BigDecimal("100.00"));
        Donation donation = Donation.builder()
                .id(7L)
                .status(DonationStatus.PENDING)
                .paymentNumber(99L)
                .amount(new BigDecimal("10.00"))
                .campaign(campaign)
                .build();
        when(donationRepository.findByIdForUpdate(7L)).thenReturn(Optional.of(donation));
        when(campaignRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(campaign));
        when(donationRepository.save(any(Donation.class))).thenAnswer(inv -> inv.getArgument(0));

        // Act
        Donation actual = donationIntentService.completeIntent(7L, paymentResponse(99L));

        // Assert
        assertThat(actual.getStatus()).isEqualTo(DonationStatus.COMPLETED);
        assertThat(campaign.getAmountRaised()).isEqualByComparingTo("100.00");
        assertThat(campaign.getStatus()).isEqualTo(CampaignStatus.COMPLETED);
        verify(campaignRepository).save(campaign);
    }

    @Test
    void completeIntent_whenSuccessAndGoalNotReached_keepsCampaignActive() {
        // Arrange
        Campaign campaign = activeCampaign(1L);
        campaign.setAmountRaised(null);
        campaign.setGoal(new BigDecimal("100.00"));
        Donation donation = Donation.builder()
                .id(7L)
                .status(DonationStatus.PENDING)
                .paymentNumber(99L)
                .amount(new BigDecimal("10.00"))
                .campaign(campaign)
                .build();
        when(donationRepository.findByIdForUpdate(7L)).thenReturn(Optional.of(donation));
        when(campaignRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(campaign));
        when(donationRepository.save(any(Donation.class))).thenAnswer(inv -> inv.getArgument(0));

        // Act
        Donation actual = donationIntentService.completeIntent(7L, paymentResponse(99L));

        // Assert
        assertThat(actual.getStatus()).isEqualTo(DonationStatus.COMPLETED);
        assertThat(campaign.getAmountRaised()).isEqualByComparingTo("10.00");
        assertThat(campaign.getStatus()).isEqualTo(CampaignStatus.ACTIVE);
    }

    @Test
    void completeIntent_whenSuccessAndNoGoal_keepsCampaignActive() {
        // Arrange
        Campaign campaign = activeCampaign(1L);
        campaign.setAmountRaised(new BigDecimal("5.00"));
        Donation donation = Donation.builder()
                .id(7L)
                .status(DonationStatus.PENDING)
                .paymentNumber(99L)
                .amount(new BigDecimal("10.00"))
                .campaign(campaign)
                .build();
        when(donationRepository.findByIdForUpdate(7L)).thenReturn(Optional.of(donation));
        when(campaignRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(campaign));
        when(donationRepository.save(any(Donation.class))).thenAnswer(inv -> inv.getArgument(0));

        // Act
        Donation actual = donationIntentService.completeIntent(7L, paymentResponse(99L));

        // Assert
        assertThat(actual.getStatus()).isEqualTo(DonationStatus.COMPLETED);
        assertThat(campaign.getAmountRaised()).isEqualByComparingTo("15.00");
        assertThat(campaign.getStatus()).isEqualTo(CampaignStatus.ACTIVE);
    }

    @Test
    void completeIntent_whenDonationMissing_throwsNotFound() {
        // Arrange
        when(donationRepository.findByIdForUpdate(7L)).thenReturn(Optional.empty());

        // Act / Assert
        assertThatThrownBy(() -> donationIntentService.completeIntent(7L, paymentResponse(99L)))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessageContaining("Donation intent not found");
    }

    @Test
    void completeIntent_whenResponseNull_throwsMismatch() {
        // Arrange
        Donation donation = pendingDonation();
        when(donationRepository.findByIdForUpdate(7L)).thenReturn(Optional.of(donation));

        // Act / Assert
        assertThatThrownBy(() -> donationIntentService.completeIntent(7L, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("does not match");
        verify(campaignRepository, never()).save(any(Campaign.class));
    }

    @Test
    void completeIntent_whenPaymentNumberMismatch_throwsMismatch() {
        // Arrange
        Donation donation = pendingDonation();
        when(donationRepository.findByIdForUpdate(7L)).thenReturn(Optional.of(donation));

        // Act / Assert
        assertThatThrownBy(() -> donationIntentService.completeIntent(7L, paymentResponse(100L)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void completeIntent_whenCampaignMissing_throwsNotFound() {
        // Arrange
        Donation donation = pendingDonation();
        when(donationRepository.findByIdForUpdate(7L)).thenReturn(Optional.of(donation));
        when(campaignRepository.findByIdForUpdate(1L)).thenReturn(Optional.empty());

        // Act / Assert
        assertThatThrownBy(() -> donationIntentService.completeIntent(7L, paymentResponse(99L)))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessageContaining("Campaign not found");
    }

    private Donation pendingDonation() {
        return Donation.builder()
                .id(7L)
                .status(DonationStatus.PENDING)
                .paymentNumber(99L)
                .amount(new BigDecimal("10.00"))
                .campaign(activeCampaign(1L))
                .build();
    }

    private PaymentResponse paymentResponse(long paymentNumber) {
        return PaymentResponse.builder()
                .status(PaymentStatus.SUCCESS)
                .paymentNumber(paymentNumber)
                .build();
    }
}
