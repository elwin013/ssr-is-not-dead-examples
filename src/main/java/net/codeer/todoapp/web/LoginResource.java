package net.codeer.todoapp.web;

import io.quarkus.qute.CheckedTemplate;
import io.quarkus.qute.TemplateInstance;
import io.smallrye.common.annotation.Blocking;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.CookieParam;
import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Cookie;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.NewCookie;
import jakarta.ws.rs.core.Response;
import net.codeer.todoapp.common.InMemoryLoginDAO;
import net.codeer.todoapp.common.LoginDAO;

import java.net.URI;
import java.util.List;

@Path("/")
@Produces(MediaType.TEXT_HTML)
@Blocking
public class LoginResource {
    private static final LoginDAO LOGIN_DAO = new InMemoryLoginDAO(List.of(
            new InMemoryLoginDAO.User("kamil@signaturky.com", "asdf1234", 1, "Signaturky"),
            new InMemoryLoginDAO.User("paulina@codeer.net", "asdf1234", 2, "Codeer"),
            new InMemoryLoginDAO.User("lusia@codeer.net", "asdf1234", 2, "Codeer")
    ));

    @Inject
    UserSessions sessions;

    @GET
    @Path("login")
    public TemplateInstance showLogin() {
        return Templates.login("");
    }

    @POST
    @Path("login")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response login(@FormParam("email") String email, @FormParam("password") String password) {
        String normalizedEmail = email == null ? "" : email.trim();
        String normalizedPassword = password == null ? "" : password;
        if (normalizedEmail.isBlank() || normalizedPassword.isBlank()) {
            return templateResponse(Response.Status.BAD_REQUEST, Templates.login("Email and password are required."));
        }
        return LOGIN_DAO.authenticate(normalizedEmail, normalizedPassword)
                .map(user -> Response.seeOther(URI.create("/"))
                        .cookie(sessionCookie(sessions.create(user)))
                        .build())
                .orElseGet(() -> templateResponse(Response.Status.UNAUTHORIZED,
                        Templates.login("Invalid email or password.")));
    }

    @POST
    @Path("logout")
    public Response logout(@CookieParam(UserSessions.COOKIE_NAME) Cookie sessionCookie) {
        if (sessionCookie != null) {
            sessions.remove(sessionCookie.getValue());
        }
        return Response.seeOther(URI.create("/login"))
                .cookie(expiredSessionCookie())
                .build();
    }

    private static Response templateResponse(Response.Status status, TemplateInstance template) {
        return Response.status(status).type(MediaType.TEXT_HTML).entity(template).build();
    }

    private static NewCookie sessionCookie(String sessionId) {
        return new NewCookie.Builder(UserSessions.COOKIE_NAME)
                .value(sessionId)
                .path("/")
                .httpOnly(true)
                .sameSite(NewCookie.SameSite.LAX)
                .build();
    }

    private static NewCookie expiredSessionCookie() {
        return new NewCookie.Builder(UserSessions.COOKIE_NAME)
                .value("")
                .path("/")
                .maxAge(0)
                .build();
    }

    @CheckedTemplate(basePath = "LoginResource")
    static class Templates {
        static native TemplateInstance login(String error);
    }
}
