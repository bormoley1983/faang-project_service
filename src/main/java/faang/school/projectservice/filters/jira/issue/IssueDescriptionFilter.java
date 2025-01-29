package faang.school.projectservice.filters.jira.issue;

import faang.school.projectservice.dto.jira.issue.IssueFilterDto;
import org.springframework.stereotype.Component;

@Component
public class IssueDescriptionFilter extends IssueFilter {
    @Override
    public Object getFilterFieldValue(IssueFilterDto filters) {
        return filters.getDescriptionPattern();
    }

    @Override
    public String getJql(IssueFilterDto filters) {
        String descriptionPattern = filters.getDescriptionPattern();
        if (descriptionPattern.contains(" ")) {
            descriptionPattern = "\"" + descriptionPattern + "\"";
        }
        return String.format("description = '%s'", descriptionPattern);
    }
}
