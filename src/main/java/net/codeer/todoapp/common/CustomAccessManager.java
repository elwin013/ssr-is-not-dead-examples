package net.codeer.todoapp.common;

import io.javalin.http.Context;

import java.util.Set;

public final class CustomAccessManager {
    public static final String SESSION_USER = "user";
    private static final Set<String> PUBLIC_PATHS = Set.of("/login", "/logout");

    private CustomAccessManager() {
    }

    public static void beforeMatched(Context ctx) {
        if (isPublic(ctx)) return;
        UserInfo user = getUserInfo(ctx);
        if (user == null || user.email().isBlank()) {
            ctx.redirect("/login");
            ctx.skipRemainingHandlers();
        }
    }

    public static UserInfo getUserInfo(Context ctx) {
        return ctx.sessionAttribute(SESSION_USER);
    }

    private static boolean isPublic(Context ctx) {
        String path = ctx.path();
        return PUBLIC_PATHS.contains(path)
                || path.endsWith(".css")
                || path.endsWith(".js")
                || path.endsWith(".png")
                || path.endsWith(".jpg")
                || path.endsWith(".svg")
                || path.endsWith(".ico")
                || path.endsWith(".woff")
                || path.endsWith(".woff2");
    }
}
