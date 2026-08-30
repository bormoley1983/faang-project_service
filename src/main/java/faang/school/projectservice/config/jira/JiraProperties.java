package faang.school.projectservice.config.jira;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.util.Set;

@Component
@ConfigurationProperties(prefix = "services.jira")
public class JiraProperties {
    private String baseUrl;
    private String username;
    private String password;
    private Set<String> allowedHosts = Set.of();

    public URI validatedBaseUri() {
        if (baseUrl == null || username == null || password == null) {
            throw new IllegalStateException("Jira integration is not configured");
        }
        URI uri = URI.create(baseUrl);
        if (!"https".equalsIgnoreCase(uri.getScheme()) || uri.getHost() == null
                || !allowedHosts.contains(uri.getHost().toLowerCase())) {
            throw new IllegalStateException("Jira base URL must be HTTPS and use an administratively allowed host");
        }
        return uri;
    }

    public String getBaseUrl() { return baseUrl; }
    public void setBaseUrl(String baseUrl) { this.baseUrl = baseUrl; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public Set<String> getAllowedHosts() { return allowedHosts; }
    public void setAllowedHosts(Set<String> allowedHosts) {
        this.allowedHosts = allowedHosts == null ? Set.of() : allowedHosts.stream()
                .map(String::toLowerCase).collect(java.util.stream.Collectors.toUnmodifiableSet());
    }
}
