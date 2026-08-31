package faang.school.projectservice.filter.moment;

import faang.school.projectservice.dto.moment.MomentFilterDto;
import faang.school.projectservice.model.Moment;
import faang.school.projectservice.model.Project;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for all moment filters.
 */
@DisplayName("Moment Filters")
class MomentFilterTest {

    private Moment buildMoment(String name, String description, LocalDateTime date, List<Project> projects) {
        return Moment.builder()
                .name(name)
                .description(description)
                .date(date)
                .projects(projects)
                .build();
    }

    // ── MomentAfterDateFilter ──────────────────────────────────────────────────

    @Nested
    @DisplayName("MomentAfterDateFilter")
    class AfterDateFilter {

        private final MomentAfterDateFilter filter = new MomentAfterDateFilter();

        @Test
        @DisplayName("isApplicable returns true when afterDatePattern is set")
        void applicableWhenSet() {
            MomentFilterDto dto = MomentFilterDto.builder().afterDatePattern(LocalDateTime.of(2025, 1, 1, 0, 0)).build();
            assertThat(filter.isApplicable(dto)).isTrue();
        }

        @Test
        @DisplayName("isApplicable returns false when afterDatePattern is null")
        void notApplicableWhenNull() {
            MomentFilterDto dto = MomentFilterDto.builder().build();
            assertThat(filter.isApplicable(dto)).isFalse();
        }

        @Test
        @DisplayName("apply returns true when moment date is after filter date")
        void applyTrueWhenAfter() {
            Moment moment = buildMoment("m", "d", LocalDateTime.of(2025, 6, 1, 0, 0), null);
            MomentFilterDto dto = MomentFilterDto.builder().afterDatePattern(LocalDateTime.of(2025, 1, 1, 0, 0)).build();
            assertThat(filter.apply(moment, dto)).isTrue();
        }

        @Test
        @DisplayName("apply returns false when moment date is before filter date")
        void applyFalseWhenBefore() {
            Moment moment = buildMoment("m", "d", LocalDateTime.of(2024, 12, 31, 0, 0), null);
            MomentFilterDto dto = MomentFilterDto.builder().afterDatePattern(LocalDateTime.of(2025, 1, 1, 0, 0)).build();
            assertThat(filter.apply(moment, dto)).isFalse();
        }
    }

    // ── MomentBeforeDatePattern ────────────────────────────────────────────────

    @Nested
    @DisplayName("MomentBeforeDatePattern")
    class BeforeDateFilter {

        private final MomentBeforeDatePattern filter = new MomentBeforeDatePattern();

        @Test
        @DisplayName("isApplicable returns true when beforeDatePattern is set")
        void applicableWhenSet() {
            MomentFilterDto dto = MomentFilterDto.builder().beforeDatePattern(LocalDateTime.of(2025, 1, 1, 0, 0)).build();
            assertThat(filter.isApplicable(dto)).isTrue();
        }

        @Test
        @DisplayName("isApplicable returns false when beforeDatePattern is null")
        void notApplicableWhenNull() {
            MomentFilterDto dto = MomentFilterDto.builder().build();
            assertThat(filter.isApplicable(dto)).isFalse();
        }

        @Test
        @DisplayName("apply returns true when moment date is before filter date")
        void applyTrueWhenBefore() {
            Moment moment = buildMoment("m", "d", LocalDateTime.of(2024, 12, 31, 0, 0), null);
            MomentFilterDto dto = MomentFilterDto.builder().beforeDatePattern(LocalDateTime.of(2025, 1, 1, 0, 0)).build();
            assertThat(filter.apply(moment, dto)).isTrue();
        }

        @Test
        @DisplayName("apply returns false when moment date is after filter date")
        void applyFalseWhenAfter() {
            Moment moment = buildMoment("m", "d", LocalDateTime.of(2025, 6, 1, 0, 0), null);
            MomentFilterDto dto = MomentFilterDto.builder().beforeDatePattern(LocalDateTime.of(2025, 1, 1, 0, 0)).build();
            assertThat(filter.apply(moment, dto)).isFalse();
        }
    }

    // ── MomentDescriptionFilter ────────────────────────────────────────────────

    @Nested
    @DisplayName("MomentDescriptionFilter")
    class DescriptionFilter {

        private final MomentDescriptionFilter filter = new MomentDescriptionFilter();

        @Test
        @DisplayName("isApplicable returns true when descriptionPattern is set")
        void applicableWhenSet() {
            MomentFilterDto dto = MomentFilterDto.builder().descriptionPattern("hello").build();
            assertThat(filter.isApplicable(dto)).isTrue();
        }

        @Test
        @DisplayName("isApplicable returns false when descriptionPattern is null")
        void notApplicableWhenNull() {
            MomentFilterDto dto = MomentFilterDto.builder().build();
            assertThat(filter.isApplicable(dto)).isFalse();
        }

        @Test
        @DisplayName("apply returns true when description contains pattern")
        void applyTrueWhenContains() {
            Moment moment = buildMoment("m", "Hello World", null, null);
            MomentFilterDto dto = MomentFilterDto.builder().descriptionPattern("Hello").build();
            assertThat(filter.apply(moment, dto)).isTrue();
        }

        @Test
        @DisplayName("apply returns false when description does not contain pattern")
        void applyFalseWhenNotContains() {
            Moment moment = buildMoment("m", "Goodbye World", null, null);
            MomentFilterDto dto = MomentFilterDto.builder().descriptionPattern("hello").build();
            assertThat(filter.apply(moment, dto)).isFalse();
        }
    }

    // ── MomentNameFilter ───────────────────────────────────────────────────────

    @Nested
    @DisplayName("MomentNameFilter")
    class NameFilter {

        private final MomentNameFilter filter = new MomentNameFilter();

        @Test
        @DisplayName("isApplicable returns true when namePattern is set")
        void applicableWhenSet() {
            MomentFilterDto dto = MomentFilterDto.builder().namePattern("test").build();
            assertThat(filter.isApplicable(dto)).isTrue();
        }

        @Test
        @DisplayName("isApplicable returns false when namePattern is null")
        void notApplicableWhenNull() {
            MomentFilterDto dto = MomentFilterDto.builder().build();
            assertThat(filter.isApplicable(dto)).isFalse();
        }

        @Test
        @DisplayName("apply returns true when name contains pattern")
        void applyTrueWhenContains() {
            Moment moment = buildMoment("My Test Moment", "d", null, null);
            MomentFilterDto dto = MomentFilterDto.builder().namePattern("Test").build();
            assertThat(filter.apply(moment, dto)).isTrue();
        }

        @Test
        @DisplayName("apply returns false when name does not contain pattern")
        void applyFalseWhenNotContains() {
            Moment moment = buildMoment("My Other Moment", "d", null, null);
            MomentFilterDto dto = MomentFilterDto.builder().namePattern("test").build();
            assertThat(filter.apply(moment, dto)).isFalse();
        }
    }

    // ── MomentProjectIdsFilter ─────────────────────────────────────────────────

    @Nested
    @DisplayName("MomentProjectIdsFilter")
    class ProjectIdsFilter {

        private final MomentProjectIdsFilter filter = new MomentProjectIdsFilter();

        @Test
        @DisplayName("isApplicable returns true when projectIdsPattern is set")
        void applicableWhenSet() {
            MomentFilterDto dto = MomentFilterDto.builder().projectIdsPattern(List.of(1L, 2L)).build();
            assertThat(filter.isApplicable(dto)).isTrue();
        }

        @Test
        @DisplayName("isApplicable returns false when projectIdsPattern is null")
        void notApplicableWhenNull() {
            MomentFilterDto dto = MomentFilterDto.builder().build();
            assertThat(filter.isApplicable(dto)).isFalse();
        }

        @Test
        @DisplayName("apply returns true when all filter project IDs are in moment projects")
        void applyTrueWhenAllContained() {
            Project p1 = new Project();
            p1.setId(1L);
            Project p2 = new Project();
            p2.setId(2L);
            Moment moment = buildMoment("m", "d", null, List.of(p1, p2));
            MomentFilterDto dto = MomentFilterDto.builder().projectIdsPattern(List.of(1L)).build();
            assertThat(filter.apply(moment, dto)).isTrue();
        }

        @Test
        @DisplayName("apply returns false when some filter project IDs are missing")
        void applyFalseWhenMissing() {
            Project p1 = new Project();
            p1.setId(1L);
            Moment moment = buildMoment("m", "d", null, List.of(p1));
            MomentFilterDto dto = MomentFilterDto.builder().projectIdsPattern(List.of(1L, 99L)).build();
            assertThat(filter.apply(moment, dto)).isFalse();
        }

        @Test
        @DisplayName("apply returns true when moment has no projects and filter is empty list")
        void applyTrueWhenNoProjectsAndEmptyFilter() {
            Moment moment = buildMoment("m", "d", null, null);
            MomentFilterDto dto = MomentFilterDto.builder().projectIdsPattern(List.of()).build();
            assertThat(filter.apply(moment, dto)).isTrue();
        }
    }
}
