create table if not exists exam
(
    id          varchar                  not null
        constraint exam_pk primary key,
    course_id   varchar                  not null
        constraint exam_course_fk references course (id),
    date_exam   timestamp with time zone not null,
    coefficient numeric(5, 2)            not null
);

create table if not exists grade
(
    id         varchar                  not null
        constraint grade_pk primary key,
    exam_id    varchar                  not null
        constraint grade_exam_fk references exam (id),
    student_id varchar                  not null
        constraint grade_student_fk references student (id),
    value      numeric(5, 2)            not null,
    graded_by  varchar                  not null
        constraint grade_graded_by_fk references teacher (id),
    graded_at  timestamp with time zone not null,
    constraint uk_grade_exam_student unique (exam_id, student_id)
);
