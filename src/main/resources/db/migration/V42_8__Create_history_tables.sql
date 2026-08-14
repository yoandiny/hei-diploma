create table if not exists j_student_group_history
(
    id         varchar(255)             not null
        constraint j_student_group_history_pk primary key,
    student_id varchar(255)             not null
        constraint j_student_group_history_student_fk references j_student (id),
    group_id   varchar(255)             not null
        constraint j_student_group_history_group_fk references class_group (id),
    start_date timestamp with time zone not null,
    end_date   timestamp with time zone
);

create table if not exists j_grade_history
(
    id             varchar(255)             not null
        constraint j_grade_history_pk primary key,
    grade_id       varchar(255)             not null
        constraint j_grade_history_grade_fk references j_grade (id),
    previous_value numeric(5, 2),
    new_value      numeric(5, 2)            not null,
    reason         varchar(255)             not null,
    changed_by     varchar(255)             not null
        constraint j_grade_history_changed_by_fk references j_user (id),
    changed_at     timestamp with time zone not null
);
