package net.codeer.todoapp.common;

import gg.jte.TemplateEngine;
import gg.jte.output.StringOutput;
import io.javalin.http.sse.SseClient;
import net.codeer.todoapp.Todo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Demo-only in-memory SSE hub. It works while the application runs as one JVM;
 * a multi-instance deployment needs a shared broker such as Redis or PostgreSQL LISTEN/NOTIFY.
 */
public final class TenantTodoEventHub {
    private static final Logger logger = LoggerFactory.getLogger(TenantTodoEventHub.class);
    private final TemplateEngine templateEngine;
    private final Map<Long, Set<Subscriber>> subscribersByTenant = new ConcurrentHashMap<>();

    public TenantTodoEventHub(TemplateEngine templateEngine) {
        this.templateEngine = templateEngine;
    }

    public void connect(SseClient client) {
        UserInfo user = CustomAccessManager.getUserInfo(client.ctx());
        if (user == null) {
            client.close();
            return;
        }

        Subscriber subscriber = new Subscriber(user.email(), client);
        Set<Subscriber> subscribers = subscribersByTenant.computeIfAbsent(
                user.tenantId(), ignored -> ConcurrentHashMap.newKeySet());
        subscribers.add(subscriber);
        client.keepAlive();
        client.onClose(() -> {
            subscribers.remove(subscriber);
            logger.info("SSE disconnected: {} in tenant {}", user.email(), user.tenantId());
        });
        client.sendEvent("connected", "");
        logger.info("SSE connected: {} in tenant {}", user.email(), user.tenantId());
    }

    public void publish(long tenantId, String originatingEmail, List<Todo> todos) {
        Set<Subscriber> subscribers = subscribersByTenant.getOrDefault(tenantId, Set.of());
        if (subscribers.isEmpty()) return;

        StringOutput output = new StringOutput();
        templateEngine.render("components/todoListsUpdate.jte", Map.of(
                "todos", todos.stream().filter(todo -> !todo.done()).toList(),
                "completed", todos.stream().filter(Todo::done).toList()), output);
        byte[] html = output.toString().getBytes(StandardCharsets.UTF_8);

        for (Subscriber subscriber : subscribers) {
            if (subscriber.email().equals(originatingEmail) || subscriber.client().terminated()) continue;
            subscriber.client().sendData(new ByteArrayInputStream(html));
        }
    }

    private record Subscriber(String email, SseClient client) {
    }
}
