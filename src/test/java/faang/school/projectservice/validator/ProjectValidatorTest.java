package faang.school.projectservice.validator;

import faang.school.projectservice.dto.project.CreateSubProjectDto;
import faang.school.projectservice.model.ProjectVisibility;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for {@link ProjectValidator}.
 */
@DisplayName("ProjectValidator")
class ProjectValidatorTest {

    private CreateSubProjectDto validDto() {
        return CreateSubProjectDto.builder()
                .name("My Sub Project")
                .parentProjectId(1L)
                .visibility(ProjectVisibility.PUBLIC)
                .build();
    }

    // ── validateCreateSubProject ───────────────────────────────────────────────

    @Nested
    @DisplayName("validateCreateSubProject")
    class ValidateCreateSubProject {

        @Test
        @DisplayName("passes with valid data")
        void passesWhenValid() {
            assertThatCode(() -> ProjectValidator.validateCreateSubProject(validDto())).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("throws when name is null")
        void throwsWhenNameNull() {
            CreateSubProjectDto dto = CreateSubProjectDto.builder()
                    .name(null)
                    .parentProjectId(1L)
                    .visibility(ProjectVisibility.PUBLIC)
                    .build();

            assertThatThrownBy(() -> ProjectValidator.validateCreateSubProject(dto))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("throws when name is blank")
        void throwsWhenNameBlank() {
            CreateSubProjectDto dto = CreateSubProjectDto.builder()
                    .name("   ")
                    .parentProjectId(1L)
                    .visibility(ProjectVisibility.PUBLIC)
                    .build();

            assertThatThrownBy(() -> ProjectValidator.validateCreateSubProject(dto))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("throws when parentProjectId is null")
        void throwsWhenParentIdNull() {
            CreateSubProjectDto dto = CreateSubProjectDto.builder()
                    .name("Valid Name")
                    .parentProjectId(null)
                    .visibility(ProjectVisibility.PUBLIC)
                    .build();

            assertThatThrownBy(() -> ProjectValidator.validateCreateSubProject(dto))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    // ── validateUpdateProject ──────────────────────────────────────────────────

    @Nested
    @DisplayName("validateUpdateProject")
    class ValidateUpdateProject {

        @Test
        @DisplayName("passes with valid name")
        void passesWhenValid() {
            CreateSubProjectDto dto = CreateSubProjectDto.builder()
                    .name("Updated Name")
                    .build();
            assertThatCode(() -> ProjectValidator.validateUpdateProject(dto)).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("throws when name is null")
        void throwsWhenNameNull() {
            CreateSubProjectDto dto = CreateSubProjectDto.builder()
                    .name(null)
                    .build();
            assertThatThrownBy(() -> ProjectValidator.validateUpdateProject(dto))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("throws when name is blank")
        void throwsWhenNameBlank() {
            CreateSubProjectDto dto = CreateSubProjectDto.builder()
                    .name("  ")
                    .build();
            assertThatThrownBy(() -> ProjectValidator.validateUpdateProject(dto))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }
}
