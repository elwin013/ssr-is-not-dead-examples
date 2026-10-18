@param String error
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>TodoApp</title>
    <link rel="stylesheet" href="/bulma.css">
</head>
<body>
<main class="container is-max-tablet mt-6 px-4">
    <h1 class="title">TodoApp</h1>
    <p class="subtitle">SSR + PostgreSQL schema-per-tenant</p>
    <div class="columns is-centered mt-5">
        <div class="column is-two-thirds">
            @if(!error.isBlank())
                <article class="message is-danger">
                    <div class="message-body">${error}</div>
                </article>
            @endif
            <form action="/login" method="post" class="box">
                <div class="field">
                    <label class="label" for="email">E-mail</label>
                    <div class="control"><input class="input" autofocus id="email" name="email" type="email" required>
                    </div>
                </div>
                <div class="field">
                    <label class="label" for="password">Password</label>
                    <div class="control"><input class="input" id="password" name="password" type="password" required>
                    </div>
                </div>
                <button class="button is-primary" type="submit">Sign in</button>
            </form>
        </div>
    </div>
</main>
</body>
</html>
