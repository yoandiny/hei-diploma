alter table student
    add column if not exists deleted_at timestamp with time zone;
