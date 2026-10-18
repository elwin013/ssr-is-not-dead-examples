@import net.codeer.todoapp.Todo
@param Todo todoToMove
@param boolean activeEmpty
@param boolean completedEmpty

@if(todoToMove != null)
    @if(todoToMove.done())
        <div hx-swap-oob="afterbegin:#completed-todo-list">
            @template.components.todoItem(todo = todoToMove, completed = true)
        </div>
    @else
        <div hx-swap-oob="afterbegin:#active-todo-list">
            @template.components.todoItem(todo = todoToMove, completed = false)
        </div>
    @endif
@endif

@if(activeEmpty)
    <p id="empty-active-todo-list" class="has-text-grey" hx-swap-oob="afterbegin:#active-todo-list">No todos yet.</p>
@else
    <span id="empty-active-todo-list" hx-swap-oob="delete"></span>
@endif

@if(completedEmpty)
    <p id="empty-completed-todo-list" class="has-text-grey" hx-swap-oob="afterbegin:#completed-todo-list">Nothing completed yet.</p>
@else
    <span id="empty-completed-todo-list" hx-swap-oob="delete"></span>
@endif
