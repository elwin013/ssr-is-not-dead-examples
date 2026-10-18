package net.codeer.todoapp.common;

import net.codeer.todoapp.Todo;

import java.util.List;

public interface TodoDAO {
    Todo addTodo(String content, UserInfo user);

    List<Todo> getTodos();

    Todo deleteTodo(String id);

    Todo markAsDone(String id);

    Todo markAsNotDone(String id);
}
