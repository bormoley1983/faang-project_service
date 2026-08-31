package faang.school.projectservice.validator;

import faang.school.projectservice.dto.project.InternshipDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for {@link InternshipValidator}.
 */
@DisplayName("InternshipValidator")
class InternshipValidatorTest {

    private InternshipValidator validator;

    @BeforeEach
    void setUp() {
        validator = new InternshipValidator();
    }

    private InternshipDto validDto() {
        return InternshipDto.builder()
                .id(1L)
                .startDate(LocalDateTime.of(2025, 1, 1, 0, 0))
                .endDate(LocalDateTime.of(2025, 3, 1, 0, 0)) // ~59 days
                .internIds(List.of(10L, 20L))
                .build();
    }

    // ── validateForCreation ────────────────────────────────────────────────────

    @Nested
    @DisplayName("validateForCreation")
    class ValidateForCreation {

        @Test
        @DisplayName("passes with valid data")
        void passesWhenValid() {
            assertThatCode(() -> validator.validateForCreation(validDto())).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("throws when startDate is null")
        void throwsWhenStartDateNull() {
            InternshipDto dto = InternshipDto.builder()
                    .startDate(null)
                    .endDate(LocalDateTime.of(2025, 3, 1, 0, 0))
                    .internIds(List.of(10L))
                    .build();

            assertThatThrownBy(() -> validator.validateForCreation(dto))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Start Date");
        }

        @Test
        @DisplayName("throws when endDate is null")
        void throwsWhenEndDateNull() {
            InternshipDto dto = InternshipDto.builder()
                    .startDate(LocalDateTime.of(2025, 1, 1, 0, 0))
                    .endDate(null)
                    .internIds(List.of(10L))
                    .build();

            assertThatThrownBy(() -> validator.validateForCreation(dto))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("End Date");
        }

        @Test
        @DisplayName("throws when internIds is null")
        void throwsWhenInternIdsNull() {
            InternshipDto dto = InternshipDto.builder()
                    .startDate(LocalDateTime.of(2025, 1, 1, 0, 0))
                    .endDate(LocalDateTime.of(2025, 3, 1, 0, 0))
                    .internIds(null)
                    .build();

            assertThatThrownBy(() -> validator.validateForCreation(dto))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Intern IDs");
        }

        @Test
        @DisplayName("throws when internIds is empty")
        void throwsWhenInternIdsEmpty() {
            InternshipDto dto = InternshipDto.builder()
                    .startDate(LocalDateTime.of(2025, 1, 1, 0, 0))
                    .endDate(LocalDateTime.of(2025, 3, 1, 0, 0))
                    .internIds(List.of())
                    .build();

            assertThatThrownBy(() -> validator.validateForCreation(dto))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("at least one intern");
        }

        @Test
        @DisplayName("throws when duration exceeds 90 days")
        void throwsWhenDurationExceeds90Days() {
            InternshipDto dto = InternshipDto.builder()
                    .startDate(LocalDateTime.of(2025, 1, 1, 0, 0))
                    .endDate(LocalDateTime.of(2025, 6, 1, 0, 0)) // ~151 days
                    .internIds(List.of(10L))
                    .build();

            assertThatThrownBy(() -> validator.validateForCreation(dto))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("longer than 3 months");
        }
    }

    // ── validateForUpdate ──────────────────────────────────────────────────────

    @Nested
    @DisplayName("validateForUpdate")
    class ValidateForUpdate {

        @Test
        @DisplayName("passes with valid data")
        void passesWhenValid() {
            assertThatCode(() -> validator.validateForUpdate(validDto())).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("throws when id is null")
        void throwsWhenIdNull() {
            InternshipDto dto = InternshipDto.builder()
                    .id(null)
                    .startDate(LocalDateTime.of(2025, 1, 1, 0, 0))
                    .endDate(LocalDateTime.of(2025, 3, 1, 0, 0))
                    .build();

            assertThatThrownBy(() -> validator.validateForUpdate(dto))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Internship ID");
        }

        @Test
        @DisplayName("throws when startDate is null")
        void throwsWhenStartDateNull() {
            InternshipDto dto = InternshipDto.builder()
                    .id(1L)
                    .startDate(null)
                    .endDate(LocalDateTime.of(2025, 3, 1, 0, 0))
                    .build();

            assertThatThrownBy(() -> validator.validateForUpdate(dto))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Start Date");
        }
    }

    // ── validatePartialUpdate ──────────────────────────────────────────────────

    @Nested
    @DisplayName("validatePartialUpdate")
    class ValidatePartialUpdate {

        @Test
        @DisplayName("passes when only one date is set (no validation triggered)")
        void passesWhenOnlyOneDateSet() {
            InternshipDto dto = InternshipDto.builder()
                    .startDate(LocalDateTime.of(2025, 1, 1, 0, 0))
                    .build();

            assertThatCode(() -> validator.validatePartialUpdate(dto)).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("passes when both dates are set and valid")
        void passesWhenBothDatesValid() {
            InternshipDto dto = InternshipDto.builder()
                    .startDate(LocalDateTime.of(2025, 1, 1, 0, 0))
                    .endDate(LocalDateTime.of(2025, 2, 1, 0, 0))
                    .build();

            assertThatCode(() -> validator.validatePartialUpdate(dto)).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("throws when both dates are set and duration exceeds 90 days")
        void throwsWhenDurationExceeds() {
            InternshipDto dto = InternshipDto.builder()
                    .startDate(LocalDateTime.of(2025, 1, 1, 0, 0))
                    .endDate(LocalDateTime.of(2025, 7, 1, 0, 0)) // ~181 days
                    .build();

            assertThatThrownBy(() -> validator.validatePartialUpdate(dto))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("longer than 3 months");
        }

        @Test
        @DisplayName("passes when both dates are null")
        void passesWhenBothDatesNull() {
            InternshipDto dto = InternshipDto.builder().build();

            assertThatCode(() -> validator.validatePartialUpdate(dto)).doesNotThrowAnyException();
        }
    }
}
