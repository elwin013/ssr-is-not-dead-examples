@import net.codeer.todoapp.Todo
@param Todo todo
@param boolean completed
<article class="card mb-3">
    <div class="card-content py-4">
        <div class="is-flex is-align-items-center is-justify-content-space-between" style="gap: 1rem">
            <div>
                @if(completed)
                    <p><del>${todo.content()}</del></p>
                @else
                    <p>${todo.content()}</p>
                @endif
                <p class="is-size-7 has-text-grey">created by: ${todo.createdBy()}</p>
            </div>
            @if(completed)
                <div class="buttons are-small">
                    <form action="/todos/${todo.id()}/undone" method="post">
                        <button class="button">Undo</button>
                    </form>
                    <form action="/todos/${todo.id()}/delete" method="post">
                        <button class="button is-danger is-light">Delete</button>
                    </form>
                </div>
            @else
                <form action="/todos/${todo.id()}/done" method="post">
                    <button class="button is-primary is-small">Done</button>
                </form>
            @endif
        </div>
    </div>
</article>
