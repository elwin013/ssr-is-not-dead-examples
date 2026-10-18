package net.codeer.todoapp.common;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

public final class InMemoryLoginDAO implements LoginDAO {
    private final Map<String, User> users;

    public InMemoryLoginDAO(List<User> initialUsers) {
        this.users = initialUsers.stream().collect(Collectors.toUnmodifiableMap(User::email, user -> user));
    }

    @Override
    public Optional<UserInfo> authenticate(String email, String password) {
        if (email == null || password == null) return Optional.empty();
        User user = users.get(email);
        if (user == null || !Objects.equals(user.password(), password)) return Optional.empty();
        return Optional.of(new UserInfo(user.email(), user.tenantId(), user.tenantName()));
    }

    public record User(String email, String password, long tenantId, String tenantName) {
    }
}
