create table if not exists j_course_assignment
(
    id         varchar(255) not null
        constraint j_course_assignment_pk primary key,
    course_id  varchar(255) not null
        constraint j_course_assignment_course_fk references j_course (id),
    group_id   varchar(255) not null
        constraint j_course_assignment_group_fk references class_group (id),
    teacher_id varchar(255) not null
        constraint j_course_assignment_teacher_fk references j_teacher (id),
    constraint uk_course_assignment_course_group_teacher
        unique (course_id, group_id, teacher_id)
);
