package net.codeer.todoapp;

import java.time.LocalDateTime;

public record Todo(String id, String content, boolean done, LocalDateTime created, String createdBy) {
}
