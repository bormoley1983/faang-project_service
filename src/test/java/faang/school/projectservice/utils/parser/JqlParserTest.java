package faang.school.projectservice.utils.parser;

import faang.school.projectservice.dto.jira.issue.IssueFilterDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link JqlParser}.
 */
@DisplayName("JqlParser")
class JqlParserTest {

    private final JqlParser parser = new JqlParser();

    @Test
    @DisplayName("returns empty string when filters are null")
    void returnsEmptyWhenNull() {
        assertThat(parser.buildJql(null)).isEmpty();
    }

    @Test
    @DisplayName("returns empty string when all filter fields are null")
    void returnsEmptyWhenAllFieldsNull() {
        IssueFilterDto dto = new IssueFilterDto();

        assertThat(parser.buildJql(dto)).isEmpty();
    }

    @Test
    @DisplayName("builds issueType clause")
    void buildsIssueTypeClause() {
        IssueFilterDto dto = new IssueFilterDto();
        dto.setTypeIdPattern(1L);

        assertThat(parser.buildJql(dto)).isEqualTo("issueType = \"1\"");
    }

    @Test
    @DisplayName("builds summary clause")
    void buildsSummaryClause() {
        IssueFilterDto dto = new IssueFilterDto();
        dto.setSummaryPattern("bug fix");

        assertThat(parser.buildJql(dto)).isEqualTo("summary ~ \"bug fix\"");
    }

    @Test
    @DisplayName("builds description clause")
    void buildsDescriptionClause() {
        IssueFilterDto dto = new IssueFilterDto();
        dto.setDescriptionPattern("regression");

        assertThat(parser.buildJql(dto)).isEqualTo("description ~ \"regression\"");
    }

    @Test
    @DisplayName("builds assignee clause")
    void buildsAssigneeClause() {
        IssueFilterDto dto = new IssueFilterDto();
        dto.setAssigneeNamePattern("john.doe");

        assertThat(parser.buildJql(dto)).isEqualTo("assignee = \"john.doe\"");
    }

    @Test
    @DisplayName("builds reporter clause")
    void buildsReporterClause() {
        IssueFilterDto dto = new IssueFilterDto();
        dto.setReporterNamePattern("jane.smith");

        assertThat(parser.buildJql(dto)).isEqualTo("reporter = \"jane.smith\"");
    }

    @Test
    @DisplayName("joins multiple clauses with AND")
    void joinsMultipleClausesWithAnd() {
        IssueFilterDto dto = new IssueFilterDto();
        dto.setTypeIdPattern(2L);
        dto.setSummaryPattern("login");
        dto.setAssigneeNamePattern("alice");

        assertThat(parser.buildJql(dto))
                .isEqualTo("issueType = \"2\" AND summary ~ \"login\" AND assignee = \"alice\"");
    }

    @Test
    @DisplayName("escapes backslashes in values")
    void escapesBackslashes() {
        IssueFilterDto dto = new IssueFilterDto();
        dto.setSummaryPattern("path\\to\\file");

        assertThat(parser.buildJql(dto)).isEqualTo("summary ~ \"path\\\\to\\\\file\"");
    }

    @Test
    @DisplayName("escapes double quotes in values")
    void escapesDoubleQuotes() {
        IssueFilterDto dto = new IssueFilterDto();
        dto.setSummaryPattern("say \"hello\"");

        assertThat(parser.buildJql(dto)).isEqualTo("summary ~ \"say \\\"hello\\\"\"");
    }

    @Test
    @DisplayName("builds all five clauses together")
    void buildsAllClauses() {
        IssueFilterDto dto = new IssueFilterDto();
        dto.setTypeIdPattern(3L);
        dto.setSummaryPattern("perf");
        dto.setDescriptionPattern("slow query");
        dto.setAssigneeNamePattern("bob");
        dto.setReporterNamePattern("carol");

        String jql = parser.buildJql(dto);

        assertThat(jql)
                .contains("issueType = \"3\"")
                .contains("summary ~ \"perf\"")
                .contains("description ~ \"slow query\"")
                .contains("assignee = \"bob\"")
                .contains("reporter = \"carol\"");
    }
}
