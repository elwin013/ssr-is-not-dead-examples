package net.codeer.todoapp.common;

import io.javalin.config.RoutesConfig;
import io.javalin.http.Context;
import net.codeer.todoapp.Todo;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

public final class TodoController {
    private final Function<Context, TodoDAO> todoDAOProvider;
    private final TenantTodoEventHub tenantTodoEventHub;

    public TodoController(Function<Context, TodoDAO> todoDAOProvider, TenantTodoEventHub tenantTodoEventHub) {
        this.todoDAOProvider = todoDAOProvider;
        this.tenantTodoEventHub = tenantTodoEventHub;
    }

    public void registerRoutes(RoutesConfig routes) {
        routes.get("/", this::listTodos);
        routes.post("/todos", this::createTodo);
        routes.post("/todos/{id}/done", this::markAsDone);
        routes.post("/todos/{id}/undone", this::markAsNotDone);
        routes.post("/todos/{id}/delete", this::deleteTodo);
    }

    private void listTodos(Context ctx) {
        renderTodos(ctx, "", "");
    }

    private void createTodo(Context ctx) {
        String content = Optional.ofNullable(ctx.formParam("content")).orElse("").trim();
        if (content.isBlank()) {
            renderValidationError(ctx, content, "Enter a todo before adding it.");
            return;
        }
        if (content.length() > 1_000) {
            renderValidationError(ctx, content, "A todo can contain at most 1,000 characters.");
            return;
        }

        Todo todo = todoDAOProvider.apply(ctx).addTodo(content, CustomAccessManager.getUserInfo(ctx));
        publishTodosChanged(ctx);
        if (isHtmxRequest(ctx)) {
            ctx.render("components/todoCreateResponse.jte", Map.of("todo", todo));
            return;
        }

        ctx.redirect("/");
    }

    private void deleteTodo(Context ctx) {
        Todo deletedTodo = todoDAOProvider.apply(ctx).deleteTodo(ctx.pathParam("id"));
        if (deletedTodo != null) publishTodosChanged(ctx);
        renderMutationOrRedirect(ctx, deletedTodo, null);
    }

    private void markAsDone(Context ctx) {
        Todo completedTodo = todoDAOProvider.apply(ctx).markAsDone(ctx.pathParam("id"));
        if (completedTodo != null) publishTodosChanged(ctx);
        renderMutationOrRedirect(ctx, completedTodo, completedTodo);
    }

    private void markAsNotDone(Context ctx) {
        Todo activeTodo = todoDAOProvider.apply(ctx).markAsNotDone(ctx.pathParam("id"));
        if (activeTodo != null) publishTodosChanged(ctx);
        renderMutationOrRedirect(ctx, activeTodo, activeTodo);
    }

    private void renderMutationOrRedirect(Context ctx, Todo changedTodo, Todo todoToMove) {
        if (!isHtmxRequest(ctx)) {
            ctx.redirect("/");
            return;
        }
        if (changedTodo == null) {
            ctx.status(404);
            return;
        }

        List<Todo> todos = todoDAOProvider.apply(ctx).getTodos();
        Map<String, Object> model = new HashMap<>();
        model.put("todoToMove", todoToMove);
        model.put("activeEmpty", todos.stream().noneMatch(todo -> !todo.done()));
        model.put("completedEmpty", todos.stream().noneMatch(Todo::done));
        ctx.render("components/todoActionResponse.jte", model);
    }

    private void renderValidationError(Context ctx, String content, String validationError) {
        if (isHtmxRequest(ctx)) {
            ctx.render("components/todoFormOob.jte", Map.of(
                    "content", content,
                    "validationError", validationError));
            return;
        }
        renderTodos(ctx, content, validationError);
    }

    private void renderTodos(Context ctx, String content, String validationError) {
        List<Todo> todos = todoDAOProvider.apply(ctx).getTodos();
        ctx.render("todos.jte", Map.of(
                "completed", todos.stream().filter(Todo::done).toList(),
                "todos", todos.stream().filter(todo -> !todo.done()).toList(),
                "user", CustomAccessManager.getUserInfo(ctx),
                "content", content,
                "validationError", validationError));
    }

    private void publishTodosChanged(Context ctx) {
        UserInfo user = CustomAccessManager.getUserInfo(ctx);
        tenantTodoEventHub.publish(user.tenantId(), user.email(), todoDAOProvider.apply(ctx).getTodos());
    }

    private static boolean isHtmxRequest(Context ctx) {
        return "true".equals(ctx.header("HX-Request"));
    }
}
