package net.codeer.todoapp.web;

import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.subscription.MultiEmitter;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.sse.OutboundSseEvent;
import jakarta.ws.rs.sse.Sse;
import net.codeer.todoapp.Todo;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@ApplicationScoped
public class TodoEventHub {
    private final Map<Long, Set<Subscriber>> subscribersByTenant = new ConcurrentHashMap<>();

    public Multi<OutboundSseEvent> connect(long tenantId, String email, Sse sse) {
        return Multi.createFrom().emitter(emitter -> {
            Subscriber subscriber = new Subscriber(email, sse, emitter);
            Set<Subscriber> subscribers = subscribersByTenant.computeIfAbsent(tenantId,
                    ignored -> ConcurrentHashMap.newKeySet());
            subscribers.add(subscriber);
            emitter.onTermination(() -> subscribers.remove(subscriber));
            emitter.emit(event(sse, "connected", ""));
        });
    }

    public void publish(long tenantId, String originatingEmail, List<Todo> todos) {
        Set<Subscriber> subscribers = subscribersByTenant.getOrDefault(tenantId, Set.of());
        if (subscribers.isEmpty()) {
            return;
        }

        String html = TodoResource.Components.todoListsUpdate(
                todos.stream().filter(todo -> !todo.done()).toList(),
                todos.stream().filter(Todo::done).toList()).render();
        for (Subscriber subscriber : subscribers) {
            if (!subscriber.email().equals(originatingEmail) && !subscriber.emitter().isCancelled()) {
                subscriber.emitter().emit(event(subscriber.sse(), null, html));
            }
        }
    }

    private static OutboundSseEvent event(Sse sse, String name, String data) {
        OutboundSseEvent.Builder builder = sse.newEventBuilder().data(String.class, data);
        if (name != null) {
            builder.name(name);
        }
        return builder.build();
    }

    private record Subscriber(String email, Sse sse, MultiEmitter<? super OutboundSseEvent> emitter) {
    }
}
