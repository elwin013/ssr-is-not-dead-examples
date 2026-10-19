package net.codeer.todoapp.web;

import jakarta.enterprise.context.ApplicationScoped;
import net.codeer.todoapp.common.UserInfo;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@ApplicationScoped
public class UserSessions {
    public static final String COOKIE_NAME = "todo-session";

    private final Map<String, UserInfo> usersBySessionId = new ConcurrentHashMap<>();

    public String create(UserInfo user) {
        String sessionId = UUID.randomUUID().toString();
        usersBySessionId.put(sessionId, user);
        return sessionId;
    }

    public UserInfo find(String sessionId) {
        return sessionId == null ? null : usersBySessionId.get(sessionId);
    }

    public void remove(String sessionId) {
        if (sessionId != null) {
            usersBySessionId.remove(sessionId);
        }
    }
}
