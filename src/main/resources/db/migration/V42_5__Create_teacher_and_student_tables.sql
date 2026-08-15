create table if not exists teacher
(
    id              varchar not null
        constraint teacher_pk primary key
        constraint teacher_user_fk references "user" (id),
    employee_number varchar not null
        constraint teacher_employee_number_unique unique
);

create table if not exists student
(
    id               varchar not null
        constraint student_pk primary key
        constraint student_user_fk references "user" (id),
    student_number   varchar not null
        constraint student_student_number_unique unique,
    promotion_id     varchar not null
        constraint student_promotion_fk references promotion (id),
    current_group_id varchar
        constraint student_current_group_fk references "group" (id)
);
