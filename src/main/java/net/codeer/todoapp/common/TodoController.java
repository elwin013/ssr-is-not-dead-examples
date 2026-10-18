package net.codeer.todoapp.common;

import io.javalin.config.RoutesConfig;
import io.javalin.http.Context;
import net.codeer.todoapp.Todo;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

public final class TodoController {
    private final Function<Context, TodoDAO> todoDAOProvider;

    public TodoController(Function<Context, TodoDAO> todoDAOProvider) {
        this.todoDAOProvider = todoDAOProvider;
    }

    public void registerRoutes(RoutesConfig routes) {
        routes.get("/", this::listTodos);
        routes.post("/todos", this::createTodo);
        routes.post("/todos/{id}/done", this::markAsDone);
        routes.post("/todos/{id}/undone", this::markAsNotDone);
        routes.post("/todos/{id}/delete", this::deleteTodo);
    }

    private void listTodos(Context ctx) {
        List<Todo> todos = todoDAOProvider.apply(ctx).getTodos();
        ctx.render("todos.jte", Map.of(
                "completed", todos.stream().filter(Todo::done).toList(),
                "todos", todos.stream().filter(todo -> !todo.done()).toList(),
                "user", CustomAccessManager.getUserInfo(ctx)));
    }

    private void createTodo(Context ctx) {
        String content = Optional.ofNullable(ctx.formParam("content")).orElse("").trim();
        if (!content.isBlank() && content.length() <= 1_000) {
            todoDAOProvider.apply(ctx).addTodo(content, CustomAccessManager.getUserInfo(ctx));
        }
        ctx.redirect("/");
    }

    private void deleteTodo(Context ctx) {
        todoDAOProvider.apply(ctx).deleteTodo(ctx.pathParam("id"));
        ctx.redirect("/");
    }

    private void markAsDone(Context ctx) {
        todoDAOProvider.apply(ctx).markAsDone(ctx.pathParam("id"));
        ctx.redirect("/");
    }

    private void markAsNotDone(Context ctx) {
        todoDAOProvider.apply(ctx).markAsNotDone(ctx.pathParam("id"));
        ctx.redirect("/");
    }
}
