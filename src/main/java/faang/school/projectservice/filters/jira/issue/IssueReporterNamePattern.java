package faang.school.projectservice.filters.jira.issue;

import faang.school.projectservice.dto.jira.issue.IssueFilterDto;
import org.springframework.stereotype.Component;

@Component
public class IssueReporterNamePattern extends IssueFilter {
    @Override
    public Object getFilterFieldValue(IssueFilterDto filters) {
        return filters.getReporterNamePattern();
    }

    @Override
    public String getJql(IssueFilterDto filters) {
        String reporterPattern = filters.getReporterNamePattern();
        if (reporterPattern.contains(" ")) {
            reporterPattern = "\"" + reporterPattern + "\"";
        }
        return String.format("reporter = '%s'", reporterPattern);
    }
}
