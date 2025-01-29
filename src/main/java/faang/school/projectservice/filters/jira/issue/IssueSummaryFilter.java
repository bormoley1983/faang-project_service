package faang.school.projectservice.filters.jira.issue;

import faang.school.projectservice.dto.jira.issue.IssueFilterDto;
import org.springframework.stereotype.Component;

@Component
public class IssueSummaryFilter extends IssueFilter {
    @Override
    public Object getFilterFieldValue(IssueFilterDto filters) {
        return filters.getSummaryPattern();
    }

    @Override
    public String getJql(IssueFilterDto filters) {
        String summaryPattern = filters.getSummaryPattern();
        if (summaryPattern.contains(" ")) {
            summaryPattern = "\"" + summaryPattern + "\"";
        }
        return String.format("summary ~ '%s'", summaryPattern);
    }
}
