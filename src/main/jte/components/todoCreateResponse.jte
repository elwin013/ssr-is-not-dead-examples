@import net.codeer.todoapp.Todo
@param Todo todo
<div hx-swap-oob="afterbegin:#active-todo-list">
    @template.components.todoItem(todo = todo, completed = false)
</div>
<span id="empty-active-todo-list" hx-swap-oob="delete"></span>
@template.components.todoFormOob(content = "", validationError = "")
