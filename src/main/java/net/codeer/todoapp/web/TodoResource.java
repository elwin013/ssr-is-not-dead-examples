package net.codeer.todoapp.web;

import io.quarkus.qute.CheckedTemplate;
import io.quarkus.qute.TemplateInstance;
import io.smallrye.common.annotation.Blocking;
import jakarta.inject.Inject;
import jakarta.ws.rs.FormParam;
import io.smallrye.mutiny.Multi;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.sse.OutboundSseEvent;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.sse.Sse;
import net.codeer.todoapp.Todo;
import net.codeer.todoapp.common.TodoDAO;
import net.codeer.todoapp.common.UserInfo;

import java.net.URI;
import java.util.List;

@Path("/")
@Produces(MediaType.TEXT_HTML)
@Blocking
public class TodoResource {

    @Inject
    TodoDaoFactory todoDaoFactory;

    @Inject
    TodoEventHub todoEvents;


    @jakarta.ws.rs.core.Context
    ContainerRequestContext requestContext;

    @GET
    public TemplateInstance listTodos() {
        return renderTodos("", "");
    }


    @POST
    @Path("todos")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Object createTodo(@FormParam("content") String submittedContent,
                             @HeaderParam("HX-Request") String htmxRequest) {
        String content = submittedContent == null ? "" : submittedContent.trim();
        if (content.isBlank()) {
            return validationError(content, "Enter a todo before adding it.", isHtmx(htmxRequest));
        }
        if (content.length() > 1_000) {
            return validationError(content, "A todo can contain at most 1,000 characters.", isHtmx(htmxRequest));
        }

        UserInfo user = user();
        Todo todo = dao(user).addTodo(content, user);
        publishTodosChanged(user);
        if (!isHtmx(htmxRequest)) {
            return Response.seeOther(URI.create("/")).build();
        }
        return Components.todoCreateResponse(todo);
    }

    @POST
    @Path("todos/{id}/done")
    public Object markAsDone(@PathParam("id") String id, @HeaderParam("HX-Request") String htmxRequest) {
        return mutationResponse(dao(user()).markAsDone(id), isHtmx(htmxRequest));
    }

    @POST
    @Path("todos/{id}/undone")
    public Object markAsNotDone(@PathParam("id") String id, @HeaderParam("HX-Request") String htmxRequest) {
        return mutationResponse(dao(user()).markAsNotDone(id), isHtmx(htmxRequest));
    }

    @POST
    @Path("todos/{id}/delete")
    public Object deleteTodo(@PathParam("id") String id, @HeaderParam("HX-Request") String htmxRequest) {
        return mutationResponse(dao(user()).deleteTodo(id), isHtmx(htmxRequest));
    }

    @GET
    @Path("todos/events")
    @Produces(MediaType.SERVER_SENT_EVENTS)
    public Multi<OutboundSseEvent> events(@jakarta.ws.rs.core.Context Sse sse) {
        UserInfo user = user();
        return todoEvents.connect(user.tenantId(), user.email(), sse);
    }

    private Object mutationResponse(Todo changedTodo, boolean htmx) {
        UserInfo user = user();
        if (changedTodo != null) {
            publishTodosChanged(user);
        }
        if (!htmx) {
            return Response.seeOther(URI.create("/")).build();
        }
        if (changedTodo == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        List<Todo> todos = dao(user).getTodos();
        return Components.todoActionResponse(changedTodo,
                todos.stream().noneMatch(todo -> !todo.done()),
                todos.stream().noneMatch(Todo::done));
    }

    private Object validationError(String content, String error, boolean htmx) {
        return htmx ? Components.todoFormOob(content, error) : renderTodos(content, error);
    }

    private TemplateInstance renderTodos(String content, String validationError) {
        UserInfo user = user();
        List<Todo> todos = dao(user).getTodos();
        return Templates.todos(
                todos.stream().filter(todo -> !todo.done()).toList(),
                todos.stream().filter(Todo::done).toList(),
                user,
                content,
                validationError);
    }

    private void publishTodosChanged(UserInfo user) {
        todoEvents.publish(user.tenantId(), user.email(), dao(user).getTodos());
    }

    private TodoDAO dao(UserInfo user) {
        return todoDaoFactory.forTenant(user.tenantId());
    }

    private UserInfo user() {
        return AuthenticationFilter.user(requestContext);
    }

    private static boolean isHtmx(String htmxRequest) {
        return "true".equals(htmxRequest);
    }


    @CheckedTemplate(basePath = "TodoResource")
    static class Templates {

        static native TemplateInstance todos(List<Todo> todos, List<Todo> completed, UserInfo user,
                                             String content, String validationError);
    }

    @CheckedTemplate(basePath = "TodoResource/components")
    static class Components {
        static native TemplateInstance todoFormOob(String content, String validationError);

        static native TemplateInstance todoCreateResponse(Todo todo);

        static native TemplateInstance todoActionResponse(Todo todoToMove, boolean activeEmpty,
                                                           boolean completedEmpty);

        static native TemplateInstance todoListsUpdate(List<Todo> todos, List<Todo> completed);
    }
}
