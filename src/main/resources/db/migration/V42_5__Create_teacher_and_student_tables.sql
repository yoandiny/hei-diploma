create table if not exists j_teacher
(
    id              varchar(255) not null
        constraint j_teacher_pk primary key
        constraint j_teacher_user_fk references j_user (id),
    employee_number varchar(255) not null
        constraint j_teacher_employee_number_unique unique
);

create table if not exists j_student
(
    id               varchar(255) not null
        constraint j_student_pk primary key
        constraint j_student_user_fk references j_user (id),
    student_number   varchar(255) not null
        constraint j_student_student_number_unique unique,
    promotion_id     varchar(255) not null
        constraint j_student_promotion_fk references j_promotion (id),
    current_group_id varchar(255)
        constraint j_student_current_group_fk references class_group (id)
);
