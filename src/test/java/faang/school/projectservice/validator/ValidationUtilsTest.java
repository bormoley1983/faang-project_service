package faang.school.projectservice.validator;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for {@link ValidationUtils}.
 */
@DisplayName("ValidationUtils")
class ValidationUtilsTest {

    // ── validateNotBlank ───────────────────────────────────────────────────────

    @Test
    @DisplayName("validateNotBlank passes when value is non-blank")
    void validateNotBlankPasses() {
        assertThatCode(() -> ValidationUtils.validateNotBlank("hello", "Name")).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("validateNotBlank throws when value is null")
    void validateNotBlankThrowsOnNull() {
        assertThatThrownBy(() -> ValidationUtils.validateNotBlank(null, "Name"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Name cannot be blank");
    }

    @Test
    @DisplayName("validateNotBlank throws when value is empty")
    void validateNotBlankThrowsOnEmpty() {
        assertThatThrownBy(() -> ValidationUtils.validateNotBlank("", "Title"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Title cannot be blank");
    }

    @Test
    @DisplayName("validateNotBlank throws when value is whitespace")
    void validateNotBlankThrowsOnWhitespace() {
        assertThatThrownBy(() -> ValidationUtils.validateNotBlank("   ", "Description"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Description cannot be blank");
    }

    // ── validateNotNull ────────────────────────────────────────────────────────

    @Test
    @DisplayName("validateNotNull passes when value is not null")
    void validateNotNullPasses() {
        assertThatCode(() -> ValidationUtils.validateNotNull(42L, "Id")).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("validateNotNull throws when value is null")
    void validateNotNullThrowsOnNull() {
        assertThatThrownBy(() -> ValidationUtils.validateNotNull(null, "Project"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Project is required");
    }
}
