package net.codeer.todoapp.common;

import java.util.Optional;

public interface LoginDAO {
    Optional<UserInfo> authenticate(String email, String password);
}
