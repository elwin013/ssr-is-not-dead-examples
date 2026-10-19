package net.codeer.todoapp.web;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import net.codeer.todoapp.common.TodoDAO;
import net.codeer.todoapp.psql.PsqlTodoDAO;

import javax.sql.DataSource;

@ApplicationScoped
public class TodoDaoFactory {
    @Inject
    DataSource dataSource;

    public TodoDAO forTenant(long tenantId) {
        return new PsqlTodoDAO(dataSource, tenantId);
    }
}
