package faang.school.projectservice.config.context.user;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class UserHeaderFilter implements Filter {

    private final UserContext userContext;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        HttpServletRequest req = (HttpServletRequest) request;
        String userId = req.getHeader("x-user-id");
        if (userId != null) {
            try {
                long parsedUserId = Long.parseLong(userId);
                if (parsedUserId <= 0) {
                    throw new NumberFormatException("non-positive user id");
                }
                userContext.setUserId(parsedUserId);
            } catch (NumberFormatException exception) {
                ((HttpServletResponse) response).sendError(HttpServletResponse.SC_BAD_REQUEST,
                        "x-user-id must be a positive integer");
                return;
            }
        }
        try {
            chain.doFilter(request, response);
        } finally {
            userContext.clear();
        }
    }
}
