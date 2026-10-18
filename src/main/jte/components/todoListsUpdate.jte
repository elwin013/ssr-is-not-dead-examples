@import net.codeer.todoapp.Todo
@import java.util.List
@param List<Todo> todos
@param List<Todo> completed
<div id="active-todo-list">
    @if(todos.isEmpty())
        <p id="empty-active-todo-list" class="has-text-grey">No todos yet.</p>
    @endif
    @for(Todo todo : todos)
        @template.components.todoItem(todo = todo, completed = false)
    @endfor
</div>
<div id="completed-todo-list" hx-swap-oob="outerHTML">
    @if(completed.isEmpty())
        <p id="empty-completed-todo-list" class="has-text-grey">Nothing completed yet.</p>
    @endif
    @for(Todo todo : completed)
        @template.components.todoItem(todo = todo, completed = true)
    @endfor
</div>
