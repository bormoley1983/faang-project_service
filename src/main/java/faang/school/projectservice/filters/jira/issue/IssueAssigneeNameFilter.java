package faang.school.projectservice.filters.jira.issue;

import faang.school.projectservice.dto.jira.issue.IssueFilterDto;
import org.springframework.stereotype.Component;

@Component
public class IssueAssigneeNameFilter extends IssueFilter {
    @Override
    public Object getFilterFieldValue(IssueFilterDto filters) {
        return filters.getAssigneeNamePattern();
    }

    @Override
    public String getJql(IssueFilterDto filters) {
        String assigneeNamePattern = filters.getAssigneeNamePattern();
        if (assigneeNamePattern.contains(" ")) {
            assigneeNamePattern = "\"" + assigneeNamePattern + "\"";
        }
        return String.format("assignee = '%s'", assigneeNamePattern);
    }
}
