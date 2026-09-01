package faang.school.projectservice.mapper;

import faang.school.projectservice.dto.project.ProjectDto;
import faang.school.projectservice.dto.vacancy.VacancyDto;
import faang.school.projectservice.model.Project;
import faang.school.projectservice.model.ProjectStatus;
import faang.school.projectservice.model.ProjectVisibility;
import faang.school.projectservice.model.TeamRole;
import faang.school.projectservice.model.Vacancy;
import faang.school.projectservice.model.VacancyStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link VacancyMapper}.
 *
 * <p>Focus: PRJ-22 — the vacancy DTO must expose a lightweight {@link ProjectDto}
 * projection instead of leaking the raw JPA {@link Project} entity.
 */
@DisplayName("VacancyMapper")
class VacancyMapperTest {

    private final VacancyMapper mapper = Mappers.getMapper(VacancyMapper.class);

    @Test
    @DisplayName("toDto maps vacancy fields and projects the nested project into a ProjectDto")
    void toDto_mapsProjectIntoProjectDto() {
        // Arrange
        Project project = Project.builder()
                .id(7L)
                .name("Apollo")
                .status(ProjectStatus.IN_PROGRESS)
                .visibility(ProjectVisibility.PUBLIC)
                .build();
        Vacancy vacancy = Vacancy.builder()
                .id(1L)
                .name("Backend Engineer")
                .description("Build APIs")
                .position(TeamRole.DEVELOPER)
                .project(project)
                .status(VacancyStatus.OPEN)
                .salary(5000.0)
                .count(3)
                .build();

        // Act
        VacancyDto dto = mapper.toDto(vacancy);

        // Assert
        assertThat(dto).isNotNull();
        assertThat(dto.getName()).isEqualTo("Backend Engineer");
        assertThat(dto.getDescription()).isEqualTo("Build APIs");
        assertThat(dto.getPosition()).isEqualTo(TeamRole.DEVELOPER);
        assertThat(dto.getStatus()).isEqualTo(VacancyStatus.OPEN);
        assertThat(dto.getSalary()).isEqualTo(5000.0);
        assertThat(dto.getCount()).isEqualTo(3);

        // PRJ-22: nested project must be a DTO projection, not the entity
        ProjectDto projectDto = dto.getProject();
        assertThat(projectDto).isNotNull();
        assertThat(projectDto.getId()).isEqualTo(7L);
        assertThat(projectDto.getName()).isEqualTo("Apollo");
        assertThat(projectDto.getStatus()).isEqualTo(ProjectStatus.IN_PROGRESS);
        assertThat(projectDto.getVisibility()).isEqualTo(ProjectVisibility.PUBLIC);
    }

    @Test
    @DisplayName("toDto handles a null project without failing")
    void toDto_nullProject_yieldsNullProjectDto() {
        // Arrange
        Vacancy vacancy = Vacancy.builder()
                .id(2L)
                .name("QA")
                .position(TeamRole.TESTER)
                .status(VacancyStatus.CLOSED)
                .project(null)
                .build();

        // Act
        VacancyDto dto = mapper.toDto(vacancy);

        // Assert
        assertThat(dto).isNotNull();
        assertThat(dto.getProject()).isNull();
    }

    @Test
    @DisplayName("toProjectDto maps a project entity to its DTO projection")
    void toProjectDto_mapsFields() {
        // Arrange
        Project project = Project.builder()
                .id(9L)
                .name("Borealis")
                .status(ProjectStatus.COMPLETED)
                .visibility(ProjectVisibility.PRIVATE)
                .build();

        // Act
        ProjectDto dto = mapper.toProjectDto(project);

        // Assert
        assertThat(dto).isNotNull();
        assertThat(dto.getId()).isEqualTo(9L);
        assertThat(dto.getName()).isEqualTo("Borealis");
        assertThat(dto.getStatus()).isEqualTo(ProjectStatus.COMPLETED);
        assertThat(dto.getVisibility()).isEqualTo(ProjectVisibility.PRIVATE);
    }

    @Test
    @DisplayName("toProjectDto handles a null project")
    void toProjectDto_null_returnsNull() {
        // Act
        ProjectDto dto = mapper.toProjectDto(null);

        // Assert
        assertThat(dto).isNull();
    }

    @Test
    @DisplayName("toDto maps a list of vacancies, projecting each nested project")
    void toDto_listProjectsEachNestedProject() {
        // Arrange
        Vacancy v1 = Vacancy.builder()
                .id(1L)
                .name("A")
                .position(TeamRole.DEVELOPER)
                .status(VacancyStatus.OPEN)
                .project(Project.builder().id(10L).name("P1").build())
                .build();
        Vacancy v2 = Vacancy.builder()
                .id(2L)
                .name("B")
                .position(TeamRole.DESIGNER)
                .status(VacancyStatus.CLOSED)
                .project(Project.builder().id(11L).name("P2").build())
                .build();

        // Act
        List<VacancyDto> dtos = mapper.toDto(List.of(v1, v2));

        // Assert
        assertThat(dtos).hasSize(2);
        assertThat(dtos.get(0).getProject().getId()).isEqualTo(10L);
        assertThat(dtos.get(0).getProject().getName()).isEqualTo("P1");
        assertThat(dtos.get(1).getProject().getId()).isEqualTo(11L);
        assertThat(dtos.get(1).getProject().getName()).isEqualTo("P2");
    }
}
