@import net.codeer.todoapp.Todo
@import java.util.List
@param List<Todo> todos
@param boolean completed
<section class="mt-6">
    @if(completed)
        <h2 class="subtitle">Completed</h2>
    @else
        <h2 class="subtitle">To do</h2>
    @endif

    @if(completed)
        @if(todos.isEmpty())
            <p class="has-text-grey">Nothing completed yet.</p>
        @endif
        @for(Todo todo : todos)
            @template.components.todoItem(todo = todo, completed = true)
        @endfor
    @else
        <div id="todo-list">
            @if(todos.isEmpty())
                <p id="empty-todo-list" class="has-text-grey">No todos yet.</p>
            @endif
            @for(Todo todo : todos)
                @template.components.todoItem(todo = todo, completed = false)
            @endfor
        </div>
    @endif
</section>
