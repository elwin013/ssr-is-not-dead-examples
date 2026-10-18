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
    <script src="/htmx.js"></script>
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

    @template.components.todoForm()
    @template.components.todoList(todos = todos, completed = false)
    @template.components.todoList(todos = completed, completed = true)
</main>
</body>
</html>
