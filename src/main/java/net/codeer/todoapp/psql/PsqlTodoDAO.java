package net.codeer.todoapp.psql;

import net.codeer.todoapp.Todo;
import net.codeer.todoapp.common.TodoDAO;
import net.codeer.todoapp.common.UserInfo;
import org.flywaydb.core.Flyway;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Record;
import org.jooq.SQLDialect;
import org.jooq.Table;

import javax.sql.DataSource;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.name;
import static org.jooq.impl.DSL.table;
import static org.jooq.impl.DSL.using;

public final class PsqlTodoDAO implements TodoDAO {
    private static final ConcurrentHashMap<String, Boolean> MIGRATED_SCHEMAS = new ConcurrentHashMap<>();
    private static final Field<String> ID = field(name("id"), String.class);
    private static final Field<String> CONTENT = field(name("content"), String.class);
    private static final Field<Boolean> DONE = field(name("done"), Boolean.class);
    private static final Field<LocalDateTime> CREATED = field(name("created"), LocalDateTime.class);
    private static final Field<String> CREATED_BY = field(name("created_by"), String.class);

    private final DSLContext dsl;
    private final Table<?> todos;

    public PsqlTodoDAO(DataSource dataSource, long tenantId) {
        String schema = schemaName(tenantId);
        migrate(dataSource, schema);
        this.dsl = using(dataSource, SQLDialect.POSTGRES);
        this.todos = table(name(schema, "todos"));
    }

    @Override
    public Todo addTodo(String content, UserInfo user) {
        return dsl.insertInto(todos)
                .set(ID, UUID.randomUUID().toString())
                .set(CONTENT, content)
                .set(DONE, false)
                .set(CREATED, LocalDateTime.now(ZoneOffset.UTC))
                .set(CREATED_BY, user.email())
                .returning(ID, CONTENT, DONE, CREATED, CREATED_BY)
                .fetchOne(PsqlTodoDAO::toTodo);
    }

    @Override
    public List<Todo> getTodos() {
        return dsl.select(ID, CONTENT, DONE, CREATED, CREATED_BY)
                .from(todos)
                .orderBy(CREATED.desc())
                .fetch(row -> new Todo(
                        row.get(ID), row.get(CONTENT), Boolean.TRUE.equals(row.get(DONE)),
                        row.get(CREATED), row.get(CREATED_BY)));
    }

    @Override
    public Todo deleteTodo(String id) {
        return dsl.deleteFrom(todos)
                .where(ID.eq(id))
                .returning(ID, CONTENT, DONE, CREATED, CREATED_BY)
                .fetchOne(PsqlTodoDAO::toTodo);
    }

    @Override
    public Todo markAsDone(String id) {
        return dsl.update(todos)
                .set(DONE, true)
                .where(ID.eq(id))
                .returning(ID, CONTENT, DONE, CREATED, CREATED_BY)
                .fetchOne(PsqlTodoDAO::toTodo);
    }

    @Override
    public Todo markAsNotDone(String id) {
        return dsl.update(todos)
                .set(DONE, false)
                .where(ID.eq(id))
                .returning(ID, CONTENT, DONE, CREATED, CREATED_BY)
                .fetchOne(PsqlTodoDAO::toTodo);
    }

    private static Todo toTodo(Record row) {
        return new Todo(
                row.get(ID), row.get(CONTENT), Boolean.TRUE.equals(row.get(DONE)),
                row.get(CREATED), row.get(CREATED_BY));
    }

    private static String schemaName(long tenantId) {
        if (tenantId <= 0) throw new IllegalArgumentException("Tenant id must be positive");
        return "tenant_" + tenantId;
    }

    private static void migrate(DataSource dataSource, String schema) {
        MIGRATED_SCHEMAS.computeIfAbsent(schema, ignored -> {
            Flyway.configure()
                    .dataSource(dataSource)
                    .schemas(schema)
                    .defaultSchema(schema)
                    .createSchemas(true)
                    .locations("classpath:db/migration")
                    .load()
                    .migrate();
            return true;
        });
    }
}
