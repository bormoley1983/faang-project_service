package faang.school.projectservice.filters.jira.issue;

import faang.school.projectservice.dto.jira.issue.IssueFilterDto;
import org.springframework.stereotype.Component;

@Component
public class IssueTypeIdFilter extends IssueFilter {

    @Override
    public Object getFilterFieldValue(IssueFilterDto filters) {
        return filters.getTypeIdPattern();
    }

    @Override
    public String getJql(IssueFilterDto filters) {
        String typeIdPattern = filters.getTypeIdPattern().toString();
        if (typeIdPattern.contains(" ")) {
            typeIdPattern = "\"" + typeIdPattern + "\"";
        }
        return String.format("issueType = '%s'", typeIdPattern);
    }
}
