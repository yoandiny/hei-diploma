create table if not exists exam_group
(
    exam_id  varchar not null
        constraint exam_group_exam_fk references exam (id) on delete cascade,
    group_id varchar not null
        constraint exam_group_group_fk references "group" (id),
    constraint exam_group_pk primary key (exam_id, group_id)
);
