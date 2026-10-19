package net.codeer.todoapp.web;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

record CreateTodoForm(
        @NotBlank(message = "Enter a todo before adding it.")
        @Size(max = 1_000, message = "A todo can contain at most 1,000 characters.")
        String content
) {
}
