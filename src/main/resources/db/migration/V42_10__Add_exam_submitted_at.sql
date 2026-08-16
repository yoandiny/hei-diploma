alter table exam
    add column if not exists submitted_at timestamp with time zone;
