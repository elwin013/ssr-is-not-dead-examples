@import net.codeer.todoapp.Todo
@import net.codeer.todoapp.common.UserInfo
@import java.util.List
@param List<Todo> todos
@param List<Todo> completed
@param UserInfo user
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>TodoApp</title>
    <link rel="stylesheet" href="/bulma.css">
</head>
<body>
<main class="container is-max-tablet mt-5 px-4">
    <nav class="level is-mobile">
        <div class="level-left">
            <div>
                <h1 class="title mb-1">TodoApp</h1>
                <p class="subtitle is-6">Tenant: ${user.tenantName()}</p>
            </div>
        </div>
        <div class="level-right">
            <div class="has-text-right">
                <p class="is-size-7 has-text-grey">${user.email()}</p>
                <form action="/logout" method="post">
                    <button class="button is-light is-small mt-1">Sign out</button>
                </form>
            </div>
        </div>
    </nav>

    <form action="/todos" method="post" class="mb-6">
        <div class="field has-addons">
            <div class="control is-expanded"><input autofocus class="input" name="content" maxlength="1000" placeholder="New todo" required></div>
            <div class="control"><button class="button is-primary">Add</button></div>
        </div>
    </form>

    <section>
        <h2 class="subtitle">To do</h2>
        @if(todos.isEmpty())
            <p class="has-text-grey">No todos yet.</p>
        @endif
        @for(Todo todo : todos)
            <article class="card mb-3"><div class="card-content py-4"><div class="is-flex is-align-items-center is-justify-content-space-between" style="gap: 1rem">
                <div><p>${todo.content()}</p><p class="is-size-7 has-text-grey">created by: ${todo.createdBy()}</p></div>
                <form action="/todos/${todo.id()}/done" method="post"><button class="button is-primary is-small">Done</button></form>
            </div></div></article>
        @endfor
    </section>

    <section class="mt-6">
        <h2 class="subtitle">Completed</h2>
        @if(completed.isEmpty())
            <p class="has-text-grey">Nothing completed yet.</p>
        @endif
        @for(Todo todo : completed)
            <article class="card mb-3"><div class="card-content py-4"><div class="is-flex is-align-items-center is-justify-content-space-between" style="gap: 1rem">
                <div><p><del>${todo.content()}</del></p><p class="is-size-7 has-text-grey">created by: ${todo.createdBy()}</p></div>
                <div class="buttons are-small"><form action="/todos/${todo.id()}/undone" method="post"><button class="button">Undo</button></form><form action="/todos/${todo.id()}/delete" method="post"><button class="button is-danger is-light">Delete</button></form></div>
            </div></div></article>
        @endfor
    </section>
</main>
</body>
</html>
