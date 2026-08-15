create table if not exists course_assignment
(
    id         varchar not null
        constraint course_assignment_pk primary key,
    course_id  varchar not null
        constraint course_assignment_course_fk references course (id),
    group_id   varchar not null
        constraint course_assignment_group_fk references "group" (id),
    teacher_id varchar not null
        constraint course_assignment_teacher_fk references teacher (id),
    constraint uk_course_assignment_course_group_teacher
        unique (course_id, group_id, teacher_id)
);
