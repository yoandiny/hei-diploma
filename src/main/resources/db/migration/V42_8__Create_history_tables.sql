create table if not exists student_group_history
(
    id         varchar                  not null
        constraint student_group_history_pk primary key,
    student_id varchar                  not null
        constraint student_group_history_student_fk references student (id),
    group_id   varchar                  not null
        constraint student_group_history_group_fk references "group" (id),
    start_date timestamp with time zone not null,
    end_date   timestamp with time zone
);

create table if not exists grade_history
(
    id             varchar                  not null
        constraint grade_history_pk primary key,
    grade_id       varchar                  not null
        constraint grade_history_grade_fk references grade (id),
    previous_value numeric(5, 2),
    new_value      numeric(5, 2)            not null,
    reason         varchar                  not null,
    changed_by     varchar                  not null
        constraint grade_history_changed_by_fk references "user" (id),
    changed_at     timestamp with time zone not null
);
