package net.codeer.todoapp.common;

import io.javalin.config.RoutesConfig;
import io.javalin.http.Context;

import java.util.Map;
import java.util.Optional;

public final class LoginController {
    private final LoginDAO loginDAO;

    public LoginController(LoginDAO loginDAO) {
        this.loginDAO = loginDAO;
    }

    public void registerRoutes(RoutesConfig routes) {
        routes.get("/login", this::showLogin);
        routes.post("/login", this::login);
        routes.post("/logout", this::logout);
    }

    private void showLogin(Context ctx) {
        ctx.render("login.jte", Map.of("error", ""));
    }

    private void login(Context ctx) {
        String email = Optional.ofNullable(ctx.formParam("email")).orElse("").trim();
        String password = Optional.ofNullable(ctx.formParam("password")).orElse("");
        if (email.isBlank() || password.isBlank()) {
            ctx.status(400).render("login.jte", Map.of("error", "Email and password are required."));
            return;
        }
        loginDAO.authenticate(email, password).ifPresentOrElse(user -> {
            ctx.sessionAttribute(CustomAccessManager.SESSION_USER, user);
            ctx.redirect("/");
        }, () -> ctx.status(401).render("login.jte", Map.of("error", "Invalid email or password.")));
    }

    private void logout(Context ctx) {
        ctx.req().getSession().invalidate();
        ctx.redirect("/login");
    }
}
