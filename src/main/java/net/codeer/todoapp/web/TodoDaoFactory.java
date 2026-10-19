package net.codeer.todoapp.web;

import net.codeer.todoapp.common.TodoDAO;
import net.codeer.todoapp.psql.PsqlTodoDAO;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;

@Component
final class TodoDaoFactory {
    private final DataSource dataSource;

    TodoDaoFactory(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    TodoDAO forTenant(long tenantId) {
        return new PsqlTodoDAO(dataSource, tenantId);
    }
}
