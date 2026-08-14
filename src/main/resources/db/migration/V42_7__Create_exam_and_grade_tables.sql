create table if not exists j_exam
(
    id          varchar(255)             not null
        constraint j_exam_pk primary key,
    course_id   varchar(255)             not null
        constraint j_exam_course_fk references j_course (id),
    date_exam   timestamp with time zone not null,
    coefficient numeric(5, 2)            not null
);

create table if not exists j_grade
(
    id         varchar(255)             not null
        constraint j_grade_pk primary key,
    exam_id    varchar(255)             not null
        constraint j_grade_exam_fk references j_exam (id),
    student_id varchar(255)             not null
        constraint j_grade_student_fk references j_student (id),
    value      numeric(5, 2)            not null,
    graded_by  varchar(255)             not null
        constraint j_grade_graded_by_fk references j_teacher (id),
    graded_at  timestamp with time zone not null,
    constraint uk_grade_exam_student unique (exam_id, student_id)
);
