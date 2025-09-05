CREATE TABLE projects (
    id UUID PRIMARY KEY,
    name TEXT NOT NULL,
    auth_enabled BOOLEAN NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE project_tables (
    project_id UUID NOT NULL,
    table_name TEXT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    PRIMARY KEY (project_id, table_name)
);
