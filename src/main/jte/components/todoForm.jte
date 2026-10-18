<form action="/todos" method="post" class="mb-6"
      hx-post="/todos"
      hx-target="#active-todo-list"
      hx-swap="afterbegin"
      hx-on::after-request="if(event.detail.successful) { this.reset(); document.getElementById('empty-active-todo-list')?.remove() }">
    <div class="field has-addons">
        <div class="control is-expanded">
            <input autofocus class="input" name="content" maxlength="1000" placeholder="New todo" required>
        </div>
        <div class="control">
            <button class="button is-primary">Add</button>
        </div>
    </div>
</form>
