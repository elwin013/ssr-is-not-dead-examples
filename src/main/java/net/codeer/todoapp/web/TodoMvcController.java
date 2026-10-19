package net.codeer.todoapp.web;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import net.codeer.todoapp.Todo;
import net.codeer.todoapp.common.TodoDAO;
import net.codeer.todoapp.common.UserInfo;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import jakarta.validation.Valid;
import org.springframework.validation.BindingResult;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.FragmentsRendering;

import java.util.List;
import java.util.Map;

@Controller
public final class TodoMvcController {
    private final TodoDaoFactory todoDaoFactory;
    private final TodoEventsController todoEvents;
    public TodoMvcController(TodoDaoFactory todoDaoFactory, TodoEventsController todoEvents) {
        this.todoDaoFactory = todoDaoFactory;
        this.todoEvents = todoEvents;
    }

    @GetMapping("/")
    String listTodos(HttpServletRequest request, Model model) {
        addPageModel(model, dao(request), WebConfiguration.user(request), "", "");
        return "todos";
    }

    @PostMapping("/todos")
    Object createTodo(@Valid @ModelAttribute CreateTodoForm form, BindingResult bindingResult,
                      HttpServletRequest request, HttpServletResponse response, Model model) {
        String content = form.content() == null ? "" : form.content().trim();
        if (bindingResult.hasErrors()) {
            String error = bindingResult.getFieldError("content").getDefaultMessage();
            if (isHtmx(request)) {
                return FragmentsRendering.fragment("components/todo-form-oob :: form", Map.of(
                        "content", content, "validationError", error)).build();
            }
            addPageModel(model, dao(request), WebConfiguration.user(request), content, error);
            return "todos";
        }

        Todo todo = dao(request).addTodo(content, WebConfiguration.user(request));
        publishTodosChanged(request, response);
        if (!isHtmx(request)) return "redirect:/";
        return FragmentsRendering.fragment("components/todo-create-response :: response", Map.of("todo", todo)).build();
    }

    @PostMapping("/todos/{id}/done")
    Object markDone(@PathVariable String id, HttpServletRequest request, HttpServletResponse response) {
        return mutationResponse(request, response, dao(request).markAsDone(id));
    }

    @PostMapping("/todos/{id}/undone")
    Object markUndone(@PathVariable String id, HttpServletRequest request, HttpServletResponse response) {
        return mutationResponse(request, response, dao(request).markAsNotDone(id));
    }

    @PostMapping("/todos/{id}/delete")
    Object deleteTodo(@PathVariable String id, HttpServletRequest request, HttpServletResponse response) {
        return mutationResponse(request, response, dao(request).deleteTodo(id));
    }


    private FragmentsRendering renderLists(HttpServletRequest request) {
        TodoLists lists = lists(dao(request));
        return FragmentsRendering.fragments(List.of(
                new ModelAndView("components/todo-list-active :: list", Map.of("todos", lists.active())),
                new ModelAndView("components/todo-list-completed :: list", Map.of(
                        "completed", lists.completed(), "oob", true)))).build();
    }

    private Object mutationResponse(HttpServletRequest request, HttpServletResponse response, Todo changedTodo) {
        if (changedTodo != null) {
            publishTodosChanged(request, response);
        }
        if (!isHtmx(request)) {
            return "redirect:/";
        }
        if (changedTodo == null) {
            return ResponseEntity.notFound().build();
        }
        return renderLists(request);
    }

    private void publishTodosChanged(HttpServletRequest request, HttpServletResponse response) {
        UserInfo user = WebConfiguration.user(request);
        TodoLists lists = lists(dao(request));
        todoEvents.publish(user, lists.active(), lists.completed(), request, response);
    }

    private void addPageModel(Model model, TodoDAO todoDAO, UserInfo user, String content, String validationError) {
        TodoLists lists = lists(todoDAO);
        model.addAttribute("todos", lists.active());
        model.addAttribute("completed", lists.completed());
        model.addAttribute("user", user);
        model.addAttribute("content", content);
        model.addAttribute("validationError", validationError);
        model.addAttribute("oob", false);
    }

    private TodoDAO dao(HttpServletRequest request) {
        return todoDaoFactory.forTenant(WebConfiguration.user(request).tenantId());
    }

    private static TodoLists lists(TodoDAO todoDAO) {
        List<Todo> all = todoDAO.getTodos();
        return new TodoLists(all.stream().filter(todo -> !todo.done()).toList(),
                all.stream().filter(Todo::done).toList());
    }

    private static boolean isHtmx(HttpServletRequest request) {
        return "true".equals(request.getHeader("HX-Request"));
    }

    private record TodoLists(List<Todo> active, List<Todo> completed) {
    }
}
