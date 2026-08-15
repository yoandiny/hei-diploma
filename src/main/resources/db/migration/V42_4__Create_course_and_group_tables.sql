create table if not exists course
(
    id      varchar not null
        constraint course_pk primary key,
    ref     varchar not null,
    title   varchar not null,
    credits integer not null
);

create table if not exists "group"
(
    id           varchar not null
        constraint group_pk primary key,
    ref          varchar not null,
    promotion_id varchar not null
        constraint group_promotion_fk references promotion (id)
);
