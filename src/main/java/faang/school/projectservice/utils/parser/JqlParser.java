package faang.school.projectservice.utils.parser;

import faang.school.projectservice.dto.jira.issue.IssueFilterDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class JqlParser {
    public String buildJql(IssueFilterDto filters) {
        if (filters == null) {
            log.debug("Filters object is null, returning empty JQL.");
            return "";
        }

        List<String> jqlParts = new ArrayList<>();

        if (filters.getTypeIdPattern() != null) {
            jqlParts.add("issueType = " + quote(filters.getTypeIdPattern().toString()));
        }

        if (StringUtils.hasText(filters.getSummaryPattern())) {
            jqlParts.add("summary ~ " + quote(filters.getSummaryPattern()));
        }

        if (StringUtils.hasText(filters.getDescriptionPattern())) {
            jqlParts.add("description ~ " + quote(filters.getDescriptionPattern()));
        }

        if (StringUtils.hasText(filters.getAssigneeNamePattern())) {
            jqlParts.add("assignee = " + quote(filters.getAssigneeNamePattern()));
        }

        if (StringUtils.hasText(filters.getReporterNamePattern())) {
            jqlParts.add("reporter = " + quote(filters.getReporterNamePattern()));
        }

        String jql = String.join(" AND ", jqlParts);
        log.debug("Generated JQL: {}", jql);
        return jql;
    }

    private String quote(String value) {
        return "\"" + value.replace("\\", "\\\\").replace("\"", "\\\"") + "\"";
    }
}
