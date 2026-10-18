@param String content
@param String validationError
<form action="/todos" method="post"
      hx-post="/todos"
      hx-swap="none">
    <div class="field has-addons">
        <div class="control is-expanded">
            <input autofocus class="input${validationError.isBlank() ? "" : " is-danger"}" name="content" maxlength="1000" placeholder="New todo" value="${content}">
        </div>
        <div class="control">
            <button class="button is-primary">Add</button>
        </div>
    </div>
    @if(!validationError.isBlank())
        <p class="help is-danger">${validationError}</p>
    @endif
</form>
