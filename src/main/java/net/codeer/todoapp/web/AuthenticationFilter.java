package net.codeer.todoapp.web;

import jakarta.annotation.Priority;
import jakarta.inject.Inject;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;
import net.codeer.todoapp.common.UserInfo;

import java.net.URI;

@Provider
@Priority(Priorities.AUTHENTICATION)
public class AuthenticationFilter implements ContainerRequestFilter {
    public static final String USER_PROPERTY = AuthenticationFilter.class.getName() + ".user";

    @Inject
    UserSessions sessions;

    @Override
    public void filter(ContainerRequestContext requestContext) {
        String path = requestContext.getUriInfo().getPath();
        if ("login".equals(path) || "/login".equals(path) || "logout".equals(path) || "/logout".equals(path)) {
            return;
        }

        UserInfo user = sessions.find(requestContext.getCookies().get(UserSessions.COOKIE_NAME) == null
                ? null
                : requestContext.getCookies().get(UserSessions.COOKIE_NAME).getValue());
        if (user == null) {
            requestContext.abortWith(Response.seeOther(URI.create("/login")).build());
            return;
        }
        requestContext.setProperty(USER_PROPERTY, user);
    }

    public static UserInfo user(ContainerRequestContext requestContext) {
        return (UserInfo) requestContext.getProperty(USER_PROPERTY);
    }
}
