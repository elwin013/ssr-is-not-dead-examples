create table todos (
    id varchar(36) primary key,
    content text not null,
    done boolean not null default false,
    created timestamp not null default now(),
    created_by varchar(255) not null
);

create index idx_todos_created on todos (created desc);
