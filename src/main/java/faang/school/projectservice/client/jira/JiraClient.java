package faang.school.projectservice.client.jira;

import com.atlassian.jira.rest.client.api.IssueRestClient;
import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.api.SearchRestClient;
import com.atlassian.jira.rest.client.internal.async.AsynchronousJiraRestClientFactory;
import faang.school.projectservice.config.jira.JiraProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class JiraClient {
    private final JiraProperties jiraProperties;

    public JiraRestClient getJiraRestClient() {
        return new AsynchronousJiraRestClientFactory()
                .createWithBasicHttpAuthentication(jiraProperties.validatedBaseUri(),
                        jiraProperties.getUsername(), jiraProperties.getPassword());
    }

    public IssueRestClient getIssueClient() {
        return getJiraRestClient().getIssueClient();
    }

    public SearchRestClient getSearchClient() {
        return getJiraRestClient().getSearchClient();
    }
}
