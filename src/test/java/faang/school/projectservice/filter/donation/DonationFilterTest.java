package faang.school.projectservice.filter.donation;

import faang.school.projectservice.dto.client.Currency;
import faang.school.projectservice.dto.donation.DonationFilterDto;
import faang.school.projectservice.model.Donation;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for all donation filters.
 */
@DisplayName("Donation Filters")
class DonationFilterTest {

    private Donation buildDonation(LocalDate date, BigDecimal amount, Currency currency) {
        return Donation.builder()
                .donationTime(LocalDateTime.of(date, java.time.LocalTime.NOON))
                .amount(amount)
                .currency(currency)
                .build();
    }

    // ── DonationDateFilter ─────────────────────────────────────────────────────

    @Nested
    @DisplayName("DonationDateFilter")
    class DateFilter {

        private final DonationDateFilter filter = new DonationDateFilter();

        @Test
        @DisplayName("isApplicable returns true when datePattern is set")
        void applicableWhenDateSet() {
            DonationFilterDto dto = DonationFilterDto.builder().datePattern(LocalDate.of(2025, 1, 1)).build();
            assertThat(filter.isApplicable(dto)).isTrue();
        }

        @Test
        @DisplayName("isApplicable returns false when datePattern is null")
        void notApplicableWhenDateNull() {
            DonationFilterDto dto = DonationFilterDto.builder().build();
            assertThat(filter.isApplicable(dto)).isFalse();
        }

        @Test
        @DisplayName("isApplicable returns false when filters is null")
        void notApplicableWhenFiltersNull() {
            assertThat(filter.isApplicable(null)).isFalse();
        }

        @Test
        @DisplayName("apply returns true when dates match")
        void applyTrueWhenMatch() {
            Donation donation = buildDonation(LocalDate.of(2025, 1, 1), BigDecimal.TEN, Currency.USD);
            DonationFilterDto dto = DonationFilterDto.builder().datePattern(LocalDate.of(2025, 1, 1)).build();
            assertThat(filter.apply(donation, dto)).isTrue();
        }

        @Test
        @DisplayName("apply returns false when dates differ")
        void applyFalseWhenMismatch() {
            Donation donation = buildDonation(LocalDate.of(2025, 1, 1), BigDecimal.TEN, Currency.USD);
            DonationFilterDto dto = DonationFilterDto.builder().datePattern(LocalDate.of(2025, 2, 1)).build();
            assertThat(filter.apply(donation, dto)).isFalse();
        }
    }

    // ── DonationMaxAmountFilter ────────────────────────────────────────────────

    @Nested
    @DisplayName("DonationMaxAmountFilter")
    class MaxAmountFilter {

        private final DonationMaxAmountFilter filter = new DonationMaxAmountFilter();

        @Test
        @DisplayName("isApplicable returns true when maxAmountPattern is set")
        void applicableWhenMaxSet() {
            DonationFilterDto dto = DonationFilterDto.builder().maxAmountPattern(BigDecimal.TEN).build();
            assertThat(filter.isApplicable(dto)).isTrue();
        }

        @Test
        @DisplayName("isApplicable returns false when maxAmountPattern is null")
        void notApplicableWhenMaxNull() {
            DonationFilterDto dto = DonationFilterDto.builder().build();
            assertThat(filter.isApplicable(dto)).isFalse();
        }

        @Test
        @DisplayName("apply returns true when amount <= max")
        void applyTrueWhenBelowMax() {
            Donation donation = buildDonation(LocalDate.of(2025, 1, 1), new BigDecimal("5.00"), Currency.USD);
            DonationFilterDto dto = DonationFilterDto.builder().maxAmountPattern(new BigDecimal("10.00")).build();
            assertThat(filter.apply(donation, dto)).isTrue();
        }

        @Test
        @DisplayName("apply returns true when amount == max")
        void applyTrueWhenEqualMax() {
            Donation donation = buildDonation(LocalDate.of(2025, 1, 1), new BigDecimal("10.00"), Currency.USD);
            DonationFilterDto dto = DonationFilterDto.builder().maxAmountPattern(new BigDecimal("10.00")).build();
            assertThat(filter.apply(donation, dto)).isTrue();
        }

        @Test
        @DisplayName("apply returns false when amount > max")
        void applyFalseWhenAboveMax() {
            Donation donation = buildDonation(LocalDate.of(2025, 1, 1), new BigDecimal("15.00"), Currency.USD);
            DonationFilterDto dto = DonationFilterDto.builder().maxAmountPattern(new BigDecimal("10.00")).build();
            assertThat(filter.apply(donation, dto)).isFalse();
        }
    }

    // ── DonationMinAmountFilter ────────────────────────────────────────────────

    @Nested
    @DisplayName("DonationMinAmountFilter")
    class MinAmountFilter {

        private final DonationMinAmountFilter filter = new DonationMinAmountFilter();

        @Test
        @DisplayName("isApplicable returns true when minAmountPattern is set")
        void applicableWhenMinSet() {
            DonationFilterDto dto = DonationFilterDto.builder().minAmountPattern(BigDecimal.TEN).build();
            assertThat(filter.isApplicable(dto)).isTrue();
        }

        @Test
        @DisplayName("isApplicable returns false when minAmountPattern is null")
        void notApplicableWhenMinNull() {
            DonationFilterDto dto = DonationFilterDto.builder().build();
            assertThat(filter.isApplicable(dto)).isFalse();
        }

        @Test
        @DisplayName("apply returns true when amount >= min")
        void applyTrueWhenAboveMin() {
            Donation donation = buildDonation(LocalDate.of(2025, 1, 1), new BigDecimal("15.00"), Currency.USD);
            DonationFilterDto dto = DonationFilterDto.builder().minAmountPattern(new BigDecimal("10.00")).build();
            assertThat(filter.apply(donation, dto)).isTrue();
        }

        @Test
        @DisplayName("apply returns true when amount == min")
        void applyTrueWhenEqualMin() {
            Donation donation = buildDonation(LocalDate.of(2025, 1, 1), new BigDecimal("10.00"), Currency.USD);
            DonationFilterDto dto = DonationFilterDto.builder().minAmountPattern(new BigDecimal("10.00")).build();
            assertThat(filter.apply(donation, dto)).isTrue();
        }

        @Test
        @DisplayName("apply returns false when amount < min")
        void applyFalseWhenBelowMin() {
            Donation donation = buildDonation(LocalDate.of(2025, 1, 1), new BigDecimal("5.00"), Currency.USD);
            DonationFilterDto dto = DonationFilterDto.builder().minAmountPattern(new BigDecimal("10.00")).build();
            assertThat(filter.apply(donation, dto)).isFalse();
        }
    }

    // ── DonationCurrencyPattern ────────────────────────────────────────────────

    @Nested
    @DisplayName("DonationCurrencyPattern")
    class CurrencyFilter {

        private final DonationCurrencyPattern filter = new DonationCurrencyPattern();

        @Test
        @DisplayName("isApplicable returns true when currencyPattern is set")
        void applicableWhenCurrencySet() {
            DonationFilterDto dto = DonationFilterDto.builder().currencyPattern(Currency.USD).build();
            assertThat(filter.isApplicable(dto)).isTrue();
        }

        @Test
        @DisplayName("isApplicable returns false when currencyPattern is null")
        void notApplicableWhenCurrencyNull() {
            DonationFilterDto dto = DonationFilterDto.builder().build();
            assertThat(filter.isApplicable(dto)).isFalse();
        }

        @Test
        @DisplayName("apply returns true when currencies match")
        void applyTrueWhenMatch() {
            Donation donation = buildDonation(LocalDate.of(2025, 1, 1), BigDecimal.TEN, Currency.USD);
            DonationFilterDto dto = DonationFilterDto.builder().currencyPattern(Currency.USD).build();
            assertThat(filter.apply(donation, dto)).isTrue();
        }

        @Test
        @DisplayName("apply returns false when currencies differ")
        void applyFalseWhenMismatch() {
            Donation donation = buildDonation(LocalDate.of(2025, 1, 1), BigDecimal.TEN, Currency.USD);
            DonationFilterDto dto = DonationFilterDto.builder().currencyPattern(Currency.EUR).build();
            assertThat(filter.apply(donation, dto)).isFalse();
        }
    }
}
