create table if not exists j_user
(
    id         varchar(255)             not null
        constraint j_user_pk primary key,
    email      varchar(255)             not null
        constraint j_user_email_unique unique,
    password   varchar(255)             not null,
    first_name varchar(255)             not null,
    last_name  varchar(255)             not null,
    role       varchar(255)             not null,
    enabled    boolean                  not null,
    created_at timestamp with time zone not null
);

create table if not exists j_promotion
(
    id         varchar(255) not null
        constraint j_promotion_pk primary key,
    label      varchar(255) not null,
    start_year integer      not null,
    end_year   integer      not null
);
