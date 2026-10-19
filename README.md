# Todoapp – SSR in Javalin/Spring/Quarkus + PSQL Schema-Per-Tenant

Small Java SSR examples built around the same Todo application.

## Branches

- `1-javalin-plain-ssr` — Classic Javalin SSR with jte and PostgreSQL schema-per-tenant isolation.
- `2-javalin-adding-without-reload` — Javalin, jte and htmx form submission that adds Todos without a page reload.
- `3-javalin-deleting-and-marking-without-reload` — htmx updates for completing, restoring and deleting Todos.
- `4-javalin-validation` — Server-side Todo validation returned as an HTML form fragment.
- `5-javalin-sse` — Tenant-scoped realtime Todo updates over SSE with rendered HTML fragments.
- `6-spring-thymeleaf` — Spring Boot MVC, Thymeleaf and htmx implementation of the Todo application.
- `7-quarkus-qute` — Quarkus REST, Qute checked templates and htmx implementation of the Todo application.
