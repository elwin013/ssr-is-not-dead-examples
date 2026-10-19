package net.codeer.todoapp.web;

import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import net.codeer.todoapp.Todo;
import net.codeer.todoapp.common.UserInfo;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.web.servlet.JakartaServletWebApplication;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Controller
final class TodoEventsController {
    private final SpringTemplateEngine templateEngine;
    private final JakartaServletWebApplication webApplication;
    private final Map<Long, Set<Subscriber>> subscribersByTenant = new ConcurrentHashMap<>();

    TodoEventsController(SpringTemplateEngine templateEngine, ServletContext servletContext) {
        this.templateEngine = templateEngine;
        this.webApplication = JakartaServletWebApplication.buildApplication(servletContext);
    }

    @GetMapping(value = "/todos/events", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    SseEmitter events(HttpServletRequest request) {
        return connect(WebConfiguration.user(request));
    }

    void publish(UserInfo originatingUser, List<Todo> active, List<Todo> completed,
                 HttpServletRequest request, HttpServletResponse response) {
        WebContext context = new WebContext(webApplication.buildExchange(request, response));
        context.setVariable("todos", active);
        context.setVariable("completed", completed);
        context.setVariable("oob", true);
        String html = templateEngine.process("components/todo-lists-update", context);

        for (Subscriber subscriber : subscribersByTenant.getOrDefault(originatingUser.tenantId(), Set.of())) {
            if (subscriber.email().equals(originatingUser.email())) continue;
            try {
                subscriber.emitter().send(SseEmitter.event().data(html));
            } catch (IOException exception) {
                subscriber.emitter().completeWithError(exception);
            }
        }
    }

    private SseEmitter connect(UserInfo user) {
        SseEmitter emitter = new SseEmitter(0L);
        Subscriber subscriber = new Subscriber(user.email(), emitter);
        Set<Subscriber> subscribers = subscribersByTenant.computeIfAbsent(user.tenantId(), ignored -> ConcurrentHashMap.newKeySet());
        subscribers.add(subscriber);

        Runnable remove = () -> subscribers.remove(subscriber);
        emitter.onCompletion(remove);
        emitter.onTimeout(remove);
        try {
            emitter.send(SseEmitter.event().name("connected").data(""));
        } catch (IOException exception) {
            remove.run();
            emitter.completeWithError(exception);
        }
        return emitter;
    }

    private record Subscriber(String email, SseEmitter emitter) {
    }
}
