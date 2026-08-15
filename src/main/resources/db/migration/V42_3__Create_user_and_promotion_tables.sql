create table if not exists "user"
(
    id         varchar                 not null
        constraint user_pk primary key,
    email      varchar                 not null
        constraint user_email_unique unique,
    password   varchar                 not null,
    first_name varchar                 not null,
    last_name  varchar                 not null,
    role       varchar                 not null,
    enabled    boolean                 not null,
    created_at timestamp with time zone not null
);

create table if not exists promotion
(
    id         varchar not null
        constraint promotion_pk primary key,
    label      varchar not null,
    start_year integer not null,
    end_year   integer not null
);
