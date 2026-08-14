create table if not exists j_course
(
    id      varchar(255) not null
        constraint j_course_pk primary key,
    ref     varchar(255) not null,
    title   varchar(255) not null,
    credits integer      not null
);

create table if not exists class_group
(
    id           varchar(255) not null
        constraint class_group_pk primary key,
    ref          varchar(255) not null,
    promotion_id varchar(255) not null
        constraint class_group_promotion_fk references j_promotion (id)
);
