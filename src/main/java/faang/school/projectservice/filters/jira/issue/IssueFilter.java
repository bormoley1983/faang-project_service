package faang.school.projectservice.filters.jira.issue;

import faang.school.projectservice.dto.jira.issue.IssueFilterDto;

public abstract class IssueFilter {
    public boolean isApplicable(IssueFilterDto filters) {
        return filters != null && getFilterFieldValue(filters) != null;
    }

    public abstract Object getFilterFieldValue(IssueFilterDto filters);

    public abstract String getJql(IssueFilterDto filters);
}
